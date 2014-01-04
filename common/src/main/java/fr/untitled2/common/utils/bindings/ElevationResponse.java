package fr.untitled2.common.utils.bindings;

import com.beust.jcommander.internal.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 11/12/13
 * Time: 23:21
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "ElevationResponse") @XmlAccessorType(XmlAccessType.FIELD)
public class ElevationResponse {

    @XmlElement
    private Status status;

    @XmlElement
    private List<Result> result = Lists.newArrayList();

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Result {

        private Location location;

        private double elevation;

        private double resolution;

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public double getElevation() {
            return elevation;
        }

        public void setElevation(double elevation) {
            this.elevation = elevation;
        }

        public double getResolution() {
            return resolution;
        }

        public void setResolution(double resolution) {
            this.resolution = resolution;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Location {

            @XmlElement(name = "lat")
            private double latitude;

            @XmlElement(name = "lng")
            private double longitude;

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
        }

    }

    public static enum Status {
        OK,
        INVALID_REQUEST,
        OVER_QUERY_LIMIT,
        REQUEST_DENIED,
        UNKNOWN_ERROR
    }

}
