package fr.untitled2.business.beans;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/15/13
 * Time: 5:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class Marker {

    private String latitude;

    private String longitude;

    private String title;

    private String lowResolutionImageUrl;

    private String squareLowResolutionImageUrl;

    private String highResolutionImageUrl;

    public Marker() {
    }

    public Marker(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Marker(String latitude, String longitude, String title, String lowResolutionImageUrl, String squareLowResolutionImageUrl, String highResolutionImageUrl) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.lowResolutionImageUrl = lowResolutionImageUrl;
        this.squareLowResolutionImageUrl = squareLowResolutionImageUrl;
        this.highResolutionImageUrl = highResolutionImageUrl;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLowResolutionImageUrl() {
        return lowResolutionImageUrl;
    }

    public void setLowResolutionImageUrl(String lowResolutionImageUrl) {
        this.lowResolutionImageUrl = lowResolutionImageUrl;
    }

    public String getSquareLowResolutionImageUrl() {
        return squareLowResolutionImageUrl;
    }

    public void setSquareLowResolutionImageUrl(String squareLowResolutionImageUrl) {
        this.squareLowResolutionImageUrl = squareLowResolutionImageUrl;
    }

    public String getHighResolutionImageUrl() {
        return highResolutionImageUrl;
    }

    public void setHighResolutionImageUrl(String highResolutionImageUrl) {
        this.highResolutionImageUrl = highResolutionImageUrl;
    }
}
