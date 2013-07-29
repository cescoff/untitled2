package fr.untitled2.desktop;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 6/26/13
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {

    @XmlElement
    private String oAuthAccessKey;

    @XmlElement
    private String oAuthAccessSecret;

    public String getoAuthAccessKey() {
        return oAuthAccessKey;
    }

    public void setoAuthAccessKey(String oAuthAccessKey) {
        this.oAuthAccessKey = oAuthAccessKey;
    }

    public String getoAuthAccessSecret() {
        return oAuthAccessSecret;
    }

    public void setoAuthAccessSecret(String oAuthAccessSecret) {
        this.oAuthAccessSecret = oAuthAccessSecret;
    }
}
