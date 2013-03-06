package fr.untitled2.business;

import com.google.appengine.labs.repackaged.com.google.common.collect.Iterables;
import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.beans.LogList;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.User;
import fr.untitled2.utils.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 10:51 AM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class LogBusiness {

    private static Logger logger = LoggerFactory.getLogger(LogBusiness.class);

    public LogList getLogList(int pageNumber, final User user) {
        Iterable<Log> trips = ObjectifyService.ofy().load().type(Log.class).filter("user", user);
        trips = Iterables.transform(trips, new com.google.appengine.labs.repackaged.com.google.common.base.Function<Log, Log>() {
            @Override
            public Log apply(Log log) {
                Log result = log.clone();
                String timeZoneId = user.getTimeZoneId();
                if (StringUtils.isNotEmpty(result.getTimeZoneId())) {
                    timeZoneId = result.getTimeZoneId();
                } else {
                    result.setTimeZoneId(timeZoneId);
                }
                result.setStartTime(result.getStartTime().toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID(timeZoneId)).toLocalDateTime());
                result.setEndTime(result.getEndTime().toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID(timeZoneId)).toLocalDateTime());
                return result;
            }
        });
        List<List<Log>> pages = Lists.partition(Lists.newArrayList(Ordering.natural().reverse().onResultOf(new Function<Log, LocalDateTime>() {
            @Override
            public LocalDateTime apply(Log trip) {
                return trip.getStartTime();
            }
        }).sortedCopy(trips)), 20);

        if (CollectionUtils.isNotEmpty(pages) && pageNumber < pages.size()) {
            int nextPageNumber = pageNumber + 1;
            if (nextPageNumber >= pages.size()) nextPageNumber = 0;
            LogList logList = new LogList(pageNumber, nextPageNumber);
            logList.getLogs().addAll(pages.get(pageNumber));
            return logList;
        }
        return null;
    }
}
