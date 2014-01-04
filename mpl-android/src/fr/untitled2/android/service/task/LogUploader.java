package fr.untitled2.android.service.task;

import android.util.Log;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import fr.untitled2.android.entities.UploadProcessStatus;
import fr.untitled2.android.service.SchedulingService;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.DistanceUtils;
import fr.untitled2.utils.CollectionUtils;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 11/12/13
 * Time: 21:12
 * To change this template use File | Settings | File Templates.
 */
public class LogUploader extends ITask {

    @Override
    protected void executeTask(DbHelper dbHelper, Preferences preferences) throws Exception {
        Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();

        boolean connected = false;
        boolean connectionChecked = false;

        if (dbHelper.hasCurrentLog()) {
            if (logRecordingOptional.isPresent()) {
                LogRecording logRecording = logRecordingOptional.get();
                LogRecording.LogRecord lastLogRecord = logRecording.getLastLogRecord();

                Optional<UploadProcessStatus> uploadProcessStatusOptional = dbHelper.getLastUploadStatus();

                if (isLastUploadTooFarOrTooOld(uploadProcessStatusOptional, lastLogRecord, preferences)) {
                    if (isConnected()) {
                        connectionChecked = true;
                        connected = true;
                        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(preferences.getOauth2Key(), preferences.getOauthSecret());
//                        logRecording.addElevations();
                        appEngineOAuthClient.pushLogRecording(logRecording);
                        dbHelper.notifyUploadDone(DateTime.now(), lastLogRecord.getLatitude(), lastLogRecord.getLongitude());
                    } else {
                        connectionChecked = true;
                    }
                }

            }
        }

        Collection<LogRecording> logRecordingsToBeSent = dbHelper.getLogRecordingToBeSentToCloud();

        if (CollectionUtils.isNotEmpty(logRecordingsToBeSent)) {
            if (!connectionChecked) connected = isConnected();

            if (connected) {
                AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(preferences.getOauth2Key(), preferences.getOauthSecret());
                for (LogRecording logRecordingToBeUploaded : logRecordingsToBeSent) {
                    try {
                        logRecordingToBeUploaded.addElevations();
                    } catch (Throwable t) {
                        Log.e(getClass().getName(), "An error has occured while loading elevations", t);
                    }
                    appEngineOAuthClient.pushLogRecording(logRecordingToBeUploaded);
                    dbHelper.markLogRecordingAsSynchronizedWithCloud(logRecordingToBeUploaded.getId());
                }
            }
        }
    }

    private boolean isLastUploadTooFarOrTooOld(Optional<UploadProcessStatus> uploadProcessStatusOptional, LogRecording.LogRecord lastLogRecord, Preferences preferences) {
        if (!uploadProcessStatusOptional.isPresent()) return true;
        long durationMillis = new Period(uploadProcessStatusOptional.get().getLastUploadDate(), DateTime.now()).toStandardDuration().getMillis();
        return durationMillis > preferences.getFrequency()
                && (
                    durationMillis > 5 * preferences.getFrequency()
                    || DistanceUtils.getDistance(
                        Triplet.with(uploadProcessStatusOptional.get().getLastPointLatitude(), uploadProcessStatusOptional.get().getLastPointLongitude(), -1.0),
                        Triplet.with(lastLogRecord.getLatitude(), lastLogRecord.getLongitude(), -1.0)) > preferences.getMinDistance());
    }

    @Override
    public boolean isAvailableOnSleepMode() {
        return false;
    }

    @Override
    public SchedulingService.Scheduling getScheduling() {
        return SchedulingService.Scheduling.MIDDLE;
    }
}
