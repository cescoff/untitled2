package fr.untitled2.utils;

import org.javatuples.Pair;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/9/13
 * Time: 3:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class DistanceUtils {

    private static int earth_radius_in_meter = 6371 * 1000;

    public static int getDistance(Pair<Double, Double> firstPoint, Pair<Double, Double> secondPoint) {
        Double dLat = deg2Rad(firstPoint.getValue0() - secondPoint.getValue0());
        Double dLon = deg2Rad(firstPoint.getValue1() - secondPoint.getValue1());

        Double angle = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2Rad(firstPoint.getValue0())) * Math.cos(deg2Rad(secondPoint.getValue0())) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        Double c = 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1 - angle));

        return new Double(c * earth_radius_in_meter).intValue();
    }

    private static Double deg2Rad(Double degrees) {
        return degrees * (Math.PI / 180);
    }

}
