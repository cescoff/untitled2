package fr.untitled2.common.entities.raspi;

import fr.untitled2.common.entities.raspi.executables.KnownExecutables;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 9:01 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "server-config") @XmlAccessorType(XmlAccessType.FIELD)
public class ServerConfig {

    @XmlElement
    private String serverId;

    @XmlElement
    private String serverName;

    @XmlElement
    private String accessKey;

    @XmlElement
    private String accessSecret;

    @XmlElement
    private int cpuCoreCount;

    @XmlElementWrapper(name = "executables") @XmlElement(name = "executable")
    private List<KnownExecutables> knownExecutableses;

    public boolean isConnected() {
        return StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(accessSecret);
    }

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

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public List<KnownExecutables> getKnownExecutableses() {
        return knownExecutableses;
    }

    public void setKnownExecutableses(List<KnownExecutables> knownExecutableses) {
        this.knownExecutableses = knownExecutableses;
    }

    public int getCpuCoreCount() {
        return cpuCoreCount;
    }

    public void setCpuCoreCount(int cpuCoreCount) {
        this.cpuCoreCount = cpuCoreCount;
    }
}
