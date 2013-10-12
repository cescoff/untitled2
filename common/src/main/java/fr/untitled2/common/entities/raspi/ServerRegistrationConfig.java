package fr.untitled2.common.entities.raspi;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class ServerRegistrationConfig {

    @XmlElement
    private ServerConfig serverConfig;

    @XmlElement
    private LocalDateTime urlGenerationTime;

    @XmlElement
    private String tokenGenerationUrl;

    @XmlElement
    private String tokenCode;

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public LocalDateTime getUrlGenerationTime() {
        return urlGenerationTime;
    }

    public void setUrlGenerationTime(LocalDateTime urlGenerationTime) {
        this.urlGenerationTime = urlGenerationTime;
    }

    public String getTokenGenerationUrl() {
        return tokenGenerationUrl;
    }

    public void setTokenGenerationUrl(String tokenGenerationUrl) {
        this.tokenGenerationUrl = tokenGenerationUrl;
    }

    public String getTokenCode() {
        return tokenCode;
    }

    public void setTokenCode(String tokenCode) {
        this.tokenCode = tokenCode;
    }
}
