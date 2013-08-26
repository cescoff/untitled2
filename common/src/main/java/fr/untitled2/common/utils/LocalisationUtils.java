package fr.untitled2.common.utils;

import fr.untitled2.common.entities.LogRecording;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.joda.time.LocalDateTime;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 6/27/13
 * Time: 11:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocalisationUtils {

    public static Triplet<Double, Double, String> getImagePosition(LocalDateTime localDateTime, Collection<LogRecording> logRecordings) {
        for (LogRecording logRecording : logRecordings) {
            List<LogRecording.LogRecord> sortedTrackPoints = LogRecording.DATE_ORDERING.sortedCopy(logRecording.getRecords());

            for (int index = 1; index < sortedTrackPoints.size(); index++) {
                LogRecording.LogRecord startSeg = sortedTrackPoints.get(index - 1);
                LogRecording.LogRecord endSeg = sortedTrackPoints.get(index);
                if ((localDateTime.isAfter(startSeg.getDateTime()) || localDateTime.equals(startSeg.getDateTime())) && localDateTime.isBefore(endSeg.getDateTime())) {
                    double vectorMultiplier = (localDateTime.toDateTime().getMillis() - localDateTime.toDateTime().getMillis()) / (endSeg.getDateTime().toDateTime().getMillis() - startSeg.getDateTime().toDateTime().getMillis());

                    double latitude = 0.0;
                    double longitude = 0.0;

                    if (startSeg.getLatitude() >= 0.0) {
                        latitude = Math.abs((1 - vectorMultiplier) * endSeg.getLatitude() - vectorMultiplier * startSeg.getLatitude());
                    }
                    if (startSeg.getLongitude() >= 0.0) {
                        longitude = Math.abs((1 - vectorMultiplier) * endSeg.getLongitude() - vectorMultiplier * startSeg.getLongitude());
                    }

                    if (startSeg.getLatitude() < 0.0) {
                        latitude = -1.0 * (Math.abs((1 - vectorMultiplier) * endSeg.getLatitude() - vectorMultiplier * startSeg.getLatitude()));
                    }
                    if (startSeg.getLongitude() < 0.0) {
                        longitude = - 1.0 * (Math.abs((1 - vectorMultiplier) * startSeg.getLongitude() - vectorMultiplier * endSeg.getLongitude()));
                    }

                    return new Triplet<Double, Double, String>(latitude, longitude, logRecording.getDateTimeZone());
                }
            }
        }
        LogRecording.LogRecord closestTrackPoint = null;
        String timeZone = null;
        for (LogRecording logRecording : logRecordings) {
            List<LogRecording.LogRecord> sortedTrackPoints = LogRecording.DATE_ORDERING.sortedCopy(logRecording.getRecords());
            LogRecording.LogRecord startSeg = sortedTrackPoints.get(0);
            LogRecording.LogRecord endSeg = sortedTrackPoints.get(sortedTrackPoints.size() - 1);

            if (startSeg.getDateTime().minusHours(6).isBefore(localDateTime) && startSeg.getDateTime().isAfter(localDateTime)) {
                if (closestTrackPoint == null) {
                    closestTrackPoint = startSeg;
                    timeZone = logRecording.getDateTimeZone();
                } else if (Math.abs(startSeg.getDateTime().toDateTime().getMillis() - localDateTime.toDateTime().getMillis()) < Math.abs(closestTrackPoint.getDateTime().toDateTime().getMillis() - localDateTime.toDateTime().getMillis())) {
                    closestTrackPoint = startSeg;
                    timeZone = logRecording.getDateTimeZone();
                }
            }

            if (endSeg.getDateTime().isBefore(localDateTime) && endSeg.getDateTime().plusHours(6).isAfter(localDateTime)) {
                if (closestTrackPoint == null) {
                    closestTrackPoint = endSeg;
                    timeZone = logRecording.getDateTimeZone();
                }
                else if (Math.abs(localDateTime.toDateTime().getMillis() - endSeg.getDateTime().toDateTime().getMillis()) < Math.abs(closestTrackPoint.getDateTime().toDateTime().getMillis() - localDateTime.toDateTime().getMillis())) {
                    closestTrackPoint = endSeg;
                    timeZone = logRecording.getDateTimeZone();
                }
            }
        }
        if (closestTrackPoint != null) {
            return new Triplet<Double, Double, String>(closestTrackPoint.getLatitude(), closestTrackPoint.getLongitude(), timeZone);
        }
        return null;
    }


}
