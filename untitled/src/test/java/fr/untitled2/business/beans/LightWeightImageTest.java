package fr.untitled2.business.beans;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/15/13
 * Time: 5:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class LightWeightImageTest {
    @Test
    public void testSerialize() throws Exception {
        LightWeightImage lightWeightImage = new LightWeightImage(1.0, 2.0, LocalDateTime.now(), "toto");
        ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        oos.writeObject(lightWeightImage);
    }
}
