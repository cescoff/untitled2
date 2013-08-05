package fr.untitled2.entities;

import fr.untitled2.common.entities.jaxb.DateTimeAdapter;
import fr.untitled2.common.entities.jaxb.LocalDateTimeAdapter;
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
    private Double altitude;

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

}
