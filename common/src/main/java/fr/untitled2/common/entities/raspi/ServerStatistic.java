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
 * Time: 9:24 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class ServerStatistic {

    @XmlElement(name = "lavg")
    private double loadAverage;

    @XmlElement(name = "cpu")
    private double cpuPercentage;

    @XmlElement(name = "mem")
    private double memoryPercentage;

    @XmlElement
    private LocalDateTime date;

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
