package fr.untitled2.common.entities.raspi;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/30/13
 * Time: 10:26 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class StatisticsGraphEntry {

    @XmlElement
    private LocalDateTime date;

    @XmlElement
    private double cpu;

    @XmlElement
    private double mem;

    @XmlElement
    private double load;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public double getMem() {
        return mem;
    }

    public void setMem(double mem) {
        this.mem = mem;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }
}
