package fr.untitled2.servlet.api.command;

import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.common.entities.raspi.PushVerificationCodeInput;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.User;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/29/13
 * Time: 10:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PushVerificationCode extends Command<PushVerificationCodeInput, SimpleResponse, SimpleResponse> {

    private static BatchServerBusiness batchServerBusiness = new BatchServerBusiness();

    @Override
    protected SimpleResponse execute(PushVerificationCodeInput input, User user, String fromIpAddress) throws Exception {
        BatchServer batchServer = batchServerBusiness.getBatchServer(user, input.getServerId());
        if (batchServer == null) {
            return new SimpleResponse(false);
        }

        batchServer.setOauthCode(input.getVerificationCode());
        batchServerBusiness.persist(batchServer);
        return new SimpleResponse(true);
    }

    @Override
    protected Class<PushVerificationCodeInput> getInputObjectType() {
        return PushVerificationCodeInput.class;
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
