package fr.untitled2.common.entities.raspi.batchlet;

import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/19/13
 * Time: 11:52 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class BatchTaskDescription {

    @XmlElement
    private String batchTaskId;

    @XmlElement
    private String startDate;

    @XmlElement
    private String endDate;

    @XmlElement
    private String bacthletId;

    @XmlElement
    private String batchletName;

    @XmlElement
    private String processingBatchServerId;

    @XmlElement
    private String processingBatchServerName;

    @XmlElement
    private String requestBatchServerId;

    @XmlElement
    private String requestBatchServerName;

    public String getBatchTaskId() {
        return batchTaskId;
    }

    public void setBatchTaskId(String batchTaskId) {
        this.batchTaskId = batchTaskId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getBacthletId() {
        return bacthletId;
    }

    public void setBacthletId(String bacthletId) {
        this.bacthletId = bacthletId;
    }

    public String getBatchletName() {
        return batchletName;
    }

    public void setBatchletName(String batchletName) {
        this.batchletName = batchletName;
    }

    public String getProcessingBatchServerId() {
        return processingBatchServerId;
    }

    public void setProcessingBatchServerId(String processingBatchServerId) {
        this.processingBatchServerId = processingBatchServerId;
    }

    public String getProcessingBatchServerName() {
        return processingBatchServerName;
    }

    public void setProcessingBatchServerName(String processingBatchServerName) {
        this.processingBatchServerName = processingBatchServerName;
    }

    public String getRequestBatchServerId() {
        return requestBatchServerId;
    }

    public void setRequestBatchServerId(String requestBatchServerId) {
        this.requestBatchServerId = requestBatchServerId;
    }

    public String getRequestBatchServerName() {
        return requestBatchServerName;
    }

    public void setRequestBatchServerName(String requestBatchServerName) {
        this.requestBatchServerName = requestBatchServerName;
    }
}
