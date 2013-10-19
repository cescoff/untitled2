package fr.untitled2.servlet.api.command;

import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.business.BatchletBusiness;
import fr.untitled2.common.entities.raspi.BatchTaskPayload;
import fr.untitled2.common.entities.raspi.RegisterBatchTaskPayload;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.BatchTask;
import fr.untitled2.entities.Batchlet;
import fr.untitled2.entities.User;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterBatchTask extends Command<RegisterBatchTaskPayload, BatchTaskPayload, SimpleResponse> {

    private static BatchServerBusiness batchServerBusiness = new BatchServerBusiness();

    private static BatchletBusiness batchletBusiness = new BatchletBusiness();

    @Override
    protected BatchTaskPayload execute(RegisterBatchTaskPayload input, User user, String fromIpAddress) throws Exception {
        if (StringUtils.isEmpty(input.getServerId())) throw new Exception("Missing required input field serverId");
        if (StringUtils.isEmpty(input.getZippedPayload())) throw new Exception("Missing required input field zippedPaylod");

        BatchServer fromServer = batchServerBusiness.getBatchServer(user, input.getServerId());

        if (fromServer == null) throw new Exception("No server '" + input.getServerId() + "' is known for current user");

        Batchlet batchlet = batchletBusiness.getMatchingBatchlet(input.getBatchletClass(), user);

        if (batchlet == null) throw new Exception("The batchlet class '" + input.getBatchletClass() + "' is not declared yet");

        String id = SignUtils.calculateSha1Digest(input.getServerId() + user.getUserId() + LocalDateTime.now());
        BatchTask batchTask = new BatchTask();
        batchTask.setId(id);
        batchTask.setFromServer(fromServer);
        batchTask.setInputJson(input.getZippedPayload());
        batchTask.setStartDate(LocalDateTime.now());
        batchTask.setBatchlet(batchlet);
        batchTask.setUser(user);

        ObjectifyService.ofy().save().entity(batchTask).now();

        BatchTaskPayload batchTaskPayload = new BatchTaskPayload();
        batchTaskPayload.setBatchletClassName(input.getBatchletClass());
        batchTaskPayload.setBatchTaskId(id);

        return batchTaskPayload;
    }

    @Override
    protected Class<RegisterBatchTaskPayload> getInputObjectType() {
        return RegisterBatchTaskPayload.class;
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
