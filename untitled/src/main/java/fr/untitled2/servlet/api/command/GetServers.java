package fr.untitled2.servlet.api.command;

import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.common.entities.raspi.GetServerInput;
import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.entities.raspi.ServerInfos;
import fr.untitled2.common.entities.raspi.ServerList;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.User;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 6:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetServers extends Command<GetServerInput, ServerList, ServerList> {

    private BatchServerBusiness batchServerBusiness = new BatchServerBusiness();

    @Override
    protected ServerList status(User user) throws Exception {
        ServerList serverList = new ServerList();
        List<BatchServer> batchServers = batchServerBusiness.getUserServers(user);
        for (BatchServer batchServer : batchServers) {
            serverList.getServers().add(toServerInfos(batchServer, user));
        }
        return serverList;
    }

    @Override
    protected ServerList execute(GetServerInput input, User user, String fromIpAddress) throws Exception {
        ServerList serverList = new ServerList();
        List<BatchServer> batchServers = batchServerBusiness.getUserServers(user, fromIpAddress);
        for (BatchServer batchServer : batchServers) {
            serverList.getServers().add(toServerInfos(batchServer, user));
        }



        return serverList;
    }

    private ServerInfos toServerInfos(BatchServer batchServer, User user) {
        ServerInfos serverInfos = new ServerInfos();

        String uptime = batchServer.getUptime();

        if (StringUtils.indexOf(uptime, "up") >= 0) {
            uptime = StringUtils.substring(uptime, StringUtils.indexOf(uptime, "up") + 2);
            if (StringUtils.indexOf(uptime, ",") >= 0) {
                uptime = StringUtils.substring(uptime, 0, StringUtils.indexOf(uptime, ","));
                uptime = uptime.trim();
            }
        }

        serverInfos.setUptime(uptime);
        serverInfos.setServerId(batchServer.getServerId());
        serverInfos.setCpuCoreCount(batchServer.getNumberOfCpuCore());
        serverInfos.setCreationDate(DateTimeFormat.forPattern(user.getDateFormat()).print(batchServer.getCreationDate()));
        if (batchServer.getLastContactDate() != null) {
            serverInfos.setLastContactDateTime(DateTimeFormat.forPattern(user.getDateFormat() + " HH:mm").print(batchServer.getLastContactDate()));
            serverInfos.setOnLine(LocalDateTime.now().minusMinutes(5).isBefore(batchServer.getLastContactDate()));
        } else serverInfos.setOnLine(false);

        serverInfos.setServerName(batchServer.getHostName());
        serverInfos.setConnected(StringUtils.isEmpty(batchServer.getGenerateTokenUrl()) && StringUtils.isEmpty(batchServer.getOauthCode()));



        return serverInfos;
    }

    @Override
    protected Class<GetServerInput> getInputObjectType() {
        return GetServerInput.class;
    }

    @Override
    protected Class<ServerList> getOutputObjectType() {
        return ServerList.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
