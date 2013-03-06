package fr.untitled2.utils;

import fr.untitled2.entities.Image;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.TrackPoint;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/7/13
 * Time: 1:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeoLocalisationUtils {

    private static Logger logger = LoggerFactory.getLogger(GeoLocalisationUtils.class);

    public static void updateImagesLocalisation(final Collection<Image> images, Collection<Log> logs) {
        if (CollectionUtils.isEmpty(logs)) logger.info("Aucun trip selectionne pour la localisation de l'image");
        if (CollectionUtils.isEmpty(images)) logger.info("Aucune image a localiser");
        for (Image image : images) {
            updateImageLocalisation(image, logs);
        }

    }

    private static void updateImageLocalisation(final Image image, Collection<Log> logs) {
        logger.info("Localisation de l'image : " + image.getDateTaken());
        for (Log log : logs) {
            List<TrackPoint> sortedTrackPoints = StatisticsUtils.TRACK_POINT_SORT.sortedCopy(log.getTrackPoints());

            for (int index = 1; index < sortedTrackPoints.size(); index++) {
                TrackPoint startSeg = sortedTrackPoints.get(index - 1);
                TrackPoint endSeg = sortedTrackPoints.get(index);
                if ((image.getDateTaken().isAfter(startSeg.getPointDate()) || image.getDateTaken().equals(startSeg.getPointDate())) && image.getDateTaken().isBefore(endSeg.getPointDate())) {
                    logger.info("Une date matche : " + startSeg.getPointDate() + "->" + endSeg.getPointDate() + " (" + image.getDateTaken() + ")");

                    double vectorMultiplier = (image.getDateTaken().toDateTime().getMillis() - startSeg.getPointDate().toDateTime().getMillis()) / (endSeg.getPointDate().toDateTime().getMillis() - startSeg.getPointDate().toDateTime().getMillis());

                    image.setLatitude(Math.abs((1 - vectorMultiplier) * endSeg.getLatitude() - vectorMultiplier * startSeg.getLatitude()));
                    image.setLongitude(Math.abs((1 - vectorMultiplier) * endSeg.getLongitude() - vectorMultiplier * startSeg.getLongitude()));
                    image.setTimeZoneId(log.getTimeZoneId());
                    return;
                }
            }
        }
        logger.info("Aucune position exacte a ete trouvee, tentative en mode degrade");
        TrackPoint closestTrackPoint = null;
        for (Log log : logs) {
            List<TrackPoint> sortedTrackPoints = StatisticsUtils.TRACK_POINT_SORT.sortedCopy(log.getTrackPoints());
            TrackPoint startSeg = sortedTrackPoints.get(0);
            TrackPoint endSeg = sortedTrackPoints.get(sortedTrackPoints.size() - 1);

            if (startSeg.getPointDate().minusHours(6).isBefore(image.getDateTaken()) && startSeg.getPointDate().isAfter(image.getDateTaken())) {
                if (closestTrackPoint == null) {
                    logger.info("[START] TrackPoint plus proche trouve : " + startSeg.getPointDate() + " (nouveau)");
                    closestTrackPoint = startSeg;
                    image.setTimeZoneId(log.getTimeZoneId());
                } else if (Math.abs(startSeg.getPointDate().toDateTime().getMillis() - image.getDateTaken().toDateTime().getMillis()) < Math.abs(closestTrackPoint.getPointDate().toDateTime().getMillis() - image.getDateTaken().toDateTime().getMillis())) {
                    logger.info("[START] TrackPoint plus proche trouve : " + startSeg.getPointDate());
                    closestTrackPoint = startSeg;
                    image.setTimeZoneId(log.getTimeZoneId());
                }
            }

            if (endSeg.getPointDate().isBefore(image.getDateTaken()) && endSeg.getPointDate().plusHours(6).isAfter(image.getDateTaken())) {
                if (closestTrackPoint == null) {
                    logger.info("[END] TrackPoint plus proche trouve : " + startSeg.getPointDate() + " (nouveau)");
                    closestTrackPoint = endSeg;
                    image.setTimeZoneId(log.getTimeZoneId());
                }
                else if (Math.abs(image.getDateTaken().toDateTime().getMillis() - endSeg.getPointDate().toDateTime().getMillis()) < Math.abs(closestTrackPoint.getPointDate().toDateTime().getMillis() - image.getDateTaken().toDateTime().getMillis())) {
                    logger.info("[END] TrackPoint plus proche trouve : " + endSeg.getPointDate());
                    image.setTimeZoneId(log.getTimeZoneId());
                    closestTrackPoint = endSeg;
                }
            }
        }
        if (closestTrackPoint != null) {
            image.setLatitude(closestTrackPoint.getLatitude());
            image.setLongitude(closestTrackPoint.getLongitude());
        }
    }

}
