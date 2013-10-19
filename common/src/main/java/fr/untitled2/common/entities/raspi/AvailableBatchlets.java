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
 * Date: 10/13/13
 * Time: 2:12 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class AvailableBatchlets {

    @XmlElement
    private List<BatchletPayLoad> registerdBatchlets = Lists.newArrayList();

    public List<BatchletPayLoad> getRegisterdBatchlets() {
        return registerdBatchlets;
    }

    public void setRegisterdBatchlets(List<BatchletPayLoad> registerdBatchlets) {
        this.registerdBatchlets = registerdBatchlets;
    }
}
