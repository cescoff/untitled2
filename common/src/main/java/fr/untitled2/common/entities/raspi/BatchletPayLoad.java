package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 2:08 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class BatchletPayLoad {

    @XmlElement
    private String batchletClass;

    @XmlElement
    private int frequency;

    @XmlElement
    private TimeUnit frequencyTimeUnit;

    public String getBatchletClass() {
        return batchletClass;
    }

    public void setBatchletClass(String batchletClass) {
        this.batchletClass = batchletClass;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public TimeUnit getFrequencyTimeUnit() {
        return frequencyTimeUnit;
    }

    public void setFrequencyTimeUnit(TimeUnit frequencyTimeUnit) {
        this.frequencyTimeUnit = frequencyTimeUnit;
    }
}
