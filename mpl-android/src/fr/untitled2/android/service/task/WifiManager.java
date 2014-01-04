package fr.untitled2.android.service.task;

import com.google.common.base.Optional;
import fr.untitled2.android.service.SchedulingService;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.sqlilite.WifiDetectionHolder;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.utils.DateTimeUtils;
import fr.untitled2.common.utils.DistanceUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 15/12/13
 * Time: 13:10
 * To change this template use File | Settings | File Templates.
 */
public class WifiManager extends ITask {

    private static DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("HH:mm:ss");

    private KnownLocation currentKnownLocation = null;

    @Override
    protected void executeTask(DbHelper dbHelper, Preferences preferences) throws Exception {
        Optional<String> ssidOptional = getWIFISSID();
        if (ssidOptional.isPresent()) {
            String ssid = ssidOptional.get();
            boolean isKnownWifi = false;
            for (KnownLocation knownLocation : preferences.getKnownLocations()) {
                if (knownLocation.getWifiSSIDs().contains(ssid)) {
                    isKnownWifi = true;
                    Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();
                    if (logRecordingOptional.isPresent()) {
                        if (logRecordingOptional.get().getLastLogRecord() != null) {
                            if (DistanceUtils.getDistance(knownLocation.getLatitudeLongitude(), logRecordingOptional.get().getLastLogRecord().getLatitudeAndLongitude()) > knownLocation.getDetectionRadius()) {
                                LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                                logRecord.setKnownLocation(knownLocation);
                                logRecord.setDateTime(DateTimeUtils.getCurrentDateTimeInUTC());
                                logRecord.setAltitude(knownLocation.getAltitude());
                                logRecord.setLongitude(knownLocation.getLongitude());
                                logRecord.setLatitude(knownLocation.getLatitude());

                                dbHelper.addRecordToCurrentLog(logRecord, Optional.of(knownLocation));
                                updateMainView();
                                sendNotificationToUser(dateTimeFormat.print(DateTime.now()) + " : Arrived at " + knownLocation.getName() + "", "The wifi network '" + ssid + "' is known as located at " + knownLocation.getName() + " you are now considered as located to this point", SchedulingService.MessageType.arrival, true);
                            }
                        } else {
                            LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                            logRecord.setLatitude(knownLocation.getLatitude());
                            logRecord.setLongitude(knownLocation.getLongitude());
                            logRecord.setAltitude(knownLocation.getAltitude());
                            logRecord.setDateTime(DateTimeUtils.getCurrentDateTimeInUTC());
                            logRecord.setKnownLocation(knownLocation);

                            dbHelper.addRecordToCurrentLog(logRecord, Optional.of(knownLocation));
                            updateMainView();
                        }
                    }
                    if (currentKnownLocation == null) {
                        sendNotificationToUser(dateTimeFormat.print(DateTime.now()) + " : Arrived at " + knownLocation.getName() + "", "The wifi network '" + ssid + "' is known as located at " + knownLocation.getName() + " you are now considered as located to this point", SchedulingService.MessageType.arrival, true);
                        currentKnownLocation = knownLocation;
                    }
                    stopRecorderService();
                    break;
                }
            }

            if (!isKnownWifi) {
                Optional<WifiDetectionHolder> wifiDetectionHolderOptional = dbHelper.getDetectedWifiBySSID(ssid);
                if (!wifiDetectionHolderOptional.isPresent()) {
                    dbHelper.markWifiAsDetected(ssid, DateTimeUtils.getCurrentDateTimeInUTC());
                } else {
                    WifiDetectionHolder wifiDetectionHolder = wifiDetectionHolderOptional.get();
                    LocalDateTime now = DateTimeUtils.getCurrentDateTimeInUTC();
                    if (wifiDetectionHolder.getWifiDetection().getDetectionDate().isBefore(now.minusMinutes(2))) {
                        wifiDetectionHolder.setStable(true);
                        dbHelper.updateDetectedWifi(wifiDetectionHolder);
                        updateMainView();
                    }
                }
                if (currentKnownLocation != null) {
                    sendNotificationToUser(dateTimeFormat.print(DateTime.now()) + " : You have left " + currentKnownLocation.getName(), "You are out of wifi range", SchedulingService.MessageType.departure, true);
                    currentKnownLocation = null;
                }
                startRecorderService();
            }
        } else {
            if (currentKnownLocation != null) {
                sendNotificationToUser(dateTimeFormat.print(DateTime.now()) + " : You have left " + currentKnownLocation.getName(), "You are out of wifi range", SchedulingService.MessageType.departure, true);
                currentKnownLocation = null;
            }
            startRecorderService();
        }
    }

    @Override
    public SchedulingService.Scheduling getScheduling() {
        return SchedulingService.Scheduling.SHORT;
    }

    @Override
    public boolean isAvailableOnSleepMode() {
        return true;
    }
}
