package fr.untitled2.android.service;

import android.app.Activity;
import android.util.Log;
import com.google.common.base.Optional;
import fr.untitled2.android.entities.UploadProcessStatus;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.utils.DistanceUtils;
import org.javatuples.Pair;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 8/29/13
 * Time: 10:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogUploader {

    private static LogUploader instance;

    private DbHelper dbHelper;

    private Preferences preferences;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture uploadHandle;

    public LogUploader(DbHelper dbHelper, Preferences preferences) {
        this.dbHelper = dbHelper;
        this.preferences = preferences;
    }

    public static synchronized LogUploader getInstance(DbHelper dbHelper, Preferences preferences) {
        if (instance == null) {
            instance = new LogUploader(dbHelper, preferences);
        }
        return instance;
    }

    public void start(Activity activity) {
        Uploader uploader = new Uploader(dbHelper, preferences, activity);
        uploadHandle = scheduler.scheduleAtFixedRate(uploader, 30, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        uploadHandle.cancel(true);
        dbHelper.markUploadProcessAsStopped();
    }

    public static class Uploader implements Runnable {

        private DbHelper dbHelper;

        private Preferences preferences;

        private Activity activity;

        public Uploader(DbHelper dbHelper, Preferences preferences, Activity activity) {
            this.dbHelper = dbHelper;
            this.preferences = preferences;
            this.activity = activity;
        }

        @Override
        public void run() {
            try {
                if (NetUtils.isConnected(activity)) {
                    if (dbHelper.hasCurrentLog()) {
                        Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();
                        if (logRecordingOptional.isPresent()) {
                            LogRecording logRecording = logRecordingOptional.get();
                            LogRecording.LogRecord lastLogRecord = logRecording.getLastLogRecord();

                            Optional<UploadProcessStatus> uploadProcessStatusOptional = dbHelper.getLastUploadStatus();

                            AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(preferences.getOauth2Key(), preferences.getOauthSecret());
                            if (isLastUploadTooFarOrTooOld(uploadProcessStatusOptional, lastLogRecord)) {
                                appEngineOAuthClient.pushLogRecording(logRecording);
                                dbHelper.notifyUploadDone(DateTime.now(), lastLogRecord.getLatitude(), lastLogRecord.getLongitude());
                                if (isItTimeToCreateANewLog(logRecording.getStartPointDate())) {
                                    dbHelper.markLogRecordingAsSynchronizedWithCloud(logRecording.getId());
                                    dbHelper.createNewLogInProgress();
                                }
                            }
                            for (LogRecording logRecordingToBeUploaded : dbHelper.getLogRecordingToBeSentToCloud()) {
                                appEngineOAuthClient.pushLogRecording(logRecording);
                                dbHelper.markLogRecordingAsSynchronizedWithCloud(logRecordingToBeUploaded.getId());
                            }

                        }
                    }
                } else {
                    Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();
                    if (logRecordingOptional.isPresent()) {
                        if (isItTimeToCreateANewLog(logRecordingOptional.get().getStartPointDate())) {
                            dbHelper.markLogasToBeSent(logRecordingOptional.get().getId());
                            dbHelper.createNewLogInProgress();
                        }
                    }
                }
            } catch (Throwable t) {
                Log.e(getClass().getName(), "An error has occured while uplaoding logrecording", t);
            }

        }

        private boolean isItTimeToCreateANewLog(LocalDateTime currentLogStart) {
            return currentLogStart.isBefore(getChangeLogDateTime());
        }

        private boolean isLastUploadTooFarOrTooOld(Optional<UploadProcessStatus> uploadProcessStatusOptional, LogRecording.LogRecord lastLogRecord) {
            if (!uploadProcessStatusOptional.isPresent()) return true;
            return uploadProcessStatusOptional.isPresent()
                    && (
                    new Period(uploadProcessStatusOptional.get().getLastUploadDate(), DateTime.now()).toStandardDuration().getMillis() > preferences.getFrequency()
                            || DistanceUtils.getDistance(
                            Pair.with(uploadProcessStatusOptional.get().getLastPointLatitude(), uploadProcessStatusOptional.get().getLastPointLongitude()),
                            Pair.with(lastLogRecord.getLatitude(), lastLogRecord.getLongitude())) > preferences.getMinDistance());
        }

        private LocalDateTime getChangeLogDateTime() {
            LocalDateTime result = LocalDateTime.now().withHourOfDay(preferences.getAutoModeSyncHourOfDay()).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            if (DateTime.now().getHourOfDay() < preferences.getAutoModeSyncHourOfDay()) result = result.minusDays(1);
            return result;
        }

    }


}
