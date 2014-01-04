package fr.untitled2.servlet.process;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.LogBusiness;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.utils.GeoLocalisationUtils;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.LogPersistenceJob;
import fr.untitled2.entities.TrackPoint;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.common.utils.DistanceUtils;
import org.apache.commons.lang.StringUtils;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 19/08/13
 * Time: 12:25
 * To change this template use File | Settings | File Templates.
 */
public class LogPersistenceServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(LogPersistenceServlet.class);

    private static final LogBusiness logBusiness = new LogBusiness();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String logPersistenceJobKey = req.getParameter(ServletConstants.log_peristence_job_key);
        if (StringUtils.isEmpty(logPersistenceJobKey)) {
            logger.error("Clef de persitence job vide, non supporté");
            return;
        }
        logger.info("Voici la clef de log persistence '" + logPersistenceJobKey + "'");
        try {

            LogPersistenceJob logPersistenceJob = ObjectifyService.ofy().load().key(Key.create(LogPersistenceJob.class, logPersistenceJobKey)).get();

            if (logPersistenceJob != null) {
                logger.info("Chargement du user '" + logPersistenceJob.getUserKey() + "'");

                User user = ObjectifyService.ofy().load().key(logPersistenceJob.getUserKey()).get();

                logger.info("User chargé : '" + user + "'");

                LogRecording logRecording = logPersistenceJob.getLogRecording();

                if (logRecording.getRecords() != null) logger.info("LogRecording Chargé : " + logRecording.getRecords().size());
                else logger.info("LogRecording Chargé : " + logRecording.getRecords());

                Log log = new Log();
                log.setTimeZoneId(logRecording.getDateTimeZone());
                log.setName(logRecording.getName());
                List<LogRecording.LogRecord> logRecords = LogRecording.DATE_ORDERING.sortedCopy(logRecording.getRecords());
                if (CollectionUtils.isNotEmpty(logRecording.getRecords())) {
                    log.setStartTime(logRecords.get(0).getDateTime());
                    log.setEndTime(logRecords.get(logRecords.size() - 1).getDateTime());
                }

                log.setValidated(true);

                for (LogRecording.LogRecord logRecord : logRecording.getRecords()) {
                    TrackPoint trackPoint = new TrackPoint();
                    trackPoint.setPointDate(logRecord.getDateTime());
                    trackPoint.setLatitude(logRecord.getLatitude());
                    trackPoint.setLongitude(logRecord.getLongitude());
                    trackPoint.setAltitude(logRecord.getAltitude());

                    Optional<KnownLocation> knownLocationOptional = getPointKnownLocation(logRecord, user);
                    if (knownLocationOptional.isPresent()) {
                        trackPoint.setKnownLocation(knownLocationOptional.get());
                    }
                    log.getTrackPoints().add(trackPoint);
                }
/*
                try {
                    addElevationToLog(log, user.getKnownLocations());
                } catch (Throwable t) {
                    logger.error("GoogleElevationAPIERROR:" + t.getMessage(), t);
                }
*/
                logBusiness.persistLog(user, log);
                ObjectifyService.ofy().delete().entity(logPersistenceJob);
            }
        } catch (Throwable t) {
            logger.error("Un erreur s'est produite lors du traitement du fichier '" + logPersistenceJobKey + "'", t);
            Queue queue = QueueFactory.getQueue(ServletConstants.log_persistence_queue_name);

            TaskOptions taskOptions = TaskOptions.Builder.withUrl("/logPersistence").param(ServletConstants.log_peristence_job_key, logPersistenceJobKey);
            queue.add(taskOptions);
        }
    }

    private void addElevationToLog(Log log, final Collection<KnownLocation> knownLocations) {
        List<Triplet<LocalDateTime, Double, Double>> trackPoints = Lists.newArrayList(Iterables.transform(Ordering.natural().onResultOf(new Function<TrackPoint, LocalDateTime>() {
            @Override
            public LocalDateTime apply(TrackPoint trackPoint) {
                return trackPoint.getPointDate();
            }
        }).sortedCopy(log.getTrackPoints()), new Function<TrackPoint, Triplet<LocalDateTime, Double, Double>>() {
            @Override
            public Triplet<LocalDateTime, Double, Double> apply(TrackPoint trackPoint) {
                return Triplet.with(trackPoint.getPointDate(), trackPoint.getLatitude(), trackPoint.getLongitude());
            }
        }));

        log.setTrackPoints(Lists.newArrayList(Iterables.transform(GeoLocalisationUtils.getAltitudes(trackPoints), new Function<Quartet<LocalDateTime, Double, Double, Double>, TrackPoint>() {
            @Override
            public TrackPoint apply(Quartet<LocalDateTime, Double, Double, Double> objects) {
                TrackPoint trackPoint = new TrackPoint();
                trackPoint.setPointDate(objects.getValue0());
                trackPoint.setLatitude(objects.getValue1());
                trackPoint.setLongitude(objects.getValue2());
                trackPoint.setAltitude(objects.getValue3());
                Optional<KnownLocation> knownLocationOptional = DistanceUtils.getKnownLocation(trackPoint.getLatitudeAndLongitude(), knownLocations);
                if (knownLocationOptional.isPresent()) {
                    trackPoint.setKnownLocation(knownLocationOptional.get());
                }
                return trackPoint;
            }
        })));
    }

    private Optional<KnownLocation> getPointKnownLocation(LogRecording.LogRecord logRecord, User user) {
        if (CollectionUtils.isEmpty(user.getKnownLocations())) return Optional.absent();
        return DistanceUtils.getKnownLocation(logRecord.getLatitudeAndLongitude(), user.getKnownLocations());
    }

}
