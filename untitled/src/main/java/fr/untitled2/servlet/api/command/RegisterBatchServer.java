package fr.untitled2.servlet.api.command;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.business.CacheHelper;
import fr.untitled2.common.entities.raspi.ServerStatuses;
import fr.untitled2.common.entities.raspi.ServerRegistrationConfig;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 10:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterBatchServer extends Command<ServerRegistrationConfig, ServerRegistrationConfig, ServerStatuses> {

    private static final Logger logger = LoggerFactory.getLogger(RegisterBatchServer.class);

    public static BatchServerBusiness batchServerBusiness = new BatchServerBusiness();

    @Override
    protected ServerStatuses status() throws Exception {
        List<BatchServer> batchServers = ObjectifyService.ofy().load().type(BatchServer.class).list();
        ServerStatuses serverStatuses = new ServerStatuses();

        for (BatchServer batchServer : batchServers) {
            if (batchServer.getUser() == null) {
                serverStatuses.getServerStatuses().add(new ServerStatuses.ServerStatus(batchServer.getHostName(), batchServer.getHostIp(), batchServer.getCreationDate().toString()));
            }
        }
        return serverStatuses;
    }

    @Override
    protected ServerRegistrationConfig execute(ServerRegistrationConfig input, String fromIpAddress) throws Exception {
        logger.info("Registration command launched");
        if (input == null) return null;

        BatchServer batchServer = new BatchServer();
        String serverId = null;
        if (StringUtils.isEmpty(input.getServerConfig().getServerId())) {
            logger.info("No server id in input");
            serverId = SignUtils.calculateSha1Digest(fromIpAddress + LocalDateTime.now().toString() + input.getServerConfig().getServerName());
            batchServer.setServerId(serverId);
            batchServer.setCreationDate(LocalDateTime.now());
            batchServer.setGenerateTokenUrl(input.getTokenGenerationUrl());
            batchServer.setHostIp(fromIpAddress);
            batchServer.setHostName(input.getServerConfig().getServerName());
            batchServer.setNumberOfCpuCore(input.getServerConfig().getCpuCoreCount());
            batchServer.setLastContactDate(LocalDateTime.now());
            batchServerBusiness.persist(batchServer);

        } else {
            logger.info("Server id in input : '" + input.getServerConfig().getServerId() + "'");
            batchServer = ObjectifyService.ofy().load().key(Key.create(BatchServer.class, input.getServerConfig().getServerId())).get();
            if (batchServer == null) {
                logger.info("No server entity found for id '" + input.getServerConfig().getServerId() + "'");
                batchServer = new BatchServer();
                serverId = SignUtils.calculateSha1Digest(fromIpAddress + LocalDateTime.now().toString() + input.getServerConfig().getServerName());
                batchServer.setServerId(serverId);
                batchServer.setCreationDate(LocalDateTime.now());
                batchServer.setGenerateTokenUrl(input.getTokenGenerationUrl());
                batchServer.setHostIp(fromIpAddress);
                batchServer.setHostName(input.getServerConfig().getServerName());
                batchServer.setNumberOfCpuCore(input.getServerConfig().getCpuCoreCount());
            } else {
                serverId = batchServer.getServerId();
            }
            if (StringUtils.isNotEmpty(input.getTokenGenerationUrl())) batchServer.setGenerateTokenUrl(input.getTokenGenerationUrl());
            batchServer.setLastContactDate(LocalDateTime.now());
            batchServer.setHostIp(fromIpAddress);

            batchServerBusiness.persist(batchServer);
        }
        if (StringUtils.isNotEmpty(batchServer.getOauthCode())) {
            logger.info("Found OAuthCode");
            input.setTokenCode(batchServer.getOauthCode());
        } else logger.info("No OAuthCode found");
        input.getServerConfig().setServerId(serverId);

        batchServerBusiness.registerPendingServer(batchServer);

        return input;
    }

    @Override
    protected Class<ServerRegistrationConfig> getInputObjectType() {
        return ServerRegistrationConfig.class;
    }

    @Override
    protected Class<ServerRegistrationConfig> getOutputObjectType() {
        return ServerRegistrationConfig.class;
    }

    @Override
    public boolean isPublic() {
        return true;
    }
}
