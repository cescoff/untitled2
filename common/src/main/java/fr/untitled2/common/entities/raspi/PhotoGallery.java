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
 * Date: 10/16/13
 * Time: 8:57 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class PhotoGallery {

    @XmlElement
    private String id;

    @XmlElement
    private List<FileRef> originalFiles = Lists.newArrayList();

    @XmlElement
    private List<FileRef> fullResolutionFiles = Lists.newArrayList();

    @XmlElement
    private List<FileRef> miniFiles = Lists.newArrayList();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FileRef> getOriginalFiles() {
        return originalFiles;
    }

    public void setOriginalFiles(List<FileRef> originalFiles) {
        this.originalFiles = originalFiles;
    }

    public List<FileRef> getFullResolutionFiles() {
        return fullResolutionFiles;
    }

    public void setFullResolutionFiles(List<FileRef> fullResolutionFiles) {
        this.fullResolutionFiles = fullResolutionFiles;
    }

    public List<FileRef> getMiniFiles() {
        return miniFiles;
    }

    public void setMiniFiles(List<FileRef> miniFiles) {
        this.miniFiles = miniFiles;
    }
}
