package fr.untitled2.common.entities;

import fr.untitled2.common.entities.jaxb.LocalDateTimeAdapter;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 17/12/13
 * Time: 21:54
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class WifiDetection {

    @XmlElement
    private String ssid;

    @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime detectionDate;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public LocalDateTime getDetectionDate() {
        return detectionDate;
    }

    public void setDetectionDate(LocalDateTime detectionDate) {
        this.detectionDate = detectionDate;
    }
}
