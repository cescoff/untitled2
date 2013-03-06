package fr.untitled2.entities;

import org.junit.Test;

import java.util.TimeZone;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 5:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserTest {
    @Test
    public void testGetTimeZoneId() throws Exception {
        for (String id  : TimeZone.getAvailableIDs()) {
            System.out.println(id);
        }
    }
}
