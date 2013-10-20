package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/20/13
 * Time: 10:28 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class PhotoGalleryItem {

    @XmlElement
    private FileRef originalFile;

    @XmlElement
    private FileRef optimizedFile;

    @XmlElement
    private FileRef thumnailFile;

    public FileRef getOriginalFile() {
        return originalFile;
    }

    public void setOriginalFile(FileRef originalFile) {
        this.originalFile = originalFile;
    }

    public FileRef getOptimizedFile() {
        return optimizedFile;
    }

    public void setOptimizedFile(FileRef optimizedFile) {
        this.optimizedFile = optimizedFile;
    }

    public FileRef getThumnailFile() {
        return thumnailFile;
    }

    public void setThumnailFile(FileRef thumnailFile) {
        this.thumnailFile = thumnailFile;
    }
}
