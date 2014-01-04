package fr.untitled2.common.utils;

import fr.untitled2.common.entities.Journey;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.utils.JAXBUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 30/11/13
 * Time: 18:18
 * To change this template use File | Settings | File Templates.
 */
public class JourneyUtilsTest {
    @Test
    public void testGetJourneys() throws Exception {
        LocalDateTime start = LocalDateTime.now().withYear(2013).withMonthOfYear(12).withDayOfMonth(6).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        LocalDateTime end = LocalDateTime.now().withYear(2013).withMonthOfYear(12).withDayOfMonth(7).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        ServerConfig serverConfig = JAXBUtils.unmarshal(ServerConfig.class, new File("/Users/corentinescoffier/.myPictureLog/serverConfig.xml"));
        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());
        UserPreferences userPreferences = appEngineOAuthClient.getUserPreferences();
        for (LogRecording logRecording : appEngineOAuthClient.getMatchingLogRecordings(start, end)) {
            System.out.println("Calculating journeys for log recording '" + logRecording.getName() + "'");
            Collection<Journey> journeys = JourneyUtils.getJourneys(logRecording, userPreferences.getKnownLocations());
            for (Journey journey : journeys) {
                double maxSpeedInKmH = 3.6 * journey.getMaxSpeed();
                System.out.println("Start : '" + journey.getStart().getName() + "'");
                System.out.println("End : '" + journey.getEnd().getName() + "'");
                System.out.println("PointCount : '" + journey.getPointCount() + "'");
                System.out.println("Distance : '" + journey.getDistance() / 1000 + "'");
                System.out.println("MaxSpeed : " + maxSpeedInKmH + "km/h");
                if (maxSpeedInKmH > 35) {
                    System.out.println("TransportationType : car");
                } else if (maxSpeedInKmH >= 20) {
                    System.out.println("TransportationType : bike");
                } else {
                    System.out.println("TransportationType : unknown");
                }
                System.out.println("StartTime : '" + journey.getStartDatetime() + "'");
                System.out.println("EndTime : '" + journey.getEndDateTime() + "'");
                if (journey.getStartDatetime() != null && journey.getEndDateTime() != null) System.out.println("Duration : '" + new Period(journey.getStartDatetime(), journey.getEndDateTime()) + "' minutes");
                System.out.println("<-------------------------------------------------------------->");
            }
        }
    }
}
