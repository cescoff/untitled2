package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/6/13
 * Time: 2:35 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class FileRef {

    @XmlElement
    private String id;

    @XmlElement
    private String name;

    private int filePartCount = 1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFilePartCount() {
        return filePartCount;
    }

    public void setFilePartCount(int filePartCount) {
        this.filePartCount = filePartCount;
    }

    public boolean isLargeFile() {
        return filePartCount > 1;
    }

}
