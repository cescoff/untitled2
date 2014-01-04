package fr.untitled2.entities;

import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.jaxb.DateTimeAdapter;
import fr.untitled2.common.entities.jaxb.LocalDateTimeAdapter;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/28/13
 * Time: 5:50 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TrackPoint {

    @XmlElement
    private Double latitude;

    @XmlElement
    private Double longitude;

    @XmlElement
    private Double altitude = -1.0;

    @XmlElement
    private KnownLocation knownLocation;

    @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime pointDate;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public LocalDateTime getPointDate() {
        return pointDate;
    }

    public void setPointDate(LocalDateTime pointDate) {
        this.pointDate = pointDate;
    }

    public KnownLocation getKnownLocation() {
        return knownLocation;
    }

    public void setKnownLocation(KnownLocation knownLocation) {
        this.knownLocation = knownLocation;
    }

    public Triplet<Double, Double, Double> getLatitudeAndLongitude() {
        return Triplet.with(latitude, longitude, altitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackPoint that = (TrackPoint) o;

        if (altitude != null ? !altitude.equals(that.altitude) : that.altitude != null) return false;
        if (knownLocation != null ? !knownLocation.equals(that.knownLocation) : that.knownLocation != null)
            return false;
        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null) return false;
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) return false;
        if (pointDate != null ? !pointDate.equals(that.pointDate) : that.pointDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = latitude != null ? latitude.hashCode() : 0;
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (altitude != null ? altitude.hashCode() : 0);
        result = 31 * result + (knownLocation != null ? knownLocation.hashCode() : 0);
        result = 31 * result + (pointDate != null ? pointDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TrackPoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", pointDate=" + pointDate +
                '}';
    }
}
