package fr.untitled2.entities;

import com.beust.jcommander.internal.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import fr.untitled2.common.entities.raspi.ServerStatistic;
import fr.untitled2.common.entities.raspi.ServerStatistics;
import fr.untitled2.utils.JSonUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 10:25 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class BatchServer {

    private static Logger logger = LoggerFactory.getLogger(BatchServer.class);

    @Id
    private String serverId;

    private String hostIp;

    private String generateTokenUrl;

    private String oauthCode;

    private String hostName;

    private int numberOfCpuCore;

    private String uptime;

    private String jsonStatistics;

    @Ignore
    private List<ServerStatistic> statistics = Lists.newArrayList();

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime creationDate;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime lastContactDate;

    @Index
    private Key<User> user;

    public boolean isConnected() {
        return StringUtils.isEmpty(generateTokenUrl) && StringUtils.isEmpty(oauthCode);
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getOauthCode() {
        return oauthCode;
    }

    public void setOauthCode(String oauthCode) {
        this.oauthCode = oauthCode;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getGenerateTokenUrl() {
        return generateTokenUrl;
    }

    public void setGenerateTokenUrl(String generateTokenUrl) {
        this.generateTokenUrl = generateTokenUrl;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getNumberOfCpuCore() {
        return numberOfCpuCore;
    }

    public void setNumberOfCpuCore(int numberOfCpuCore) {
        this.numberOfCpuCore = numberOfCpuCore;
    }

    public List<ServerStatistic> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<ServerStatistic> statistics) {
        this.statistics = statistics;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastContactDate() {
        return lastContactDate;
    }

    public void setLastContactDate(LocalDateTime lastContactDate) {
        this.lastContactDate = lastContactDate;
    }

    public User getUser() {
        if (user == null)  return null;
        User result = ObjectifyService.ofy().load().key(user).get();
        if (result == null) {
            result = ObjectifyService.ofy().load().key(Key.create(User.class, user.getString())).get();
        }
        return result;
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    @OnSave
    public void prepersist() {
        ServerStatistics serverStatistics = new ServerStatistics();
        serverStatistics.getStatistics().addAll(statistics);
        try {
            this.jsonStatistics = JSonUtils.writeJson(serverStatistics);
        } catch (IOException e) {
            logger.error("An error has occured while persisting statistics", e);
        }
    }

    @OnLoad
    public void postload() {
        try {
            if (StringUtils.isNotEmpty(jsonStatistics)) {
                ServerStatistics serverStatistics = JSonUtils.readJson(ServerStatistics.class, jsonStatistics);
                this.statistics.addAll(serverStatistics.getStatistics());
            }
        } catch (Throwable t) {
            throw new IllegalStateException("Enable to read json trackpoints", t);
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BatchServer that = (BatchServer) o;

        if (numberOfCpuCore != that.numberOfCpuCore) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (generateTokenUrl != null ? !generateTokenUrl.equals(that.generateTokenUrl) : that.generateTokenUrl != null)
            return false;
        if (hostIp != null ? !hostIp.equals(that.hostIp) : that.hostIp != null) return false;
        if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null) return false;
        if (oauthCode != null ? !oauthCode.equals(that.oauthCode) : that.oauthCode != null) return false;
        if (serverId != null ? !serverId.equals(that.serverId) : that.serverId != null) return false;
        if (uptime != null ? !uptime.equals(that.uptime) : that.uptime != null) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serverId != null ? serverId.hashCode() : 0;
        result = 31 * result + (hostIp != null ? hostIp.hashCode() : 0);
        result = 31 * result + (generateTokenUrl != null ? generateTokenUrl.hashCode() : 0);
        result = 31 * result + (oauthCode != null ? oauthCode.hashCode() : 0);
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        result = 31 * result + numberOfCpuCore;
        result = 31 * result + (uptime != null ? uptime.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
