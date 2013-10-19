package fr.untitled2.servlet.api.command.batchlet;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.BatchTaskPayload;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.common.entities.raspi.batchlet.BatchTaskDescription;
import fr.untitled2.common.entities.raspi.batchlet.BatchTaskDescriptions;
import fr.untitled2.entities.*;
import fr.untitled2.servlet.api.command.Command;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/19/13
 * Time: 11:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class GetServerBatchTasks extends Command<BatchTaskPayload, BatchTaskDescriptions, SimpleResponse> {

    @Override
    protected BatchTaskDescriptions execute(BatchTaskPayload input, User user, String fromIpAddress) throws Exception {
        if (StringUtils.isEmpty(input.getServerId())) throw new Exception("Missing required parameter 'batchTaskId'");
        BatchServer batchServer = ObjectifyService.ofy().load().key(Key.create(BatchServer.class, input.getServerId())).get();
        if (batchServer == null) throw new Exception("No server found with id '" + input.getServerId() + "'");

        Iterable<BatchTask> batchTasks = ObjectifyService.ofy().load().type(BatchTask.class).filter("processingServer", batchServer);

        BatchTaskDescriptions result = new BatchTaskDescriptions();
        for (BatchTask batchTask : batchTasks) {
            if (batchTask == null)  throw new Exception("No batchTask found for id '" + input.getBatchTaskId() + "'");
            if (!batchTask.getUser().equals(user)) throw new Exception("This batchTask does not belong to you");

            BatchTaskDescription batchTaskDescription = new BatchTaskDescription();

            Batchlet batchlet = batchTask.getBatchlet();

            Pattern classPattern = Pattern.compile(".*\\.([A-Za-z0-9]+)$");
            Matcher classNameMatcher = classPattern.matcher(batchlet.getClassName());
            if (classNameMatcher.matches()) {
                batchTaskDescription.setBatchletName(classNameMatcher.group(1));
            } else {
                batchTaskDescription.setBatchletName(batchlet.getClassName());
            }
            batchTaskDescription.setBacthletId(batchlet.getId());

            batchTaskDescription.setBatchTaskId(batchTask.getId());
            if (batchTask.getStartDate() != null) {
                batchTaskDescription.setStartDate(DateTimeFormat.forPattern(user.getDateFormat() + " HH:mm").print(batchTask.getStartDate()));
            } else batchTaskDescription.setStartDate("N/A");

            if (batchTask.getEndDate() != null) {
                batchTaskDescription.setEndDate(DateTimeFormat.forPattern(user.getDateFormat() + " HH:mm").print(batchTask.getEndDate()));
            } else batchTaskDescription.setEndDate("N/A");

            BatchServer processingServer = batchTask.getProcessingServer();
            batchTaskDescription.setProcessingBatchServerId(processingServer.getServerId());
            batchTaskDescription.setProcessingBatchServerName(processingServer.getHostName());

            BatchServer requestServer = batchTask.getFromServer();
            batchTaskDescription.setRequestBatchServerId(requestServer.getServerId());
            batchTaskDescription.setRequestBatchServerName(requestServer.getHostName());
            result.getDescriptions().add(batchTaskDescription);
        }
        return result;
    }

    @Override
    protected Class<BatchTaskPayload> getInputObjectType() {
        return BatchTaskPayload.class;
    }

    @Override
    protected Class<BatchTaskDescriptions> getOutputObjectType() {
        return BatchTaskDescriptions.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
