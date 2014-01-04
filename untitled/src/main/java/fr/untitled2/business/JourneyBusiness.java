package fr.untitled2.business;

import com.beust.jcommander.internal.Lists;
import com.google.appengine.labs.repackaged.com.google.common.base.Function;
import com.google.appengine.labs.repackaged.com.google.common.collect.Collections2;
import com.google.appengine.labs.repackaged.com.google.common.collect.Iterables;
import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.Journey;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.entities.*;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 23/11/13
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */
public class JourneyBusiness {

    private static Logger logger = LoggerFactory.getLogger(JourneyBusiness.class);

    private static LogBusiness logBusiness = new LogBusiness();

    public JourneyEntity getJourney(String id) {
        return ObjectifyService.ofy().load().key(Key.create(JourneyEntity.class, id)).get();
    }

    public Optional<JourneyCalculationJob> getLastJob(User user) {
        LocalDateTime maxCalculationJobAge = LocalDateTime.now().minusHours(3);
        JourneyCalculationJob result = null;

        List<JourneyCalculationJob> jobs = getUserJobs(user);
        if (CollectionUtils.isEmpty(jobs)) {
            logger.info("No calculation jobs available (" + user.getEmail() + ")");
            return Optional.absent();
        }
        for (JourneyCalculationJob journeyCalculationJob : jobs) {
            if (result == null) result = journeyCalculationJob;
            else if (journeyCalculationJob.getEndDateTime().isAfter(result.getEndDateTime())) result = journeyCalculationJob;
            if (maxCalculationJobAge.isBefore(journeyCalculationJob.getEndDateTime())) {
                ObjectifyService.ofy().delete().entity(journeyCalculationJob).now();
            }
        }
        if (result != null) return Optional.of(result);
        return Optional.absent();
    }

    private List<JourneyCalculationJob> getUserJobs(User user) {
        List<JourneyCalculationJob> result = Lists.newArrayList();
        for (JourneyCalculationJob journeyCalculationJob : ObjectifyService.ofy().load().type(JourneyCalculationJob.class)) {
            if (journeyCalculationJob.getUser().equals(user)) result.add(journeyCalculationJob);
        }
        return result;
    }

    public List<JourneyEntity> getUserJourneys(User user) {
        return ObjectifyService.ofy().load().type(JourneyEntity.class).filter("user", user).list();
    }

    public void persist(JourneyCalculationJob journeyCalculationJob) {
        ObjectifyService.ofy().save().entity(journeyCalculationJob);
    }

    public void persist(Journey journey, User user, Log log) {
        JourneyEntity journeyEntity = new JourneyEntity();
        journeyEntity.setDateTimeZone(journey.getDateTimeZone());
        journeyEntity.setDistance(journey.getDistance());
        journeyEntity.setPointCount(journey.getPointCount());
        journeyEntity.setEnd(journey.getEnd());
        journeyEntity.setStart(journey.getStart());
        journeyEntity.setStartDatetime(journey.getStartDatetime());
        journeyEntity.setEndDateTime(journey.getEndDateTime());
        journeyEntity.setUser(user);
        journeyEntity.setMaxSpeed(journey.getMaxSpeed());
        journeyEntity.setId(SignUtils.calculateSha1Digest(user.getUserId() + journey.getStart().getName() + journey.getEnd().getName() + journey.getPointCount()));
        journeyEntity.setLog(log);
        persist(journeyEntity);
    }

    public void persist(JourneyEntity journeyEntity) {
        ObjectifyService.ofy().save().entity(journeyEntity);
    }

    public Collection<Pair<LogRecording, Log>> getPendingLogRecordings(Optional<JourneyCalculationJob> journeyCalculationJob, User user) {
        Collection<Pair<LogRecording, Log>> result = Lists.newArrayList();
        for (Log log : ObjectifyService.ofy().load().type(Log.class).filter("user", user).list()) {
            LogStatistics logStatistics = logBusiness.getLogStatistics(log);
            if (journeyCalculationJob.isPresent()) {
                if (logStatistics != null && logStatistics.getEnd().isAfter(journeyCalculationJob.get().getStartDateTime().minusHours(12))) {
                    result.add(getLogRecording(log, logStatistics));
                }
            } else if (logStatistics != null) result.add(getLogRecording(log, logStatistics));
        }
        return result;
    }

    public Collection<Pair<LogRecording, Log>> getPendingLogRecordings(User user) {
        return getPendingLogRecordings(getLastJob(user), user);
    }

    private Pair<LogRecording, Log> getLogRecording(Log log, LogStatistics logStatistics) {
        LogRecording result = new LogRecording();
        result.setDateTimeZone(log.getTimeZoneId());
        result.setName(log.getName());
        result.setDistance(logStatistics.getDistance());
        result.setPointCount(logStatistics.getPointCount());
        result.setRecords(Lists.newArrayList(Collections2.transform(log.getTrackPoints(), new Function<TrackPoint, LogRecording.LogRecord>() {
            @Override
            public LogRecording.LogRecord apply(TrackPoint trackPoint) {
                LogRecording.LogRecord result = new LogRecording.LogRecord();
                result.setDateTime(trackPoint.getPointDate());
                result.setLatitude(trackPoint.getLatitude());
                result.setLongitude(trackPoint.getLongitude());
                result.setAltitude(trackPoint.getAltitude());

                return result;
            }
        })));
        return Pair.with(result, log);
    }

}
