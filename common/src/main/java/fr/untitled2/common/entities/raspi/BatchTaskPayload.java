package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:18 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class BatchTaskPayload {

    @XmlElement
    private String batchTaskId;

    @XmlElement
    private String batchletClassName;

    @XmlElement
    private String zippedPayload;

    @XmlElement
    private boolean success;

    @XmlElement
    private String log;

    @XmlElement
    private LogLevel logLevel;

    @XmlElement
    private String serverId;

    public String getBatchTaskId() {
        return batchTaskId;
    }

    public void setBatchTaskId(String batchTaskId) {
        this.batchTaskId = batchTaskId;
    }

    public String getBatchletClassName() {
        return batchletClassName;
    }

    public void setBatchletClassName(String batchletClassName) {
        this.batchletClassName = batchletClassName;
    }

    public String getZippedPayload() {
        return zippedPayload;
    }

    public void setZippedPayload(String zippedPayload) {
        this.zippedPayload = zippedPayload;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
