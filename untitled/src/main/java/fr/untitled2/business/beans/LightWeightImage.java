package fr.untitled2.business.beans;

import fr.untitled2.entities.Image;
import fr.untitled2.servlet.ImageDisplayMode;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/15/13
 * Time: 5:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class LightWeightImage implements Serializable {

    private Double latitude;

    private Double longitude;

    private int width;

    private int height;

    private LocalDateTime dateTaken;

    private String imageKey;

    public LightWeightImage(double latitude, double longitude, LocalDateTime dateTaken, String imageKey) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTaken = dateTaken;
        this.imageKey = imageKey;
    }

    public LightWeightImage(Image image) {
        this.imageKey = image.getImageKey();
        this.dateTaken = image.getDateTaken();
        this.latitude = image.getLatitude();
        this.longitude = image.getLongitude();
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public LocalDateTime getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(LocalDateTime dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public String getLowResolutionImageUrl() {
        return new StringBuilder().append("/ihm/images/view?imageKey=").append(imageKey).append("&displayMode=").append(ImageDisplayMode.low.getCode()).toString();
    }

    public String getSquareLowResolutionImageUrl() {
        return new StringBuilder().append("/ihm/images/view?imageKey=").append(imageKey).append("&displayMode=").append(ImageDisplayMode.lowSquare.getCode()).toString();
    }

    public String getMediumResolutionImageUrl() {
        return new StringBuilder().append("/ihm/images/view?imageKey=").append(imageKey).append("&displayMode=").append(ImageDisplayMode.medium.getCode()).toString();
    }

    public String getHighResolutionImageUrl() {
        return new StringBuilder().append("/ihm/images/view?imageKey=").append(imageKey).append("&displayMode=").append(ImageDisplayMode.high.getCode()).toString();
    }

    public String getOriginalImageUrl() {
        return new StringBuilder().append("/ihm/images/view?imageKey=").append(imageKey).append("&displayMode=").append(ImageDisplayMode.orginal.getCode()).toString();
    }

    public Marker toMarker(DateTimeFormatter dateTimeFormatter) {
        String urlPrefix = new StringBuilder().append("/ihm/images/view?imageKey=").append(imageKey).append("&displayMode=").toString();
        return new Marker(latitude + "", longitude + "", dateTimeFormatter.print(dateTaken), getLowResolutionImageUrl(), getSquareLowResolutionImageUrl(), getMediumResolutionImageUrl());
    }

}
