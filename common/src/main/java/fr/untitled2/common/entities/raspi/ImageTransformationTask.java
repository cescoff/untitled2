package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/16/13
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class ImageTransformationTask {

    @XmlElement
    private String galleryId;

    @XmlElement
    private FileRef originalFile;

    public String getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(String galleryId) {
        this.galleryId = galleryId;
    }

    public FileRef getOriginalFile() {
        return originalFile;
    }

    public void setOriginalFile(FileRef originalFile) {
        this.originalFile = originalFile;
    }
}
