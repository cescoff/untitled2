package fr.untitled2.common.utils.bindings;

import com.beust.jcommander.internal.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 12/12/13
 * Time: 21:30
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class PointOfInterests {

    @XmlElement
    private Collection<PointOfInterest> pointOfInterests = Lists.newArrayList();

    public Collection<PointOfInterest> getPointOfInterests() {
        return pointOfInterests;
    }

    public void setPointOfInterests(Collection<PointOfInterest> pointOfInterests) {
        this.pointOfInterests = pointOfInterests;
    }
}
