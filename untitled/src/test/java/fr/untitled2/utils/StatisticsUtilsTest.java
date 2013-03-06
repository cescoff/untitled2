package fr.untitled2.utils;

import fr.untitled2.entities.Log;
import fr.untitled2.entities.TrackPoint;
import fr.untitled2.statistics.LogStatistics;
import fr.untitled2.transformers.GPXMapping;
import org.apache.commons.io.LineIterator;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 3:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatisticsUtilsTest {

    @Test
    public void createMPLFileFromGPXFile() throws Exception {
        File gpxFile = new File("/Users/corentinescoffier/Desktop/Daily-monop-.gpx");
        File mplFile = new File("/Users/corentinescoffier/Desktop/Daily-monop-.mpl");

        StringBuilder xml = new StringBuilder("<gpx>");
        LineIterator lineIterator = new LineIterator(new InputStreamReader(new FileInputStream(gpxFile)));

        lineIterator.next();
        lineIterator.next();
        while (lineIterator.hasNext()) {
            xml.append(lineIterator.next()).append("\n");
        }
        GPXMapping gpxMapping = JAXBUtils.unmarshal(GPXMapping.class, xml.toString());

        Log log = new Log();
        for (GPXMapping.Trk trk : gpxMapping.getTracks()) {
            log.setName(trk.getName());
            for (GPXMapping.TrkSeg trkSeg : trk.getTrackSegments()) {
                for (GPXMapping.TrkPt trkPt : trkSeg.getTrackPoints()) {
                    TrackPoint trackPoint = new TrackPoint();
                    trackPoint.setPointDate(trkPt.getDateTime().toLocalDateTime());
                    trackPoint.setAltitude(trkPt.getAltitude());
                    trackPoint.setLatitude(trkPt.getLatitude());
                    trackPoint.setLongitude(trkPt.getLongitude());
                    log.getTrackPoints().add(trackPoint);
                }
            }
        }
        FileOutputStream fileOutputStream = new FileOutputStream(mplFile);
        JSonUtils.writeJson(log, fileOutputStream);
        fileOutputStream.close();

    }
    @Test
    public void testStatistics() throws Exception {
        File mplFile = new File("/Users/corentinescoffier/Desktop/Daily-monop-.mpl");
        Log log = JSonUtils.readJson(Log.class, new FileInputStream(mplFile));
        LogStatistics logStatistics = StatisticsUtils.getLogStatistics(log);

        System.out.println("Average speed : " + logStatistics.getAverageSpeed());
        System.out.println("Center : " + logStatistics.getCenter().getValue0() + ", " + logStatistics.getCenter().getValue1());
        System.out.println("Duration : " + logStatistics.getDuration());
        System.out.println("Min / Max Latitude : " + logStatistics.getMinMaxLatitude().getValue0() + ", " + logStatistics.getMinMaxLatitude().getValue1());
        System.out.println("Min / Max Longitude : " + logStatistics.getMinMaxLongitude().getValue0() + ", " + logStatistics.getMinMaxLongitude().getValue1());
        System.out.println("Total distance : " + logStatistics.getTotalDistance());

        List<TrackPoint> sortedTrackPoints = StatisticsUtils.TRACK_POINT_SORT.sortedCopy(log.getTrackPoints());
        for (TrackPoint sortedTrackPoint : sortedTrackPoints) {
            System.out.println("new google.maps.LatLng(" + sortedTrackPoint.getLatitude() + ", " + sortedTrackPoint.getLongitude() + "),");
        }


    }
    @Test
    public void testDates() {
        String dateTimeString = "2013-02-05T21:34:55Z";
        String localDateTimeString = "2013-02-05 22:34:55";

        LocalDateTime localDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseLocalDateTime(localDateTimeString);
        System.out.println(localDateTime.toDateTime(DateTimeZone.forID("Europe/Paris")).toDateTime(DateTimeZone.UTC));


/*
        DateTime dateTime = new DateTime(dateTimeString);
        System.out.println(dateTime.getZone());
        System.out.println(dateTime.toDateTime(DateTimeZone.UTC));
        System.out.println(dateTime.toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID("Europe/Paris")));
*/
    }

}
