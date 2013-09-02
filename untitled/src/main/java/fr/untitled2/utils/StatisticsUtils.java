package fr.untitled2.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.TrackPoint;
import fr.untitled2.statistics.PictureMapStatistics;
import fr.untitled2.statistics.LogStatistics;
import org.javatuples.Pair;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 3:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatisticsUtils {

    public static Ordering<TrackPoint> TRACK_POINT_SORT = Ordering.natural().onResultOf(new Function<TrackPoint, LocalDateTime>() {
        @Override
        public LocalDateTime apply(TrackPoint trackPoint) {
            return trackPoint.getPointDate();
        }
    });

    public static Ordering<Image> IMAGE_SORT  = Ordering.natural().onResultOf(new Function<Image, LocalDateTime>() {
        @Override
        public LocalDateTime apply(Image o) {
            return o.getDateTaken();
        }
    });

    public static Predicate<Image> IMAGE_READY_FILTER = new Predicate<Image>() {
        @Override
        public boolean apply(Image image) {
            return image.isReady();
        }
    };

    public static LogStatistics getLogStatistics(Log aLog) {
        LogStatistics result = new LogStatistics();

        List<TrackPoint> sortedTrackPoints = TRACK_POINT_SORT.sortedCopy(aLog.getTrackPoints());

        double totalDistance = 0;

        if (sortedTrackPoints.size() > 1) {
            Double minLat = null;
            Double maxLat = null;
            Double minLong = null;
            Double maxLong = null;
            for (int index = 1; index < sortedTrackPoints.size(); index++) {
                TrackPoint startSeg = sortedTrackPoints.get(index - 1);
                TrackPoint endSeg = sortedTrackPoints.get(index);
                Double distance = Math.sqrt(Math.pow(endSeg.getLatitude() - startSeg.getLatitude(), 2) + Math.pow(endSeg.getLongitude() - startSeg.getLongitude(), 2));
                totalDistance+=distance;

                if (minLat == null) minLat = startSeg.getLatitude();
                if (minLong == null) minLong = startSeg.getLongitude();
                if (maxLat == null) maxLat = startSeg.getLatitude();
                if (maxLong == null) maxLong = startSeg.getLongitude();

                if (startSeg.getLatitude() < minLat)  minLat = startSeg.getLatitude();
                if (startSeg.getLatitude() > maxLat) maxLat = startSeg.getLatitude();

                if (startSeg.getLongitude() < minLong) minLong = startSeg.getLongitude();
                if (startSeg.getLongitude() > maxLong) maxLong = startSeg.getLongitude();

                if (endSeg.getLatitude() < minLat)  minLat = endSeg.getLatitude();
                if (endSeg.getLatitude() > maxLat) maxLat = endSeg.getLatitude();

                if (endSeg.getLongitude() < minLong) minLong = endSeg.getLongitude();
                if (endSeg.getLongitude() > maxLong) maxLong = endSeg.getLongitude();

            }

            result.setDuration(new Period(sortedTrackPoints.get(sortedTrackPoints.size() - 1).getPointDate(), sortedTrackPoints.get(0).getPointDate()));
            result.setMinMaxLatitude(Pair.with(minLat, maxLat));
            result.setMinMaxLongitude(Pair.with(minLong, maxLong));
            result.setTotalDistance(totalDistance);
            result.setCenter(Pair.with((maxLat+minLat)/ 2, (maxLong + minLong) / 2));
        } else if (sortedTrackPoints.size() > 0) {
            TrackPoint onlyPoint = sortedTrackPoints.get(0);
            result.setCenter(Pair.with(onlyPoint.getLatitude(), onlyPoint.getLongitude()));
            result.setTotalDistance(0.0);
            result.setMinMaxLongitude(Pair.with(onlyPoint.getLatitude(), onlyPoint.getLongitude()));
            result.setMinMaxLatitude(Pair.with(onlyPoint.getLatitude(), onlyPoint.getLongitude()));
            result.setDuration(new Period());
            result.setAverageSpeed(0.0);
        }


        return result;
    }

    public static Log sortTrackPoints(Log aLog) {
        aLog.setTrackPoints(TRACK_POINT_SORT.sortedCopy(aLog.getTrackPoints()));
        return aLog;
    }

    public static PictureMapStatistics getPictureMapStatistics(List<Image> images) {
        if (CollectionUtils.isEmpty(images)) return null;
        PictureMapStatistics result = new PictureMapStatistics();

        List<Image> sortedImages = IMAGE_SORT.sortedCopy(images);

        Double minLat = null;
        Double maxLat = null;
        Double minLong = null;
        Double maxLong = null;
        for (int index = 1; index < sortedImages.size(); index++) {
            Image startSeg = sortedImages.get(index - 1);
            Image endSeg = sortedImages.get(index);

            if (startSeg.getLatitude() != null && startSeg.getLongitude() != null && endSeg.getLatitude() != null && endSeg.getLongitude() != null) {
                if (minLat == null) minLat = startSeg.getLatitude();
                if (minLong == null) minLong = startSeg.getLongitude();
                if (maxLat == null) maxLat = startSeg.getLatitude();
                if (maxLong == null) maxLong = startSeg.getLongitude();

                if (startSeg.getLatitude() < minLat)  minLat = startSeg.getLatitude();
                if (startSeg.getLatitude() > maxLat) maxLat = startSeg.getLatitude();

                if (startSeg.getLongitude() < minLong) minLong = startSeg.getLongitude();
                if (startSeg.getLongitude() > maxLong) maxLong = startSeg.getLongitude();

                if (endSeg.getLatitude() < minLat)  minLat = endSeg.getLatitude();
                if (endSeg.getLatitude() > maxLat) maxLat = endSeg.getLatitude();

                if (endSeg.getLongitude() < minLong) minLong = endSeg.getLongitude();
                if (endSeg.getLongitude() > maxLong) maxLong = endSeg.getLongitude();
            }
        }

        if (maxLat != null && minLat != null && maxLong != null && minLong != null) {
            result.setMapCenter(Pair.with((maxLat + minLat) / 2, (maxLong + minLong) / 2));
        }

        return result;
    }

}
