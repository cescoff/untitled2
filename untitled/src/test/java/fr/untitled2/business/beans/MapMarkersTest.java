package fr.untitled2.business.beans;

import fr.untitled2.utils.JSonUtils;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/13/13
 * Time: 10:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapMarkersTest {
    @Test
    public void testGetMarkers() throws Exception {
        MapMarkers mapMarkers = new MapMarkers();
        mapMarkers.getMarkers().add(new Marker("57.7973333", "12.0502107", "Angered", "Representing :)", "", ""));
        mapMarkers.getMarkers().add(new Marker("57.6969943", "11.9865", "Gothenburg", "Swedens second largest city", "", ""));
        System.out.println(JSonUtils.writeJson(mapMarkers));
    }
}
