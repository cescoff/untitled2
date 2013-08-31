package fr.untitled2.business;

import com.google.appengine.labs.repackaged.com.google.common.collect.Iterables;
import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.beans.LogList;
import fr.untitled2.entities.*;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.DistanceUtils;
import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
                if (result.getStartTime() != null) result.setStartTime(result.getStartTime().toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID(timeZoneId)).toLocalDateTime());
                if (result.getEndTime() != null) result.setEndTime(result.getEndTime().toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID(timeZoneId)).toLocalDateTime());
                return result;
            }
        });
        List<List<Log>> pages = Lists.partition(Lists.newArrayList(Ordering.natural().reverse().onResultOf(new Function<Log, LocalDateTime>() {
            @Override
            public LocalDateTime apply(Log trip) {
                if (trip.getStartTime() != null) return trip.getStartTime();
                else return LocalDateTime.now();
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

    public Key<Log> persistLog(User user, Log log) {
        Log currentRegisteringLog = getLogInProgress(user, log);

        LocalDateTime logEnd = currentRegisteringLog.getEndTime();

        if (currentRegisteringLog != null) {

            for (TrackPoint trackPoint : log.getTrackPoints()) {
                if (trackPoint.getPointDate().isAfter(logEnd)) {
                    logger.info("Les points existants ne contiennent pas le point (" + trackPoint + ")");
                    currentRegisteringLog.getTrackPoints().add(trackPoint);
                } else logger.info("Le point (" + trackPoint + ") existe dejà");
            }
            Key<Log> logKey = ObjectifyService.ofy().save().entity(currentRegisteringLog).now();
            LogStatistics logStatistics = getLogStatistics(currentRegisteringLog);
            ObjectifyService.ofy().save().entity(logStatistics).now();
            return logKey;
        } else {
            logger.info("Nouveau log à persister");
            log.setUser(user);
            Key<Log> logKey = ObjectifyService.ofy().save().entity(log).now();
            LogStatistics logStatistics = getLogStatistics(log);
            ObjectifyService.ofy().save().entity(logStatistics).now();
            return logKey;
        }

    }

    public void updateLogStatistics(Log log) {
        LogStatistics logStatistics = getLogStatistics(log);
        ObjectifyService.ofy().save().entity(logStatistics).now();
    }

    public void deleteLog(Log log) {
        LogStatistics logStatistics = getLogStatistics(log);
        LogTrackPoints logTrackPoints = ObjectifyService.ofy().load().key(Key.create(LogTrackPoints.class, log.getInternalId())).get();
        ObjectifyService.ofy().delete().entity(log).now();
        ObjectifyService.ofy().delete().entity(logTrackPoints).now();
        ObjectifyService.ofy().delete().entity(logStatistics).now();
    }

    private LogStatistics getLogStatistics(Log log) {
        LogStatistics logStatistics = new LogStatistics();
        logStatistics.setLogKey(log.getInternalId());
        logStatistics.setPointCount(log.getTrackPoints().size());

        double distance = 0D;

        List<TrackPoint> sortedTrackPoints = Ordering.natural().onResultOf(new Function<TrackPoint, LocalDateTime>() {
            @Override
            public LocalDateTime apply(TrackPoint trackPoint) {
                return trackPoint.getPointDate();
            }
        }).sortedCopy(log.getTrackPoints());

        if (CollectionUtils.isNotEmpty(sortedTrackPoints) && sortedTrackPoints.size() > 1) {
            TrackPoint trackStart = sortedTrackPoints.get(0);
            LocalDateTime start = trackStart.getPointDate();
            LocalDateTime end = trackStart.getPointDate();
            for (int index = 1; index < sortedTrackPoints.size(); index++) {
                distance += DistanceUtils.getDistance(Pair.with(trackStart.getLatitude(), trackStart.getLongitude()), Pair.with(sortedTrackPoints.get(index).getLatitude(), sortedTrackPoints.get(index).getLongitude()));
                trackStart = sortedTrackPoints.get(index);
                end = trackStart.getPointDate();
            }
            logStatistics.setStart(start);
            logStatistics.setEnd(end);
        }
        logStatistics.setDistance(distance);

        return logStatistics;
    }

    private Log getLogInProgress(User user, Log log) {
        int pageNumber = 0;

        LogList logList = null;

        do {
            logList = getLogList(pageNumber, user);

            for (Log existingLog : logList.getLogs()) {
                if (existingLog.getStartTime() != null && existingLog.getStartTime().equals(log.getStartTime()) && existingLog.getTimeZoneId().equals(log.getTimeZoneId())) return existingLog;
            }

            pageNumber++;
        } while (logList.getNextPageNumber() != 0);
        return null;
    }



}
