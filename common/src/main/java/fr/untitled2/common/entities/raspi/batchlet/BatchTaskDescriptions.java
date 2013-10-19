package fr.untitled2.common.entities.raspi.batchlet;

import com.beust.jcommander.internal.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/19/13
 * Time: 12:08 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class BatchTaskDescriptions {

    @XmlElement
    private List<BatchTaskDescription> descriptions = Lists.newArrayList();

    public List<BatchTaskDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<BatchTaskDescription> descriptions) {
        this.descriptions = descriptions;
    }
}
