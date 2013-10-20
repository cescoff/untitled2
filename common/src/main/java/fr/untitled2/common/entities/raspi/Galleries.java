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
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class Galleries {

    @XmlElement
    private List<FullPhotoGallery> galleries = Lists.newArrayList();

    public List<FullPhotoGallery> getGalleries() {
        return galleries;
    }

    public void setGalleries(List<FullPhotoGallery> galleries) {
        this.galleries = galleries;
    }
}
