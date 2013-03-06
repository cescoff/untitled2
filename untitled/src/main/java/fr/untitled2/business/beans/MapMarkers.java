package fr.untitled2.business.beans;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/13/13
 * Time: 10:35 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
public class MapMarkers {

    public static class  LightWeightImageToMarker implements  Function<LightWeightImage, Marker> {

        private DateTimeFormatter dateTimeFormatter;

        public LightWeightImageToMarker(DateTimeFormatter dateTimeFormatter) {
            this.dateTimeFormatter = dateTimeFormatter;
        }

        @Override
        public Marker apply(LightWeightImage lightWeightImage) {
            return lightWeightImage.toMarker(dateTimeFormatter);
        }
    };

    @XmlElement
    private List<Marker> markers = Lists.newArrayList();

    @XmlElement
    private boolean userMapMarker = false;

    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    public boolean isUserMapMarker() {
        return userMapMarker;
    }

    public void setUserMapMarker(boolean userMapMarker) {
        this.userMapMarker = userMapMarker;
    }
}
