package fr.untitled2.android.entities;

import org.joda.time.DateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 8/29/13
 * Time: 10:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class UploadProcessStatus {

    private Long id;

    private DateTime lastUploadDate;

    private double lastPointLatitude;

    private double lastPointLongitude;

    public UploadProcessStatus(Long id, DateTime lastUploadDate, double lastPointLatitude, double lastPointLongitude) {
        this.id = id;
        this.lastUploadDate = lastUploadDate;
        this.lastPointLatitude = lastPointLatitude;
        this.lastPointLongitude = lastPointLongitude;
    }

    public UploadProcessStatus(DateTime lastUploadDate, double lastPointLatitude, double lastPointLongitude) {
        this.lastUploadDate = lastUploadDate;
        this.lastPointLatitude = lastPointLatitude;
        this.lastPointLongitude = lastPointLongitude;
    }

    public Long getId() {
        return id;
    }

    public DateTime getLastUploadDate() {
        return lastUploadDate;
    }

    public double getLastPointLatitude() {
        return lastPointLatitude;
    }

    public double getLastPointLongitude() {
        return lastPointLongitude;
    }
}
