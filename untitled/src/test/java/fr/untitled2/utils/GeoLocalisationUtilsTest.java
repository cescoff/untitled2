package fr.untitled2.utils;

import com.google.common.collect.Lists;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.process.ReadEmailsServlet;
import org.apache.commons.io.IOUtils;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/8/13
 * Time: 7:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeoLocalisationUtilsTest {
    @Test
    public void testUpdateImagesLocalisation() throws Exception {
        DateTime imageDate = new DateTime("2013-02-07T15:25:42");
        Image image = new Image();
        image.setDateTaken(imageDate.toLocalDateTime());
//        File gpxFile = new File("/Users/corentinescoffier/Downloads/Daily-monop-.gpx");
        File gpxFile = new File("/Users/corentinescoffier/Downloads/Retromonile.gpx");

        User user = new User("corentin.escoffier@gmail.com");
        Log log = ReadEmailsServlet.GPX_TRAILS_TRANSFORMER.apply(user, IOUtils.toString(new FileInputStream(gpxFile)));

        fr.untitled2.utils.GeoLocalisationUtils.updateImagesLocalisation(Lists.newArrayList(image), Lists.newArrayList(log));
        System.out.println(image.getLatitude());
        System.out.println(image.getLongitude());
    }

    @Test
    public void testLength() {
        String text = "http://x5-teak-clarity-4.appspot.com/ihm/maps/view?mapKey=d323cf917f37c30922b952cf05bccacfe7b46f87&headless=true";
        System.out.println(text.length());
    }

    @Test
    public void getTimeMillis() {
        System.out.println(System.currentTimeMillis());
        System.out.println(DateTime.now().getMillis());
    }

    @Test
    public void testElevations() {
        long timer = System.currentTimeMillis();
        // {"knownLocations":[{"name":"Ile de Re","latitude":46.181415,"longitude":-1.38847},{"name":"Geanges","latitude":46.959314,"longitude":4.910996}, {"name":"Cabeche","latitude":46.781975,"longitude":4.846762}]}
        List<Triplet<LocalDateTime, Double, Double>> sample = Lists.newArrayList();
        sample.add(Triplet.with(LocalDateTime.now().minusDays(1), 48.823688, 2.344324));
        sample.add(Triplet.with(LocalDateTime.now().minusDays(2), 48.82331, 2.343532));
        sample.add(Triplet.with(LocalDateTime.now().minusDays(3), 48.869112, 2.324778));
        sample.add(Triplet.with(LocalDateTime.now().minusDays(3), 48.893612, 2.347791));
        sample.add(Triplet.with(LocalDateTime.now().minusDays(3), 46.181415, -1.38847));
        sample.add(Triplet.with(LocalDateTime.now().minusDays(3), 46.959314, 4.910996));

        List<Triplet<LocalDateTime, Double, Double>> input = Lists.newArrayList();
        for (int index = 0; index < 1; index++) {
            for (Triplet<LocalDateTime, Double, Double> objects : sample) {
                input.add(objects);
            }
        }

        List<Quartet<LocalDateTime, Double, Double, Double>> pointsWithElevations = fr.untitled2.common.utils.GeoLocalisationUtils.getAltitudes(input);

        Assert.assertEquals(pointsWithElevations.size(), input.size());

        for (int index = 0; index < pointsWithElevations.size(); index++) {
            Assert.assertEquals(input.get(index).getValue0(), pointsWithElevations.get(index).getValue0());
            Assert.assertEquals(input.get(index).getValue1(), pointsWithElevations.get(index).getValue1());
            Assert.assertEquals(input.get(index).getValue2(), pointsWithElevations.get(index).getValue2());
            System.out.println("Altitude : " + pointsWithElevations.get(index).getValue3());
        }
        System.out.println("Time : " + (System.currentTimeMillis() - timer) / 1000);
    }

}
