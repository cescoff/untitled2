package fr.untitled2.servlet.api.command;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.common.entities.raspi.*;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.User;
import fr.untitled2.utils.CollectionUtils;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/30/13
 * Time: 10:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetServerStatistics extends Command<GetServerInput, StatisticsGraph, SimpleResponse> {

    private static BatchServerBusiness batchServerBusiness = new BatchServerBusiness();

    @Override
    protected SimpleResponse status(User user) throws Exception {
        return new SimpleResponse(true);
    }

    @Override
    protected StatisticsGraph execute(GetServerInput input, User user, String fromIpAddress) throws Exception {
        BatchServer batchServer = batchServerBusiness.getBatchServer(user, input.getServerId());
        if (batchServer != null) {
            List<ServerStatistic> stats = batchServer.getStatistics();
            if (CollectionUtils.isNotEmpty(stats)) {
                stats = Ordering.natural().onResultOf(new Function<ServerStatistic, LocalDateTime>() {
                    @Override
                    public LocalDateTime apply(ServerStatistic serverStatistic) {
                        return serverStatistic.getDate();
                    }
                }).sortedCopy(stats);

/*                List<List<ServerStatistic>> entries = Lists.newArrayList();
                LocalDateTime periodEnd = LocalDateTime.now();
                LocalDateTime periodStart = LocalDateTime.now().minusHours(24);
                LocalDateTime cursor = periodStart.plusMinutes(5);

                StatisticsGraphEntry statisticsGraphEntry = new StatisticsGraphEntry();
                statisticsGraphEntry.setDate(periodStart);
                entries.add(statisticsGraphEntry);

                while (cursor.isBefore(periodEnd)) {
                    StatisticsGraphEntry cursorEntry = new StatisticsGraphEntry();
                    cursorEntry.setDate(cursor);
                    entries.add(cursorEntry);
                    cursor = cursor.plusMinutes(5);
                }

                for (ServerStatistic stat : stats) {
                    for (StatisticsGraphEntry entry : entries) {

                    }
                }*/

            }
        }
        return new StatisticsGraph();
    }

    @Override
    protected Class<GetServerInput> getInputObjectType() {
        return GetServerInput.class;
    }

    @Override
    protected Class<StatisticsGraph> getOutputObjectType() {
        return StatisticsGraph.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
