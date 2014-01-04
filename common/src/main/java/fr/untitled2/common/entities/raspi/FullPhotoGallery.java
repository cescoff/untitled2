package fr.untitled2.common.entities.raspi;

import com.beust.jcommander.internal.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/20/13
 * Time: 10:29 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class FullPhotoGallery {

    @XmlElement
    private String galleryId;

    @XmlElement
    private String galleryName;

    @XmlElement
    private List<PhotoGalleryItem> items = Lists.newArrayList();

    public String getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(String galleryId) {
        this.galleryId = galleryId;
    }

    public String getGalleryName() {
        return galleryName;
    }

    public void setGalleryName(String galleryName) {
        this.galleryName = galleryName;
    }

    public List<PhotoGalleryItem> getItems() {
        return items;
    }

    public void setItems(List<PhotoGalleryItem> items) {
        this.items = items;
    }
}
