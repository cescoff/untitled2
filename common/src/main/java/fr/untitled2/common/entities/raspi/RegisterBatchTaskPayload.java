package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:26 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class RegisterBatchTaskPayload {

    @XmlElement
    private String serverId;

    private String batchletClass;

    @XmlElement
    private String zippedPayload;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getBatchletClass() {
        return batchletClass;
    }

    public void setBatchletClass(String batchletClass) {
        this.batchletClass = batchletClass;
    }

    public String getZippedPayload() {
        return zippedPayload;
    }

    public void setZippedPayload(String zippedPayload) {
        this.zippedPayload = zippedPayload;
    }
}
