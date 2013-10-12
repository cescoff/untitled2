package fr.untitled2.business;

import com.beust.jcommander.internal.Lists;
import com.google.appengine.labs.repackaged.com.google.common.base.Joiner;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.PendingBatchServer;
import fr.untitled2.entities.User;
import fr.untitled2.utils.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 12:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class BatchServerBusiness {


    public BatchServer getBatchServer(User user, String serverId) {
        BatchServer batchServer = ObjectifyService.ofy().load().key(Key.create(BatchServer.class, serverId)).get();
        if (batchServer != null) {
            if (batchServer.getUser() == null) return batchServer;
            if (batchServer.getUser().equals(user)) {
                return batchServer;
            }
        }
        return null;
    }


    public List<BatchServer> getUserServers(User user) {
        return getUserServers(user, null);
    }

    public List<BatchServer> getUserServers(User user, String hostIp) {
        List<BatchServer> result = Lists.newArrayList();

        PendingBatchServer pendingBatchServer = ObjectifyService.ofy().load().key(Key.create(PendingBatchServer.class, hostIp)).get();

        if (pendingBatchServer != null) {
            String ids = pendingBatchServer.getServerIds();

            for (String id : ids.split(",")) {
                BatchServer batchServer = ObjectifyService.ofy().load().key(Key.create(BatchServer.class, id)).get();
                if (batchServer != null) {
                    result.add(batchServer);
                }
            }
        }

        List<BatchServer> batchServers = ObjectifyService.ofy().load().type(BatchServer.class).filter("user", user).list();
        if (CollectionUtils.isNotEmpty(batchServers)) result.addAll(batchServers);

        return result;
    }

    public void persist(BatchServer batchServer) {
        ObjectifyService.ofy().save().entity(batchServer).now();
    }

    public void registerPendingServer(BatchServer batchServer) {
        if (batchServer == null) return;
        String serverId = batchServer.getServerId();
        String hostIpAddress = batchServer.getHostIp();

        if (StringUtils.isEmpty(serverId) || StringUtils.isEmpty(hostIpAddress)) return;

        PendingBatchServer pendingBatchServer = ObjectifyService.ofy().load().key(Key.create(PendingBatchServer.class, hostIpAddress)).get();

        if (pendingBatchServer == null) {
            pendingBatchServer = new PendingBatchServer();
            pendingBatchServer.setHostIpAddress(hostIpAddress);
        }
        String ids = pendingBatchServer.getServerIds();
        List<String> idList = Lists.newArrayList();
        if (StringUtils.isNotEmpty(ids)) {
            idList = Lists.newArrayList(ids.split(","));
        }
        if (!idList.contains(serverId)) idList.add(serverId);
        pendingBatchServer.setServerIds(Joiner.on(",").join(idList));

        ObjectifyService.ofy().save().entity(pendingBatchServer).now();
    }

    public void unregisterPendingServer(BatchServer batchServer) {
        if (batchServer == null) return;
        String serverId = batchServer.getServerId();
        String hostIpAddress = batchServer.getHostIp();

        if (StringUtils.isEmpty(serverId) || StringUtils.isEmpty(hostIpAddress)) return;

        PendingBatchServer pendingBatchServer = ObjectifyService.ofy().load().key(Key.create(PendingBatchServer.class, hostIpAddress)).get();

        if (pendingBatchServer == null) return;

        String ids = pendingBatchServer.getServerIds();

        if (StringUtils.isEmpty(ids)) return;

        List<String> idList = Lists.newArrayList(ids.split(","));
        List<String> filteredList = Lists.newArrayList();

        for (String id : idList) {
            if (!serverId.equals(id)) {
                filteredList.add(id);
            }
        }
        if (CollectionUtils.isEmpty(filteredList)) {
            ObjectifyService.ofy().delete().entity(pendingBatchServer);
        } else {
            pendingBatchServer.setServerIds(Joiner.on(",").join(filteredList));
            ObjectifyService.ofy().save().entity(pendingBatchServer);
        }
    }

}
