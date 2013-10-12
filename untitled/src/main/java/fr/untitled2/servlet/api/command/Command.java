package fr.untitled2.servlet.api.command;

import fr.untitled2.entities.User;
import fr.untitled2.servlet.api.ServerServlet;
import fr.untitled2.utils.JSonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 10:07 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Command<F, T, S> {

    private static Logger logger = LoggerFactory.getLogger(Command.class);

    public final String execute(String json, String fromIpAddress) throws Exception {
        return execute(json, null, fromIpAddress);
    }

    public final String execute(String json, User user, String fromIpAddress) throws Exception {
        logger.info("Executing command '" + getClass().getName() + "'");
        F input = JSonUtils.readJson(getInputObjectType(), json);

        if (!isPublic()) {
            T result = execute(input, user, fromIpAddress);
            if (result != null) return JSonUtils.writeJson(result);
            else return null;
        } else {
            T result = execute(input, fromIpAddress);
            if (result != null) return JSonUtils.writeJson(result);
            else return null;
        }
    }

    public final String executeStatus() throws Exception {
        return executeStatus(null);
    }

    public final String executeStatus(User user) throws Exception {
        S status = null;
        if (isPublic()) {
            status = status();
        } else {
            status = status(user);
        }
        if (status != null) {
            return JSonUtils.writeJson(status);
        }
        return null;
    }

    protected S status(User user) throws Exception {
        if (isPublic()) throw new Exception("Public command does not support private access");
        return null;
    }

    protected S status() throws Exception {
        if (!isPublic()) throw new Exception("Private command does not support public access");
        return null;
    }

    protected T execute(F input, String fromIpAddress) throws Exception {
        if (!isPublic()) throw new Exception("Private command does not support public access");
        return null;
    }

    protected T execute(F input, User user, String fromIpAddress) throws Exception {
        if (isPublic())  throw new Exception("Public command does not support private access");
        return null;
    }

    protected abstract Class<F> getInputObjectType();

    protected abstract Class<T> getOutputObjectType();

    public abstract boolean isPublic();

}
