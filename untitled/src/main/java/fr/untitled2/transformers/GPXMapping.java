package fr.untitled2.transformers;

import com.google.common.collect.Lists;
import fr.untitled2.common.entities.jaxb.DateTimeAdapter;
import fr.untitled2.common.entities.jaxb.LocalDateTimeAdapter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/4/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "gpx") @XmlAccessorType(XmlAccessType.FIELD)
public class GPXMapping {

    @XmlElement(name = "trk")
    private List<Trk> tracks = Lists.newArrayList();

    public List<Trk> getTracks() {
        return tracks;
    }

    public void setTracks(List<Trk> tracks) {
        this.tracks = tracks;
    }

    @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
    public static class Trk {

        @XmlElement
        private String name;

        @XmlElement
        private String desc;

        @XmlElement
        private String extensions;

        @XmlElement(name = "trkseg")
        private List<TrkSeg> trackSegments = Lists.newArrayList();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public List<TrkSeg> getTrackSegments() {
            return trackSegments;
        }

        public void setTrackSegments(List<TrkSeg> trackSegments) {
            this.trackSegments = trackSegments;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TrkSeg {

        @XmlElement(name = "trkpt")
        private List<TrkPt> trackPoints = Lists.newArrayList();

        public List<TrkPt> getTrackPoints() {
            return trackPoints;
        }

        public void setTrackPoints(List<TrkPt> trackPoints) {
            this.trackPoints = trackPoints;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TrkPt {

        @XmlAttribute(name = "lat")
        private Double latitude;

        @XmlAttribute(name = "lon")
        private Double longitude;

        @XmlElement(name = "ele")
        private Double altitude;

        @XmlElement(name = "time") @XmlJavaTypeAdapter(DateTimeAdapter.class)
        private DateTime dateTime;

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

        public DateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(DateTime dateTime) {
            this.dateTime = dateTime;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}