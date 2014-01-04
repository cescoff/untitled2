package fr.untitled2.android.sqlilite;

import fr.untitled2.common.entities.KnownLocation;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 17/11/13
 * Time: 03:20
 * To change this template use File | Settings | File Templates.
 */
public class KnownLocationWithDatetime {

    private String id;

    private LocalDateTime pointDate;

    private double distance;

    private KnownLocation knownLocation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getPointDate() {
        return pointDate;
    }

    public void setPointDate(LocalDateTime pointDate) {
        this.pointDate = pointDate;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public KnownLocation getKnownLocation() {
        return knownLocation;
    }

    public void setKnownLocation(KnownLocation knownLocation) {
        this.knownLocation = knownLocation;
    }
}
