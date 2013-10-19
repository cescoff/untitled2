package fr.untitled2.utils;

import fr.untitled2.common.entities.raspi.FileRef;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/14/13
 * Time: 12:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class GzipUtilsTest {
    @Test
    public void testUnzipString() throws Exception {
        FileRef ref = new FileRef();
        ref.setId("4e340b2e362b67636ff5b2d467629a18339adae6");
        ref.setName("FILENAME");
        ref.setFilePartCount(20);
        System.out.println(GzipUtils.unzipString(GzipUtils.zipString(JSonUtils.writeJson(ref))));
    }
}
