package fr.untitled2.imageconversion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 10:43 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "queue-answer")
@XmlAccessorType(XmlAccessType.FIELD)
public class AnswerXml {

    @XmlElement
    private Status status;

    private Params params;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Params {

        @XmlElement
        private String downloadCounter;

        @XmlElement
        private String dateProcessed;

        @XmlElement
        private String directDownload;

        @XmlElement
        private String checksum;

        @XmlElement(name = "target_size")
        private String targetSize;

        @XmlElement(name = "convert_to")
        private String convertTo;

        @XmlElement(name = "mime_type")
        private String mimeType;

        @XmlElement
        private String hash;

        public String getDownloadCounter() {
            return downloadCounter;
        }

        public void setDownloadCounter(String downloadCounter) {
            this.downloadCounter = downloadCounter;
        }

        public String getDateProcessed() {
            return dateProcessed;
        }

        public void setDateProcessed(String dateProcessed) {
            this.dateProcessed = dateProcessed;
        }

        public String getDirectDownload() {
            return directDownload;
        }

        public void setDirectDownload(String directDownload) {
            this.directDownload = directDownload;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        public String getTargetSize() {
            return targetSize;
        }

        public void setTargetSize(String targetSize) {
            this.targetSize = targetSize;
        }

        public String getConvertTo() {
            return convertTo;
        }

        public void setConvertTo(String convertTo) {
            this.convertTo = convertTo;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Status {

        @XmlElement
        private String code;

        @XmlElement
        private String message;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
