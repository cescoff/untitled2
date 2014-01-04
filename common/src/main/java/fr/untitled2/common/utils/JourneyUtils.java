package fr.untitled2.common.utils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import fr.untitled2.common.entities.Journey;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.utils.CollectionUtils;
import org.javatuples.Pair;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 17/11/13
 * Time: 18:58
 * To change this template use File | Settings | File Templates.
 */
public class JourneyUtils {

//    private static final Logger logger = LoggerFactory.getLogger(JourneyUtils.class);

    public static Collection<Journey> getJourneys(LogRecording logRecording, Collection<KnownLocation> knownLocations) {
        List<LogRecording.LogRecord> orderedRecords = Ordering.natural().onResultOf(new Function<LogRecording.LogRecord, LocalDateTime>() {
            @Override
            public LocalDateTime apply(LogRecording.LogRecord logRecord) {
                return logRecord.getDateTime();
            }
        }).sortedCopy(logRecording.getRecords());

        Collection<Journey> candidates = getJourneyCandidates(orderedRecords, knownLocations);
//        logger.info("Found " + candidates.size() + " journey candidates for LogRecording '" + logRecording.getName() + "'");
        Collection<Journey> result = Lists.newArrayList();
        for (Journey candidate : candidates) {
            boolean journeyStarted = false;

            LogRecording.LogRecord firstPointAfterStart = null;
            LogRecording.LogRecord lastPointBeforeEnd = null;
            LogRecording.LogRecord previousPoint = null;
            candidate.setDistance(0);
            double maxSpeed = 0.0;
            double previousSpeed = 0.0;
            try {
                for (LogRecording.LogRecord orderedRecord : orderedRecords) {
                    if (orderedRecord.getDateTime().equals(candidate.getStartDatetime())) {
//                        logger.info("Journey '" + candidate.getStart().getName() + "->" + candidate.getEnd().getName() + "' is starting");
                        journeyStarted = true;
                    } else if (orderedRecord.getDateTime().equals(candidate.getEndDateTime())) {
                        if (firstPointAfterStart != null && lastPointBeforeEnd != null && firstPointAfterStart.getDateTime() != null && lastPointBeforeEnd.getDateTime() != null) {
//                            logger.info("Journey '" + candidate.getStart().getName() + "->" + candidate.getEnd().getName() + "' is ended");
                            double averageSpeed = candidate.getDistance() / new Period(firstPointAfterStart.getDateTime(), lastPointBeforeEnd.getDateTime()).toStandardSeconds().getSeconds();
//                            logger.info("Journey '" + candidate.getStart().getName() + "->" + candidate.getEnd().getName() + "' averages speed is " + averageSpeed + "m/s (" + (averageSpeed * 3600) / 1000 + "km/h)");

                            double beforeStartDistance = DistanceUtils.getDistance(candidate.getStart().getLatitudeLongitude(), firstPointAfterStart.getLatitudeAndLongitude());
                            double beforeEndDistance = DistanceUtils.getDistance(candidate.getEnd().getLatitudeLongitude(), lastPointBeforeEnd.getLatitudeAndLongitude());

                            double deltaTBeforeStart = beforeStartDistance / averageSpeed;
                            double deltaTBeforeEnd = beforeEndDistance / averageSpeed;
                            candidate.setStartDatetime(firstPointAfterStart.getDateTime().minusSeconds(new Double(deltaTBeforeStart).intValue()));
                            candidate.setEndDateTime(lastPointBeforeEnd.getDateTime().plusSeconds(new Double(deltaTBeforeEnd).intValue()));

                            candidate.setDistance(candidate.getDistance() + DistanceUtils.getDistance(firstPointAfterStart.getLatitudeAndLongitude(), candidate.getStart().getLatitudeLongitude()));
                            candidate.setDistance(candidate.getDistance() + DistanceUtils.getDistance(lastPointBeforeEnd.getLatitudeAndLongitude(), candidate.getEnd().getLatitudeLongitude()));

                            candidate.setDateTimeZone(logRecording.getDateTimeZone());
                            candidate.setPointCount(candidate.getPointCount() + 2);
                            candidate.setMaxSpeed(maxSpeed);
                            result.add(candidate);
//                            logger.info("Distance from '" + candidate.getStart().getName() + "'->'" + candidate.getEnd().getName() + "' : " + candidate.getDistance() / 1000 + " (" + candidate.getPointCount() + ") with max speed : " + (maxSpeed * 3600) / 1000 + "km/h at '" + candidate.getStartDatetime() + "'");
                        }/* else {
                            logger.error("Empty journey detected");
                        }*/
                        journeyStarted = false;

                    } else if (journeyStarted) {
                        Optional<KnownLocation> knownLocationOptional = DistanceUtils.getKnownLocation(orderedRecord.getLatitudeAndLongitude(), knownLocations);
                        if (!knownLocationOptional.isPresent()) {
                            if (firstPointAfterStart == null) {
                                firstPointAfterStart = orderedRecord;
                            }
                            lastPointBeforeEnd = orderedRecord;
                            if (previousPoint == null) {
                                previousPoint = orderedRecord;
                            } else {
                                double distance = DistanceUtils.getDistance(previousPoint.getLatitudeAndLongitude(), orderedRecord.getLatitudeAndLongitude());
                                candidate.setDistance(candidate.getDistance() + distance);

                                int totalDuration = new Period(firstPointAfterStart.getDateTime(), orderedRecord.getDateTime()).toStandardSeconds().getSeconds();
                                double avgSpeed = 0.0;
                                if (totalDuration > 0) avgSpeed = candidate.getDistance() / totalDuration;

                                int deltaT = new Period(previousPoint.getDateTime(), orderedRecord.getDateTime()).toStandardSeconds().getSeconds();
                                if (deltaT > 0) {
                                    double speed = distance / deltaT;
                                    if (speed > maxSpeed && previousSpeed > 0 && avgSpeed > 0 && Math.abs(speed - previousSpeed) < avgSpeed) maxSpeed = speed;
                                    previousSpeed = speed;
                                }
                                previousPoint = orderedRecord;
                            }
                            candidate.setPointCount(candidate.getPointCount() + 1);
                        }
                    }
                }
            } catch (Throwable t) {
                //logger.error("An error has occured while calculating statistics on journey '" + candidate.getStart().getName() + "'->'" + candidate.getEnd().getName() + "'", t);
                throw new IllegalStateException("Uncaught error", t);
            }
        }
        return result;
    }

    private static Collection<Journey> getJourneyCandidates(List<LogRecording.LogRecord> logRecords, Collection<KnownLocation> knownLocations) {
        Collection<Journey> result = Lists.newArrayList();

        KnownLocation start = null;
        LocalDateTime startDateTime = null;
        for (LogRecording.LogRecord orderedRecord : logRecords) {
            Optional<KnownLocation> knownLocation = DistanceUtils.getKnownLocation(orderedRecord.getLatitudeAndLongitude(), knownLocations);
            if (knownLocation.isPresent()) {

                if (start == null) {
                    start = knownLocation.get();
                    startDateTime = orderedRecord.getDateTime();
                } else {
                    Journey journey = new Journey();
                    journey.setStart(start);
                    journey.setStartDatetime(startDateTime);
                    journey.setEnd(knownLocation.get());
                    journey.setEndDateTime(orderedRecord.getDateTime());
                    Period duration = new Period(startDateTime, orderedRecord.getDateTime());
                    if (!journey.getStart().getName().equals(journey.getEnd().getName()) || (journey.getStart().equals(journey.getEnd().getName()) && duration.toStandardMinutes().getMinutes() >= 15)) {
                        result.add(journey);
                        start = null;
                    } else {
                        start = knownLocation.get();
                        startDateTime = orderedRecord.getDateTime();
                    }
                }
            }
        }

        return result;
    }


}
