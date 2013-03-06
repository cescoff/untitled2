package fr.untitled2.servlet;

import fr.untitled2.entities.Log;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.process.ReadEmailsServlet;
import fr.untitled2.transformers.GPXMapping;
import fr.untitled2.utils.JAXBUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/5/13
 * Time: 2:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReadEmailsServletTest {
    @Test
    public void testDoPost() throws Exception {
        String fileName = "test.gpx";
        String regex = ".*\\.gpx";
        System.out.println(Pattern.compile(regex).matcher(fileName).matches());
    }

    @Test
    public void testMyTracks() throws Exception {
        String line = "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:mytracks=\"http://mytracks.stichling.info/myTracksGPX/1/0\" creator=\"myTracks\" version=\"1.1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk><name>2013-03-01 01:03:51</name><extensions><mytracks:area showArea=\"no\" areaDistance=\"0.000000\"/><mytracks:directionArrows showDirectionArrows=\"yes\"/><mytracks:sync syncPhotosOniPhone=\"no\"/><mytracks:timezone offset=\"60\"/></extensions><trkseg><trkpt lat=\"48.82318882086417\" lon=\"2.343870054738627\"><ele>59.4775390625</ele><time>2013-03-01T00:03:51Z</time><extensions><mytracks:speed>0</mytracks:speed><mytracks:photo filename=\"/private/var/mobile/Applications/54A99733-6C24-43F5-BC53-0931971ACD05/Documents/2013-03-01 01-04-10.jpg\" time=\"2013-03-01T00:04:10Z+01\"/></extensions></trkpt></trkseg></trk></gpx>";
        line ="<gpx>" + StringUtils.substring(line, StringUtils.indexOf(line, ">") + 1);
        System.out.println(line);
        while (StringUtils.indexOf(line, "<extensions>") > 0) {
            line = StringUtils.substring(line, 0, StringUtils.indexOf(line, "<extensions>")) + StringUtils.substring(line, StringUtils.indexOf(line, "</extensions>") + "</extensions>".length());
        }
        System.out.println(line);
        GPXMapping gpxMapping = JAXBUtils.unmarshal(GPXMapping.class, "<gpx>" + StringUtils.substring(line, StringUtils.indexOf(line, ">")));
    }

    @Test
    public void testGPXTransformer() throws Exception {
        File trailsFile = new File("/Users/corentinescoffier/Downloads/Daily-monop-.gpx");
        File myTrackiPadFile = new File("/Users/corentinescoffier/Downloads/2013-03-01 01_03_51.gpx");

        FileInputStream trailsInput = new FileInputStream(trailsFile);
        FileInputStream myTracksiPadInput = new FileInputStream(myTrackiPadFile);
        User user = new User();
        Log logTrails = ReadEmailsServlet.GPX_TRAILS_TRANSFORMER.apply(user, IOUtils.toString(trailsInput));
        Log logMyTracksiPad = ReadEmailsServlet.GPX_TRAILS_TRANSFORMER.apply(user, IOUtils.toString(myTracksiPadInput));

        System.out.println("Trails point count : " + logTrails.getTrackPoints().size());
        System.out.println("MyTracksiPad point count : " + logMyTracksiPad.getTrackPoints().size());

    }

}
