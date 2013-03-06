package fr.untitled2.imageconversion;

import fr.untitled2.utils.JAXBUtils;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 10:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class PostXmlTest {
    @Test
    public void testGetApiKey() throws Exception {
        PostXml postXml = new PostXml();
        postXml.setApiKey("key");
        postXml.setNotificationUrl("http://www.google.com");
        postXml.setSourceUrl("http://x5-teak-clarity-4.appspot.com/imageView?imageKey=12001&lowResolution=true");
        postXml.setTestMode("false");

        System.out.println(JAXBUtils.marshal(postXml, true));
    }
}
