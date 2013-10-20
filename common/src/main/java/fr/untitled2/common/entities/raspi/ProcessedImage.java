package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/20/13
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class ProcessedImage {

    @XmlElement
    private FileRef originalFile;

    @XmlElement
    private FileRef optimizedFile;

    @XmlElement
    private FileRef thumbnailFile;

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

    public FileRef getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailFile(FileRef thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }
}
