package fr.untitled2.utils;

import com.google.common.collect.Lists;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.process.ReadEmailsServlet;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

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

        GeoLocalisationUtils.updateImagesLocalisation(Lists.newArrayList(image), Lists.newArrayList(log));
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

}
