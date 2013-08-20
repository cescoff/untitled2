package fr.untitled2.entities;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
* Created with IntelliJ IDEA.
* User: escoffier_c
* Date: 20/08/13
* Time: 11:00
* To change this template use File | Settings | File Templates.
*/
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TrackPointsHolder {

    @XmlElement(name = "point") @XmlElementWrapper(name = "points")
    private Collection<TrackPoint> trackPoints = Lists.newArrayList();

    public Collection<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(Collection<TrackPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }
}
