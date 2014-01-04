package fr.untitled2.common.entities;

import fr.untitled2.utils.JSonUtils;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 15/12/13
 * Time: 03:48
 * To change this template use File | Settings | File Templates.
 */
public class KnownLocationTest {

    @Test
    public void testJSON() throws Exception {
        KnownLocation knownLocation = new KnownLocation();
        knownLocation.setName("Home");
        knownLocation.setLatitude(48.823688);
        knownLocation.setLongitude(2.344324);
        knownLocation.setAltitude(44.1873398);
        knownLocation.setDetectionRadius(50);
        knownLocation.getWifiSSIDs().add("koffy");
        knownLocation.getWifiSSIDs().add("koffy-liveboite");

        System.out.println(JSonUtils.writeJson(knownLocation));
    }
}
