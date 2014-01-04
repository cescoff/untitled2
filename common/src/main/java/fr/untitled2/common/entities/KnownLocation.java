package fr.untitled2.common.entities;

import com.google.common.collect.Sets;
import org.javatuples.Triplet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 16/11/13
 * Time: 16:32
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class KnownLocation {

    private static final double default_detection_radius = 100;

    @XmlElement
    private String name;

    @XmlElement
    private double latitude;

    @XmlElement
    private double longitude;

    @XmlElement
    private double altitude;

    @XmlElement
    private double detectionRadius;

    private Collection<String> wifiSSIDs = Sets.newHashSet();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getDetectionRadius() {
        if (detectionRadius <= 0) return default_detection_radius;
        return detectionRadius;
    }

    public void setDetectionRadius(double detectionRadius) {
        this.detectionRadius = detectionRadius;
    }

    public Collection<String> getWifiSSIDs() {
        return wifiSSIDs;
    }

    public void setWifiSSIDs(Collection<String> wifiSSIDs) {
        this.wifiSSIDs = wifiSSIDs;
    }

    public Triplet<Double, Double, Double> getLatitudeLongitude() {
        return Triplet.with(latitude, longitude, 0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KnownLocation that = (KnownLocation) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "KnownLocation{" +
                "name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
