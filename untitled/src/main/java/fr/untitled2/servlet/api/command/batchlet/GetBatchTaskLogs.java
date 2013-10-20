package fr.untitled2.servlet.api.command.batchlet;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.BatchTaskPayload;
import fr.untitled2.common.entities.raspi.SimpleStringMessage;
import fr.untitled2.entities.BatchTask;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.api.command.Command;
import fr.untitled2.utils.GzipUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/19/13
 * Time: 11:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class GetBatchTaskLogs extends Command<BatchTaskPayload, SimpleStringMessage, SimpleStringMessage> {

    @Override
    protected SimpleStringMessage execute(BatchTaskPayload input, User user, String fromIpAddress) throws Exception {
        if (StringUtils.isEmpty(input.getBatchTaskId())) throw new Exception("Missing required parameter 'batchTaskId'");
        BatchTask batchTask = ObjectifyService.ofy().load().key(Key.create(BatchTask.class, input.getBatchTaskId())).get();
        if (batchTask == null)  throw new Exception("No batchTask found for id '" + input.getBatchTaskId() + "'");
        if (!batchTask.getUser().equals(user)) throw new Exception("This batchTask does not belong to you");
        SimpleStringMessage result = new SimpleStringMessage();
        if (StringUtils.isNotEmpty(batchTask.getLog())) {
            String log = GzipUtils.unzipString(batchTask.getLog());
            if (StringUtils.isNotEmpty(log)) {
                result.setMessage(log);
            } else {
                result.setMessage("Log is empty");
            }
        } else {
            result.setMessage("Log is empty");
        }
        return result;
    }

    @Override
    protected Class<BatchTaskPayload> getInputObjectType() {
        return BatchTaskPayload.class;
    }

    @Override
    protected Class<SimpleStringMessage> getOutputObjectType() {
        return SimpleStringMessage.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
