package fr.untitled2.entities;

import fr.untitled2.servlet.process.ReadEmailsServlet;
import fr.untitled2.utils.JSonUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/28/13
 * Time: 6:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogTest {

    @Test
    public void testXml() throws Exception {
        Log log = new Log();
        log.setName("TripName");
        log.setStartTime(LocalDateTime.now().minusHours(1));
        log.setEndTime(LocalDateTime.now());

        TrackPoint trackPoint1 = new TrackPoint();
        trackPoint1.setAltitude(1.0);
        trackPoint1.setLatitude(1.0);
        trackPoint1.setLongitude(1.0);
        trackPoint1.setPointDate(LocalDateTime.now().minusMinutes(50));

        TrackPoint trackPoint2 = new TrackPoint();
        trackPoint2.setAltitude(2.0);
        trackPoint2.setLatitude(2.0);
        trackPoint2.setLongitude(2.0);
        trackPoint2.setPointDate(LocalDateTime.now().minusMinutes(40));

        log.getTrackPoints().add(trackPoint1);
        log.getTrackPoints().add(trackPoint2);
//        System.out.println(JAXBUtils.marshal(log, true));
        System.out.println(JSonUtils.writeJson(log));
        log = JSonUtils.readJson(Log.class, JSonUtils.writeJson(log));
        System.out.println("Log : " + log.getName() + " (" + log.getStartTime() + ")->(" + log.getEndTime() + ")");
        for (TrackPoint trackPoint : log.getTrackPoints()) {
            System.out.println("\t - Altitude : " + trackPoint.getAltitude());
            System.out.println("\t - Latitude : " + trackPoint.getLatitude());
            System.out.println("\t - Longitude : " + trackPoint.getLongitude());
            System.out.println("\t - Date : " + trackPoint.getPointDate());
        }

    }

    @Test
    public void testGPX() throws Exception {
        File gpxFile = new File("/Users/corentinescoffier/Desktop/Daily-monop-.gpx");
        User user = new User("corentin.escoffier@gmail.com");
        Log log = ReadEmailsServlet.GPX_TRAILS_TRANSFORMER.apply(user, IOUtils.toString(new FileInputStream(gpxFile)));
        System.out.println(JSonUtils.writeJson(log));
    }

}
