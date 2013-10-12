package fr.untitled2.common.entities.raspi;

import com.beust.jcommander.internal.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class ServerStatuses {

    @XmlElement
    private List<ServerStatus> serverStatuses = Lists.newArrayList();

    public List<ServerStatus> getServerStatuses() {
        return serverStatuses;
    }

    public void setServerStatuses(List<ServerStatus> serverStatuses) {
        this.serverStatuses = serverStatuses;
    }

    @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
    public static class ServerStatus {
        @XmlElement
        private String serverName;

        @XmlElement
        private String hostIp;

        @XmlElement
        private String registrationDate;

        @XmlElement
        private String uptime;

        public ServerStatus() {
        }

        public ServerStatus(String serverName, String hostIp, String registrationDate, String uptime) {
            this.serverName = serverName;
            this.hostIp = hostIp;
            this.registrationDate = registrationDate;
            this.uptime = uptime;
        }

        public ServerStatus(String serverName, String hostIp, String registrationDate) {
            this.serverName = serverName;
            this.hostIp = hostIp;
            this.registrationDate = registrationDate;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getHostIp() {
            return hostIp;
        }

        public void setHostIp(String hostIp) {
            this.hostIp = hostIp;
        }

        public String getRegistrationDate() {
            return registrationDate;
        }

        public void setRegistrationDate(String registrationDate) {
            this.registrationDate = registrationDate;
        }
    }
}
