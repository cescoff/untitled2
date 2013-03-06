package fr.untitled2.mvc;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/20/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageControllerTest {
    @Test
    public void testList() throws Exception {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        System.out.println(dateTimeFormatter.parseLocalDateTime("18/05/1980"));
    }
}
