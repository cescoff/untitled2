package fr.untitled2.servlet.api.command;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.BatchTaskPayload;
import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.BatchTask;
import fr.untitled2.entities.User;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class GetNextBatchTask extends Command<ServerConfig, BatchTaskPayload, SimpleResponse> {

    @Override
    protected BatchTaskPayload execute(ServerConfig input, User user, String fromIpAddress) throws Exception {
        List<BatchTask> userBatchTasks = ObjectifyService.ofy().load().type(BatchTask.class).filter("user", user).list();
        userBatchTasks = Ordering.natural().onResultOf(new Function<BatchTask, LocalDateTime>() {
            @Override
            public LocalDateTime apply(BatchTask o) {
                return o.getStartDate();
            }
        }).sortedCopy(userBatchTasks);

        BatchServer processingServer = ObjectifyService.ofy().load().key(Key.create(BatchServer.class, input.getServerId())).get();
        processingServer.setLastContactDate(LocalDateTime.now());
        ObjectifyService.ofy().save().entity(processingServer).now();
        for (BatchTask userBatchTask : userBatchTasks) {
            if (!userBatchTask.isDone()) {
                if (userBatchTask.getLastReadDate() == null || userBatchTask.getLastReadDate().plusHours(1).isBefore(LocalDateTime.now())) {
                    BatchTaskPayload batchTaskPayload = new BatchTaskPayload();
                    batchTaskPayload.setBatchTaskId(userBatchTask.getId());
                    batchTaskPayload.setBatchletClassName(userBatchTask.getBatchlet().getClassName());
                    batchTaskPayload.setZippedPayload(userBatchTask.getInputJson());
                    batchTaskPayload.setLogLevel(userBatchTask.getBatchlet().getLogLevel());

                    userBatchTask.setProcessingServer(processingServer);
                    userBatchTask.setLastReadDate(LocalDateTime.now());
                    ObjectifyService.ofy().save().entity(userBatchTask).now();



                    return batchTaskPayload;
                }
            }
        }
        return new BatchTaskPayload();
    }

    @Override
    protected Class<ServerConfig> getInputObjectType() {
        return ServerConfig.class;
    }

    @Override
    protected Class<BatchTaskPayload> getOutputObjectType() {
        return BatchTaskPayload.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
