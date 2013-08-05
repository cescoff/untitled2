package fr.untitled2.common.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/18/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserPreferences {

    @XmlElement
    private String dateFormat;

    @XmlElement
    private String preferedLocale;

    @XmlElement
    private String cameraDateTimeZone;

    public UserPreferences() {
    }

    public UserPreferences(String dateFormat, String preferedLocale, String cameraDateTimeZone) {
        this.dateFormat = dateFormat;
        this.preferedLocale = preferedLocale;
        this.cameraDateTimeZone = cameraDateTimeZone;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getPreferedLocale() {
        return preferedLocale;
    }

    public void setPreferedLocale(String preferedLocale) {
        this.preferedLocale = preferedLocale;
    }

    public String getCameraDateTimeZone() {
        return cameraDateTimeZone;
    }

    public void setCameraDateTimeZone(String cameraDateTimeZone) {
        this.cameraDateTimeZone = cameraDateTimeZone;
    }
}
