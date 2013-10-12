package fr.untitled2.android.service;

import android.app.Activity;
import android.util.Log;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import fr.untitled2.android.entities.UploadProcessStatus;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.DistanceUtils;
import org.javatuples.Pair;
import org.joda.time.*;

import java.util.Collection;
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
        if (uploadHandle != null) uploadHandle.cancel(true);
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
                Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();
                if (logRecordingOptional.isPresent()) {
                    if (isItTimeToCreateANewLog(logRecordingOptional.get().getStartPointDate(), DateTimeZone.forID(logRecordingOptional.get().getDateTimeZone()))) {
                        dbHelper.markLogasToBeSent(logRecordingOptional.get().getId());
                        dbHelper.createNewLogInProgress();
                        if (CollectionUtils.isNotEmpty(logRecordingOptional.get().getRecords())) {
                            LogRecording.LogRecord logRecord = LogRecording.DATE_ORDERING.reverse().sortedCopy(logRecordingOptional.get().getRecords()).get(0);
                            logRecord.setDateTime(DateTime.now().toDateTime(DateTimeZone.UTC).toLocalDateTime());
                            dbHelper.addRecordToCurrentLog(logRecord);
                        }
                    }
                }

                boolean connected = false;
                boolean connectionChecked = false;

                if (dbHelper.hasCurrentLog()) {
                    if (logRecordingOptional.isPresent()) {
                        LogRecording logRecording = logRecordingOptional.get();
                        LogRecording.LogRecord lastLogRecord = logRecording.getLastLogRecord();

                        Optional<UploadProcessStatus> uploadProcessStatusOptional = dbHelper.getLastUploadStatus();

                        if (isLastUploadTooFarOrTooOld(uploadProcessStatusOptional, lastLogRecord)) {
                            if (NetUtils.isConnected(activity)) {
                                connectionChecked = true;
                                connected = true;
                                AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(preferences.getOauth2Key(), preferences.getOauthSecret());
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
                    if (!connectionChecked) connected = NetUtils.isConnected(activity);

                    if (connected) {
                        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(preferences.getOauth2Key(), preferences.getOauthSecret());
                        for (LogRecording logRecordingToBeUploaded : logRecordingsToBeSent) {
                            appEngineOAuthClient.pushLogRecording(logRecordingToBeUploaded);
                            dbHelper.markLogRecordingAsSynchronizedWithCloud(logRecordingToBeUploaded.getId());
                        }
                    }
                }
            } catch (Throwable t) {
                String stackTrace = Throwables.getStackTraceAsString(t);
                Log.e(getClass().getName(), "An error has occured while uplaoding logrecording", t);
            }

        }

        private boolean isItTimeToCreateANewLog(LocalDateTime currentLogStart, DateTimeZone timeZone) {
            if (currentLogStart == null) return false;
            currentLogStart = currentLogStart.toDateTime(DateTimeZone.UTC).toDateTime(timeZone).toLocalDateTime();
            if (LocalDateTime.now().getHourOfDay() == preferences.getAutoModeSyncHourOfDay()) {
                if (currentLogStart.toLocalDate().isBefore(LocalDate.now())) {
                    return true;
                } else {
                    if (currentLogStart.getHourOfDay() < preferences.getAutoModeSyncHourOfDay()) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isLastUploadTooFarOrTooOld(Optional<UploadProcessStatus> uploadProcessStatusOptional, LogRecording.LogRecord lastLogRecord) {
            if (!uploadProcessStatusOptional.isPresent()) return true;
            return uploadProcessStatusOptional.isPresent()
                    && (
                    new Period(uploadProcessStatusOptional.get().getLastUploadDate(), DateTime.now()).toStandardDuration().getMillis() > preferences.getFrequency()
                            && DistanceUtils.getDistance(
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
