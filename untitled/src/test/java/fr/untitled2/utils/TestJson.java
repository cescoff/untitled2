package fr.untitled2.utils;

import fr.untitled2.common.entities.LogRecording;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/12/13
 * Time: 11:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestJson {

    @Test
    public void testPeriod() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println(new Period(localDateTime, localDateTime.plusMinutes(10)).toStandardDuration().getMillis());

    }
    @Test
    public void testJson() throws Exception {
        LogRecording logRecording = new LogRecording();
        logRecording.setName("2013-03-11 14:34");
        logRecording.setDateTimeZone("Europe/Paris");

        LogRecording.LogRecord logRecord1 = new LogRecording.LogRecord();
        logRecord1.setLogRecordingId(10001L);
        logRecord1.setDateTime(LocalDateTime.now());
        logRecord1.setLatitude(50.0);
        logRecord1.setLongitude(2.34);

        LogRecording.LogRecord logRecord2 = new LogRecording.LogRecord();
        logRecord2.setLogRecordingId(10001L);
        logRecord2.setDateTime(LocalDateTime.now());
        logRecord2.setLatitude(50.0);
        logRecord2.setLongitude(2.34);

        logRecording.getRecords().add(logRecord1);
        logRecording.getRecords().add(logRecord2);

        System.out.println(JSonUtils.writeJson(logRecording));

    }

}
