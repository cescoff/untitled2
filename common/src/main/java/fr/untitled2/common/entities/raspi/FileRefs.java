package fr.untitled2.common.entities.raspi;

import com.beust.jcommander.internal.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/16/13
 * Time: 10:19 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class FileRefs {

    private List<FileRef> fileRefs = Lists.newArrayList();

    public List<FileRef> getFileRefs() {
        return fileRefs;
    }

    public void setFileRefs(List<FileRef> fileRefs) {
        this.fileRefs = fileRefs;
    }
}
