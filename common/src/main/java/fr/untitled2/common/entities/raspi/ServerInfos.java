package fr.untitled2.common.entities.raspi;

import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 6:45 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class ServerInfos {

    @XmlElement
    private String serverId;

    @XmlElement
    private String serverName;

    @XmlElement
    private String uptime;

    @XmlElement
    private int cpuCoreCount;

    @XmlElement
    private String lastContactDateTime;

    @XmlElement
    private String creationDate;

    @XmlElement
    private boolean onLine;

    @XmlElement
    private boolean connected;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public int getCpuCoreCount() {
        return cpuCoreCount;
    }

    public void setCpuCoreCount(int cpuCoreCount) {
        this.cpuCoreCount = cpuCoreCount;
    }

    public String getLastContactDateTime() {
        return lastContactDateTime;
    }

    public void setLastContactDateTime(String lastContactDateTime) {
        this.lastContactDateTime = lastContactDateTime;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isOnLine() {
        return onLine;
    }

    public void setOnLine(boolean onLine) {
        this.onLine = onLine;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
