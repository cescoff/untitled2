package fr.untitled2.servlet.api.command;

import fr.untitled2.common.entities.JourneysStatistics;
import fr.untitled2.common.entities.RemoteError;
import fr.untitled2.common.entities.raspi.SimpleStringMessage;
import fr.untitled2.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 08/12/13
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */
public class LogError extends Command<RemoteError, SimpleStringMessage, SimpleStringMessage> {

    private static final Logger logger = LoggerFactory.getLogger(LogError.class);

    @Override
    protected SimpleStringMessage execute(RemoteError input, User user, String fromIpAddress) throws Exception {
        logger.error("[" + fromIpAddress + "] : (" + user.getEmail() + ") : " + input.getMessage());
        logger.error("[" + fromIpAddress + "] : (" + user.getEmail() + ") : " + input.getStackTrace());
        SimpleStringMessage simpleStringMessage = new SimpleStringMessage();
        simpleStringMessage.setMessage("Error logged");
        return simpleStringMessage;

    }

    @Override
    protected Class<RemoteError> getInputObjectType() {
        return RemoteError.class;
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
