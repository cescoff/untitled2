package fr.untitled2.android.service.task;

import com.google.common.base.Optional;
import fr.untitled2.android.service.SchedulingService;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.sqlilite.KnownLocationWithDatetime;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.utils.DateTimeUtils;
import fr.untitled2.common.utils.DistanceUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 11/12/13
 * Time: 21:03
 * To change this template use File | Settings | File Templates.
 */
public class POIDateTimeSetter extends ITask {

    @Override
    protected void executeTask(DbHelper dbHelper, Preferences preferences) throws Exception {
        Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();

        Optional<KnownLocationWithDatetime> lastKnownLocationWithDatetimeOptional = dbHelper.getLastKnownLocation();

        if (logRecordingOptional.isPresent()) {
            if (logRecordingOptional.get().getLastLogRecord() != null) {
                Optional<KnownLocation> currentKnownLocationOptional = DistanceUtils.getKnownLocation(logRecordingOptional.get().getLastLogRecord().getLatitudeAndLongitude(), preferences.getKnownLocations());
                if (currentKnownLocationOptional.isPresent()) {
                    if (!currentKnownLocationOptional.get().equals(lastKnownLocationWithDatetimeOptional.get().getKnownLocation())) {
                        dbHelper.addKnownLocation(DateTimeUtils.getCurrentDateTimeInUTC(), currentKnownLocationOptional.get());
                        KnownLocationWithDatetime knownLocationWithDatetime = new KnownLocationWithDatetime();
                        knownLocationWithDatetime.setDistance(0.0);
                        knownLocationWithDatetime.setPointDate(DateTimeUtils.getCurrentDateTimeInUTC());
                        knownLocationWithDatetime.setKnownLocation(currentKnownLocationOptional.get());
                        lastKnownLocationWithDatetimeOptional = Optional.of(knownLocationWithDatetime);
                    }
                }
            }
        }

        if (lastKnownLocationWithDatetimeOptional.isPresent()) {
            KnownLocationWithDatetime knownLocationWithDatetime = lastKnownLocationWithDatetimeOptional.get();

            if (logRecordingOptional.isPresent()) {
                LogRecording currentLogRecording = logRecordingOptional.get();
                if (currentLogRecording.getLastLogRecord() != null) {
                    Optional<KnownLocation> currentKnownLocation = DistanceUtils.getKnownLocation(currentLogRecording.getLastLogRecord().getLatitudeAndLongitude(), preferences.getKnownLocations());
                    if (currentKnownLocation.isPresent() && currentKnownLocation.get().equals(knownLocationWithDatetime.getKnownLocation())) {
                        knownLocationWithDatetime.setPointDate(fr.untitled2.common.utils.DateTimeUtils.getCurrentDateTimeInUTC());
                        dbHelper.updateKnownLocation(knownLocationWithDatetime);
                        updateMainView();
                    }
                }
            }
        }
        updateMainView();
    }

    @Override
    public boolean isAvailableOnSleepMode() {
        return true;
    }

    @Override
    public SchedulingService.Scheduling getScheduling() {
        return SchedulingService.Scheduling.SHORT;
    }
}
