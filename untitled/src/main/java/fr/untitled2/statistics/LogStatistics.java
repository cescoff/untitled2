package fr.untitled2.statistics;

import org.javatuples.Pair;
import org.joda.time.Period;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 2:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class LogStatistics {

    private Period duration;

    private Pair<Double, Double> minMaxLatitude;

    private Pair<Double, Double> minMaxLongitude;

    private Pair<Double, Double> center;

    private Double averageSpeed;

    private Double totalDistance;

    public Period getDuration() {
        return duration;
    }

    public void setDuration(Period duration) {
        this.duration = duration;
    }

    public Pair<Double, Double> getMinMaxLatitude() {
        return minMaxLatitude;
    }

    public void setMinMaxLatitude(Pair<Double, Double> minMaxLatitude) {
        this.minMaxLatitude = minMaxLatitude;
    }

    public Pair<Double, Double> getMinMaxLongitude() {
        return minMaxLongitude;
    }

    public void setMinMaxLongitude(Pair<Double, Double> minMaxLongitude) {
        this.minMaxLongitude = minMaxLongitude;
    }

    public Pair<Double, Double> getCenter() {
        return center;
    }

    public void setCenter(Pair<Double, Double> center) {
        this.center = center;
    }

    public Double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(Double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }
}
