package fr.untitled2.servlet.api.command;

import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.business.CacheHelper;
import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.entities.raspi.ServerStatuses;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.User;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/27/13
 * Time: 2:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class AttachServer extends Command<ServerConfig, SimpleResponse, ServerStatuses> {

    private static BatchServerBusiness batchServerBusiness = new BatchServerBusiness();

    @Override
    protected ServerStatuses status(User user) throws Exception {
        Iterable<BatchServer> batchServers = ObjectifyService.ofy().load().type(BatchServer.class).filter("user", user);

        ServerStatuses serverStatuses = new ServerStatuses();
        for (BatchServer batchServer : batchServers) {
            serverStatuses.getServerStatuses().add(new ServerStatuses.ServerStatus(batchServer.getHostName(), batchServer.getHostIp(), batchServer.getCreationDate().toString(), batchServer.getUptime()));
        }

        return serverStatuses;
    }

    @Override
    protected SimpleResponse execute(ServerConfig input, User user, String fromIpAddress) throws Exception {
        List<BatchServer> batchServers = ObjectifyService.ofy().load().type(BatchServer.class).list();

        for (BatchServer batchServer : batchServers) {
            if (batchServer.getServerId().equals(input.getServerId())) {
                batchServer.setUser(user);
                batchServer.setGenerateTokenUrl(null);
                batchServer.setOauthCode(null);
                ObjectifyService.ofy().save().entity(batchServer).now();
                batchServerBusiness.unregisterPendingServer(batchServer);
                return new SimpleResponse(true);
            }
        }

        return new SimpleResponse(false);
    }

    @Override
    protected Class<ServerConfig> getInputObjectType() {
        return ServerConfig.class;
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
