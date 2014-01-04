package fr.untitled2.common.json;

import fr.untitled2.common.utils.GoogleMapsUtils;
import fr.untitled2.common.utils.bindings.GeocodeResponse;
import org.apache.commons.io.IOUtils;
import org.javatuples.Pair;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 04/01/14
 * Time: 22:07
 * To change this template use File | Settings | File Templates.
 */
public class JSonMappingsTest {
    @Test
    public void testReadJson() throws Exception {
        Pair<Double, Double> position = GoogleMapsUtils.getGeocodes("84 rue Vergniaud 75013 PARIS");
        System.out.println(position.getValue0());
        System.out.println(position.getValue1());
        GeocodeResponse geocodeResponse = JSonMappings.readJson(GeocodeResponse.class, IOUtils.toString(new FileInputStream(new File("/Users/corentinescoffier/Developpement/json.txt"))));
        System.out.println(JSonMappings.toJson(geocodeResponse));
        geocodeResponse = JSonMappings.readJson(GeocodeResponse.class, JSonMappings.toJson(geocodeResponse));
        System.out.println(geocodeResponse.getStatus());
        for (GeocodeResponse.Result result : geocodeResponse.getResults()) {
            System.out.println(result.getGeometry().getLocation().getLatitude());
            System.out.println(result.getGeometry().getLocation().getLongitude());
        }
    }
}
