package fr.untitled2.servlet.api;

import com.google.appengine.repackaged.org.joda.time.DateTimeZone;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserInfos;
import fr.untitled2.common.json.JSonMappings;
import fr.untitled2.utils.JSonUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/16/13
 * Time: 2:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserInfosServletTest {

    @Test
    public void testJSONOnUserInfos() throws Exception {
        UserInfos userInfos = new UserInfos();
        userInfos.setUserId("User:id");
        System.out.println(JSonMappings.getJSON(userInfos));
        System.out.println(JSonUtils.writeJson(userInfos));
        userInfos = JSonUtils.readJson(UserInfos.class, JSonMappings.getJSON(userInfos));
        System.out.println(userInfos.getUserId());
        userInfos = JSonMappings.getUserInfos(JSonUtils.writeJson(userInfos));
        System.out.println(userInfos.getUserId());
    }

    @Test
    public void testJsonOnLogRecording() throws Exception {
        LogRecording logRecording = new LogRecording();
        logRecording.setDateTimeZone(DateTimeZone.forID("Europe/Paris").getID());
        logRecording.setName("Name");

        LogRecording.LogRecord logRecord1 = new LogRecording.LogRecord();
        logRecord1.setLatitude(2.34);
        logRecord1.setLongitude(50.0);
        logRecord1.setDateTime(LocalDateTime.now());

        LogRecording.LogRecord logRecord2 = new LogRecording.LogRecord();
        logRecord2.setLatitude(2.34);
        logRecord2.setLongitude(50.0);
        logRecord2.setDateTime(LocalDateTime.now());

        logRecording.getRecords().add(logRecord1);
        logRecording.getRecords().add(logRecord2);

        System.out.println(JSonUtils.writeJson(logRecording));
        System.out.println(JSonMappings.getJSON(logRecording));
        System.out.println(JSonUtils.writeJson(JSonUtils.readJson(LogRecording.class, JSonMappings.getJSON(logRecording))));
        System.out.println(JSonUtils.writeJson(JSonMappings.getLogRecording(JSonUtils.writeJson(logRecording))));
        System.out.println(JSonMappings.getJSON(JSonMappings.getLogRecording(JSonMappings.getJSON(logRecording))));

    }

}
