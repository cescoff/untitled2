package fr.untitled2.servlet.api.command;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.common.entities.raspi.ServerStatistic;
import fr.untitled2.common.entities.raspi.ServerStatuses;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.common.entities.raspi.Statistics;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.User;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/27/13
 * Time: 12:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatisticsRegister extends Command<Statistics, SimpleResponse, ServerStatuses> {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsRegister.class);

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
    protected SimpleResponse execute(Statistics input, User user, String fromIpAddress) throws Exception {
        logger.info("Statistics registration start");
        Iterable<BatchServer> batchServers = ObjectifyService.ofy().load().type(BatchServer.class).filter("user", user);
        for (BatchServer batchServer : batchServers) {
            logger.info("Found batch server->'" + batchServer.getServerId() + "'");
            if (batchServer.getServerId().equals(input.getServerId())) {
                logger.info("Server '" + batchServer.getServerId() + "' matches");
                batchServer.setUptime(input.getUptime());
                batchServer.setUser(user);
                batchServer.setLastContactDate(LocalDateTime.now());

                List<ServerStatistic> statistics = Lists.newArrayList();

                ServerStatistic serverStatistic = new ServerStatistic();
                serverStatistic.setCpuPercentage(input.getCpuPercentage());
                serverStatistic.setDate(LocalDateTime.now());
                serverStatistic.setLoadAverage(input.getLoadAverage());
                serverStatistic.setMemoryPercentage(input.getMemoryPercentage());

                statistics.add(serverStatistic);
                LocalDateTime minDate = LocalDateTime.now().minusHours(24);
                for (ServerStatistic oldStats : batchServer.getStatistics()) {
                    if (oldStats.getDate().isAfter(minDate)) {
                        statistics.add(oldStats);
                    }
                }

                statistics = Ordering.natural().onResultOf(new Function<ServerStatistic, LocalDateTime>() {
                    @Override
                    public LocalDateTime apply(ServerStatistic serverStatistic) {
                        return serverStatistic.getDate();
                    }
                }).sortedCopy(statistics);
                batchServer.setStatistics(statistics);
                batchServer.setOauthCode(null);
                batchServer.setGenerateTokenUrl(null);

                batchServerBusiness.persist(batchServer);
                return new SimpleResponse(true);
            }
        }
        logger.info("No server found for id '" + input.getServerId() + "'");
        return new SimpleResponse(false);
    }

    @Override
    protected Class<Statistics> getInputObjectType() {
        return Statistics.class;
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
