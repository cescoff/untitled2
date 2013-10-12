package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/27/13
 * Time: 12:52 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class Statistics {

    @XmlElement
    private String serverId;

    @XmlElement
    private String uptime;

    @XmlElement
    private double cpuPercentage;

    @XmlElement
    private double memoryPercentage;

    @XmlElement
    private double loadAverage;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public double getCpuPercentage() {
        return cpuPercentage;
    }

    public void setCpuPercentage(double cpuPercentage) {
        this.cpuPercentage = cpuPercentage;
    }

    public double getMemoryPercentage() {
        return memoryPercentage;
    }

    public void setMemoryPercentage(double memoryPercentage) {
        this.memoryPercentage = memoryPercentage;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }
}
