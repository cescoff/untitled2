package fr.untitled2.servlet.api.command;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.BatchTaskPayload;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.BatchTask;
import fr.untitled2.entities.User;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class MarkBatchTaskAsDone extends Command<BatchTaskPayload, SimpleResponse, SimpleResponse> {

    @Override
    protected SimpleResponse execute(BatchTaskPayload input, User user, String fromIpAddress) throws Exception {
        if (StringUtils.isEmpty(input.getBatchTaskId())) throw new Exception("Missing required parameter batchTaskId");
        if (StringUtils.isEmpty(input.getZippedPayload())) throw new Exception("Missing required parameter zippedPayload");

        BatchTask batchTask = ObjectifyService.ofy().load().key(Key.create(BatchTask.class, input.getBatchTaskId())).get();

        if (batchTask == null) throw new Exception("No batch task for id '" + input.getBatchTaskId() + "'");

        if (!batchTask.getUser().equals(user)) throw new Exception("BatchTask '" + input.getBatchTaskId() + "' does not belong to the connected user");

        batchTask.setSuccess(input.isSuccess());
        batchTask.setOutputJson(input.getZippedPayload());
        batchTask.setLog(input.getLog());
        batchTask.setEndDate(LocalDateTime.now());
        batchTask.setLastReadDate(LocalDateTime.now());
        if (StringUtils.isNotEmpty(input.getServerId()) && batchTask.getProcessingServer() == null) {
            BatchServer batchServer = ObjectifyService.ofy().load().key(Key.create(BatchServer.class, input.getServerId())).get();
            batchTask.setProcessingServer(batchServer);
            batchServer.setLastContactDate(LocalDateTime.now());
            ObjectifyService.ofy().save().entity(batchServer).now();
        } else if (batchTask.getProcessingServer() != null) {
            BatchServer processingServer = batchTask.getProcessingServer();
            processingServer.setLastContactDate(LocalDateTime.now());
            ObjectifyService.ofy().save().entity(processingServer);
        }

        ObjectifyService.ofy().save().entity(batchTask).now();

        return new SimpleResponse(true);
    }

    @Override
    protected Class<BatchTaskPayload> getInputObjectType() {
        return BatchTaskPayload.class;
    }

    @Override
    protected Class<SimpleResponse> getOutputObjectType() {
        return SimpleResponse.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
