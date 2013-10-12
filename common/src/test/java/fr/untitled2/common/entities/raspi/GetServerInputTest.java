package fr.untitled2.common.entities.raspi;

import fr.untitled2.utils.JSonUtils;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 8:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetServerInputTest {
    @Test
    public void testSetHostIpAddress() throws Exception {
        GetServerInput getServerInput = new GetServerInput();
        getServerInput.setHostIpAddress("192.168.1..1");

        System.out.println(JSonUtils.writeJson(getServerInput));

        getServerInput = JSonUtils.readJson(GetServerInput.class, JSonUtils.writeJson(getServerInput));

    }
}
