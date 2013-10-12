package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 6:55 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class GetServerInput {

    @XmlElement
    private String hostIpAddress;

    @XmlElement
    private String serverId;

    public String getHostIpAddress() {
        return hostIpAddress;
    }

    public void setHostIpAddress(String hostIpAddress) {
        this.hostIpAddress = hostIpAddress;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
