package fr.untitled2.entities;

import fr.untitled2.utils.JSonUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 8/31/13
 * Time: 10:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogTrackPointsTest {

    @Test
    public void testJSON() throws Exception {
        File jsonFile = new File("/Users/corentinescoffier/IdeaProjects/untitled2/untitled/src/main/webapp/resources/test.txt");
        String json = IOUtils.toString(new FileInputStream(jsonFile));
        TrackPointsHolder trackPointsHolder = JSonUtils.readJson(TrackPointsHolder.class, json);
        System.out.println(trackPointsHolder.getTrackPoints().size());
    }

}
