package fr.untitled2.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Translate;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/29/13
 * Time: 2:01 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class PendingBatchServer {

    @Id
    private String hostIpAddress;

    private String serverIds;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime lastUpdated;

    public String getHostIpAddress() {
        return hostIpAddress;
    }

    public void setHostIpAddress(String hostIpAddress) {
        this.hostIpAddress = hostIpAddress;
    }

    public String getServerIds() {
        return serverIds;
    }

    public void setServerIds(String serverIds) {
        this.serverIds = serverIds;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PendingBatchServer that = (PendingBatchServer) o;

        if (hostIpAddress != null ? !hostIpAddress.equals(that.hostIpAddress) : that.hostIpAddress != null)
            return false;
        if (lastUpdated != null ? !lastUpdated.equals(that.lastUpdated) : that.lastUpdated != null) return false;
        if (serverIds != null ? !serverIds.equals(that.serverIds) : that.serverIds != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hostIpAddress != null ? hostIpAddress.hashCode() : 0;
        result = 31 * result + (serverIds != null ? serverIds.hashCode() : 0);
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        return result;
    }
}
