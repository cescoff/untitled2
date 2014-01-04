package fr.untitled2.common.entities;

import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 17/11/13
 * Time: 18:53
 * To change this template use File | Settings | File Templates.
 */
public class Journey {

    private String dateTimeZone;

    private LocalDateTime startDatetime;

    private LocalDateTime endDateTime;

    private int pointCount;

    private double distance;

    private KnownLocation start;

    private KnownLocation end;

    private double maxSpeed;

    public String getDateTimeZone() {
        return dateTimeZone;
    }

    public void setDateTimeZone(String dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }

    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(LocalDateTime startDatetime) {
        this.startDatetime = startDatetime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public KnownLocation getStart() {
        return start;
    }

    public void setStart(KnownLocation start) {
        this.start = start;
    }

    public KnownLocation getEnd() {
        return end;
    }

    public void setEnd(KnownLocation end) {
        this.end = end;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
