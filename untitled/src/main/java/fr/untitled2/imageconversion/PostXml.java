package fr.untitled2.imageconversion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 9:54 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "queue")
@XmlAccessorType(XmlAccessType.FIELD)
public class PostXml {

    @XmlElement
    private String apiKey;

    @XmlElement
    private String targetType = "image";

    @XmlElement
    private String method;

    @XmlElement
    private String targetMethod = "convert-to-jpg";

    @XmlElement
    private String testMode = "false";

    @XmlElement
    private String sourceUrl;

    @XmlElement
    private String notificationUrl;

    @XmlElement
    private String hash;

    @XmlElement
    private File file;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getTestMode() {
        return testMode;
    }

    public void setTestMode(String testMode) {
        this.testMode = testMode;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class File {

        @XmlElement
        private String fileName;

        @XmlElement
        private String fileData;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileData() {
            return fileData;
        }

        public void setFileData(String fileData) {
            this.fileData = fileData;
        }
    }

}
