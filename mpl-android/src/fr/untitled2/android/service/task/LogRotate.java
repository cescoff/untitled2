package fr.untitled2.android.service.task;

import com.google.common.base.Optional;
import fr.untitled2.android.service.SchedulingService;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.utils.DateTimeUtils;
import fr.untitled2.common.utils.DistanceUtils;
import fr.untitled2.utils.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 11/12/13
 * Time: 21:59
 * To change this template use File | Settings | File Templates.
 */
public class LogRotate extends ITask {

    @Override
    protected void executeTask(DbHelper dbHelper, Preferences preferences) throws Exception {
        Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();

        if (logRecordingOptional.isPresent()) {
            if (isItTimeToCreateANewLog(logRecordingOptional.get().getStartPointDate(), DateTimeZone.forID(logRecordingOptional.get().getDateTimeZone()), preferences)) {
                dbHelper.markLogasToBeSent(logRecordingOptional.get().getId());
                dbHelper.createNewLogInProgress();
                if (CollectionUtils.isNotEmpty(logRecordingOptional.get().getRecords())) {
                    LogRecording.LogRecord logRecord = LogRecording.DATE_ORDERING.reverse().sortedCopy(logRecordingOptional.get().getRecords()).get(0);
                    logRecord.setDateTime(DateTime.now().toDateTime(DateTimeZone.UTC).toLocalDateTime());
                    Optional<KnownLocation> knownLocationOptional = DistanceUtils.getKnownLocation(logRecord.getLatitudeAndLongitude(), preferences.getKnownLocations());
                    if (knownLocationOptional.isPresent()) dbHelper.addKnownLocation(DateTimeUtils.getCurrentDateTimeInUTC(), knownLocationOptional.get());
                    dbHelper.addRecordToCurrentLog(logRecord, knownLocationOptional);
                    updateMainView();
                }
            }
        }
    }

    private boolean isItTimeToCreateANewLog(LocalDateTime currentLogStart, DateTimeZone timeZone, Preferences preferences) {
        if (currentLogStart == null) return false;
        currentLogStart = DateTimeUtils.getDateTimeInTimeZone(currentLogStart, timeZone).toLocalDateTime();
        if (LocalDateTime.now().getHourOfDay() == preferences.getAutoModeSyncHourOfDay()) {
            if (currentLogStart.isBefore(LocalDateTime.now().withHourOfDay(preferences.getAutoModeSyncHourOfDay()).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAvailableOnSleepMode() {
        return true;
    }

    @Override
    public SchedulingService.Scheduling getScheduling() {
        return SchedulingService.Scheduling.MIDDLE;
    }
}
