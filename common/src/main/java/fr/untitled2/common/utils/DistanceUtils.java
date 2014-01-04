package fr.untitled2.common.utils;

import com.google.common.base.Optional;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.utils.CollectionUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/9/13
 * Time: 3:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class DistanceUtils {

    private static int earth_radius_in_meter = 6371 * 1000;

    public static double getDistance(Triplet<Double, Double, Double> firstPointLatLngAlt, Triplet<Double, Double, Double> secondPointLatLngAlt) {
        double minAltitude = Math.min(firstPointLatLngAlt.getValue2(), firstPointLatLngAlt.getValue2());

        Double dLat = deg2Rad(firstPointLatLngAlt.getValue0() - secondPointLatLngAlt.getValue0());
        Double dLon = deg2Rad(firstPointLatLngAlt.getValue1() - secondPointLatLngAlt.getValue1());

        Double angle = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2Rad(firstPointLatLngAlt.getValue0())) * Math.cos(deg2Rad(secondPointLatLngAlt.getValue0())) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        Double c = 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1 - angle));

        double radius = earth_radius_in_meter;
        if (minAltitude >= 0) radius += minAltitude;
        double horizontalDistance = c * radius;
        if (minAltitude < 0) {
            return horizontalDistance;
        } else {
            return Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(Math.abs(firstPointLatLngAlt.getValue2() - firstPointLatLngAlt.getValue2()), 2));
        }
    }

    private static Double deg2Rad(Double degrees) {
        return degrees * (Math.PI / 180);
    }

    public static Optional<KnownLocation> getKnownLocation(Triplet<Double, Double, Double> latitudeLongitudeAltitude, Collection<KnownLocation> knownLocations) {
        KnownLocation result = null;
        double minDistance = -1.0;
        if (CollectionUtils.isEmpty(knownLocations)) return Optional.absent();
        for (KnownLocation knownLocation : knownLocations) {
            double distance = getDistance(Triplet.with(latitudeLongitudeAltitude.getValue0(), latitudeLongitudeAltitude.getValue1(), -1.0), Triplet.with(knownLocation.getLatitude(), knownLocation.getLongitude(), -1.0));
            if (distance < knownLocation.getDetectionRadius()) {
                if (minDistance < 0 || distance < minDistance) {
                    result = knownLocation;
                }
            }
        }
        if (result != null) return Optional.of(result);

        return Optional.absent();
    }
}
