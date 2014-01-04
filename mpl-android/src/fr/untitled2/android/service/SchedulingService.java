package fr.untitled2.android.service;

import android.app.*;
import android.content.*;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import fr.untitled2.android.LogList;
import fr.untitled2.android.Main;
import fr.untitled2.android.R;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.service.task.*;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.sqlilite.ErrorReport;
import fr.untitled2.android.sqlilite.KnownLocationWithDatetime;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.android.utils.ServiceUtils;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.DateTimeUtils;
import fr.untitled2.common.utils.DistanceUtils;
import fr.untitled2.common.utils.NumberFormattingUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 11/12/13
 * Time: 21:36
 * To change this template use File | Settings | File Templates.
 */
public class SchedulingService extends Service {

    private static DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("HH:mm");

    private static final String ALARM_ACTION = "fr.untitled2.alarm";

    private static final long ONE_SECOND = 1000;

    private static final long TWENTY_SECONDS = ONE_SECOND * 20;

    private Object counterLocker = new Object();

    private int errorCounter = 1000;

    private Preferences preferences;

    private DbHelper dbHelper;

    private Map<Scheduling, LocalDateTime> lastRunDate = Maps.newHashMap();

    @Override
    public void onCreate() {
        try {
            super.onCreate();
            IntentFilter wifiIntentFilter = new IntentFilter();
            wifiIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);

            IntentFilter smsIntentFilter = new IntentFilter();
            smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

            registerReceiver(wifiListener, wifiIntentFilter);
            registerReceiver(alarmListener, Scheduling.getIntentFilter());
            registerReceiver(smsListener, smsIntentFilter);

            scheduleThreadsChecker();
        } catch (Throwable t) {
            sendNotificationToUser("Scheduler error", "Error executing tasks", SchedulingService.MessageType.error, true);
            Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
        }
    }

    private BroadcastReceiver alarmListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TaskManager.addTask(new LogRotate());
            TaskManager.addTask(new LogUploader());
            TaskManager.addTask(new POIDateTimeSetter());
            TaskManager.addTask(new PreferencesSynchronizer());
            TaskManager.addTask(new fr.untitled2.android.service.task.WifiManager());
            TaskManager.addTask(new KeepAlive());
            try {
                initPreferencesAndDbHelper();
                Multimap<Scheduling, ITask> tasksToExecute = HashMultimap.create();
                for (ITask iTask : TaskManager.getTasks()) {
                    //if (!lastRunDate.containsKey(iTask.getScheduling())) lastRunDate.put(iTask.getScheduling(), DateTimeUtils.getCurrentDateTimeInUTC());
                    initTask(iTask);
                    if (iTask.getScheduling().mustRun(lastRunDate.get(iTask.getScheduling()))) {
                        tasksToExecute.put(iTask.getScheduling(), iTask);
                    }
                }

                Multimap<Scheduling, Future<Boolean>> launchedTasks = TaskManager.executeTasks(tasksToExecute);
                for (Scheduling scheduling : launchedTasks.keySet()) {
                    lastRunDate.put(scheduling, DateTimeUtils.getCurrentDateTimeInUTC());
                    try {
                        for (Future<Boolean> booleanFuture : launchedTasks.get(scheduling)) {
                            booleanFuture.get(2 * scheduling.getFrequency(), scheduling.getTimeUnit());
                        }
                    } catch (Throwable t) {
                        //sendNotificationToUser("Scheduler timeout", "A timeout error occured", SchedulingService.MessageType.error, true);
                        Log.e(getClass().getName(),"Scheduler timeout : " +  Throwables.getStackTraceAsString(t));
                    }
                }
            } catch (Throwable t) {
                try {
                    sendNotificationToUser("Scheduler error", "Error executing tasks", SchedulingService.MessageType.error, true);
                    Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
                } catch (Throwable tt) {
                }
            }
            Log.d(SchedulingService.class.getName(), "Ended");
        }
    };

    private void initPreferencesAndDbHelper() {
        preferences = PreferencesUtils.getPreferences(this);
        dbHelper = new DbHelper(getApplicationContext(), preferences);
    }

    private void initTask(ITask iTask) {
        iTask.setPreferences(preferences);
        iTask.setDbHelper(dbHelper);
        iTask.setService(this);
        iTask.init();
    }

    private BroadcastReceiver smsListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            if ( extras != null )
            {
                Object[] smsextras = (Object[]) extras.get( "pdus" );

                for ( int i = 0; i < smsextras.length; i++ )
                {
                    SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])smsextras[i]);

                    String strMsgBody = smsmsg.getMessageBody().toString();
                    String strMsgSrc = smsmsg.getOriginatingAddress();

/*
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        String sms = getResponseSMS();
                        if (sms.length() > 100) {
                            ArrayList<String> parts = smsManager.divideMessage(sms);
                            smsManager.sendMultipartTextMessage(strMsgSrc, null, parts, null, null);
                        } else {
                            smsManager.sendTextMessage(strMsgSrc, null, getResponseSMS(), null, null);
                        }
                    } catch (Throwable t) {
                        sendNotificationToUser("SMS ERROR", strMsgSrc, MessageType.error, true);
                    }
*/
                }

            }
        }
    };

    private String getResponseSMS() {
        initPreferencesAndDbHelper();
        StringBuilder sms = new StringBuilder();

        Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();

        if (logRecordingOptional.isPresent()) {
            LogRecording logRecording = logRecordingOptional.get();
            LogRecording.LogRecord lastLogRecord = logRecording.getLastLogRecord();

            Optional<KnownLocation> currentKnownLocationOptional = DistanceUtils.getKnownLocation(lastLogRecord.getLatitudeAndLongitude(), preferences.getKnownLocations());

            if (currentKnownLocationOptional.isPresent()) {
                LocalDateTime lastPointDate = lastLogRecord.getDateTime();
                Optional<KnownLocationWithDatetime> lastKnownLocation = dbHelper.getLastKnownLocation();

                if (lastKnownLocation.isPresent() && lastKnownLocation.get().getKnownLocation().equals(currentKnownLocationOptional.get())) lastPointDate = lastKnownLocation.get().getPointDate();

                String[] value = new String[] {
                        preferences.getDateTimeFormatter().print(DateTimeUtils.getDateTimeInTimeZone(lastPointDate, logRecording.getDateTimeZone())),
                        NumberFormattingUtils.toLatitudeInDegreesMinutesSeconds(lastLogRecord.getLatitude()),
                        NumberFormattingUtils.toLongitudeInDegreesMinutesSeconds(lastLogRecord.getLongitude()),
                        NumberFormattingUtils.toDistance(lastLogRecord.getAltitude(), NumberFormattingUtils.DistanceUnit.metric),
                        currentKnownLocationOptional.get().getName(),
                        lastLogRecord.getLatitude() + "",
                        lastLogRecord.getLongitude() + ""
                };

                sms.append(preferences.getTranslation(I18nConstants.sms_lastknownlocationwithknownlocation, value));
            } else {
                Optional<KnownLocationWithDatetime> knownLocationOptional = dbHelper.getLastKnownLocation();
                if (knownLocationOptional.isPresent()) {
                    Period journeyDuration = new Period(knownLocationOptional.get().getPointDate(), DateTimeUtils.getCurrentDateTimeInUTC());
                    double avgSpeed = knownLocationOptional.get().getDistance() / journeyDuration.toStandardSeconds().getSeconds();
                    journeyDuration.withMillis(0);
                    if (journeyDuration.getHours() > 0) journeyDuration.withSeconds(0);
                    String[] value = new String[] {
                            preferences.getDateTimeFormatter().print(DateTimeUtils.getDateTimeInTimeZone(lastLogRecord.getDateTime(), logRecording.getDateTimeZone())),
                            NumberFormattingUtils.toLatitudeInDegreesMinutesSeconds(lastLogRecord.getLatitude()),
                            NumberFormattingUtils.toLongitudeInDegreesMinutesSeconds(lastLogRecord.getLongitude()),
                            NumberFormattingUtils.toDistance(lastLogRecord.getAltitude(), NumberFormattingUtils.DistanceUnit.metric),
                            knownLocationOptional.get().getKnownLocation().getName(),
                            PeriodFormat.wordBased(preferences.getUserLocale()).print(journeyDuration),
                            NumberFormattingUtils.toDistance(knownLocationOptional.get().getDistance(), NumberFormattingUtils.DistanceUnit.metric),
                            NumberFormattingUtils.toSpeed(avgSpeed, NumberFormattingUtils.DistanceUnit.metric),
                            lastLogRecord.getLatitude() + "",
                            lastLogRecord.getLongitude() + ""
                    };

                    sms.append(preferences.getTranslation(I18nConstants.sms_lastknownlocationwithjourneystarted, value));
                } else {
                    String[] value = new String[] {
                            preferences.getDateTimeFormatter().print(DateTimeUtils.getDateTimeInTimeZone(lastLogRecord.getDateTime(), logRecording.getDateTimeZone())),
                            NumberFormattingUtils.toLatitudeInDegreesMinutesSeconds(lastLogRecord.getLatitude()),
                            NumberFormattingUtils.toLongitudeInDegreesMinutesSeconds(lastLogRecord.getLongitude()),
                            NumberFormattingUtils.toDistance(lastLogRecord.getAltitude(), NumberFormattingUtils.DistanceUnit.metric),
                            lastLogRecord.getLatitude() + "",
                            lastLogRecord.getLongitude() + ""
                    };

                    sms.append(preferences.getTranslation(I18nConstants.sms_lastknownlocation, value));
                }
            }


        } else {
            sms.append(preferences.getTranslation(I18nConstants.sms_locationunknown));
        }

        return sms.toString();
    }

    private BroadcastReceiver wifiListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                    if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)){
                        NetUtils.notifyReconnected();
                        Thread.sleep(15000);
                        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo.getSSID();

                        for (KnownLocation knownLocation : preferences.getKnownLocations()) {
                            if (knownLocation.getWifiSSIDs().contains(ssid)) {
                                if (isConnected()) {
                                    //dbHelper.addKnownLocation(DateTimeUtils.getCurrentDateTimeInUTC(), knownLocation);
                                    LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                                    logRecord.setKnownLocation(knownLocation);
                                    logRecord.setAltitude(knownLocation.getAltitude());
                                    logRecord.setDateTime(DateTimeUtils.getCurrentDateTimeInUTC());
                                    logRecord.setLatitude(knownLocation.getLatitude());
                                    logRecord.setLongitude(knownLocation.getLongitude());
                                    dbHelper.addRecordToCurrentLog(logRecord, Optional.of(knownLocation));
                                    stopRecorderService();
                                    sendNotificationToUser("Arrived at " + knownLocation.getName() + "", "The wifi network '" + ssid + "' is known as located at " + knownLocation.getName() + " you are now considered as located to this point", MessageType.arrival, true);
                                }
                            }
                        }

                    } else {
                        startRecorderService();
                    }
                }
            } catch (Throwable t) {
                try {
                    dbHelper.addErrorReport(ErrorReport.fromThrowable(getClass(), "An error has occured while detecting wifi status change", t));
                } catch (Throwable tt) {
                    Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
                    Log.e(getClass().getName(), Throwables.getStackTraceAsString(tt));
                }
            }
        }
    };

    public void startRecorderService() {
        if (isServiceRunning()) return;
        Intent stopServiceIntent = new Intent(this, LogRecorder.class);
        startService(stopServiceIntent);
    }

    public void stopRecorderService() {
        Intent stopServiceIntent = new Intent(this, LogRecorder.class);
        stopService(stopServiceIntent);
    }


    private boolean isServiceRunning() {
        return ServiceUtils.isServiceRunning(this, LogRecorder.class);
    }

    public Optional<String> getWIFISSID() {
        String ssid = NetUtils.getCurrentWifiSSID(this);
        if (StringUtils.isNotEmpty(ssid)) return Optional.of(ssid);
        return Optional.absent();
    }

    public boolean isConnected() {
        if (!NetUtils.isWifiConnected(this)) return false;
        return NetUtils.isConnected(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotificationToUser("Start", "Scheduling service is starting", MessageType.bg_process_restart, false);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Binder binder = new Binder();
        return binder;
    }

    private void scheduleThreadsChecker() {
        AlarmManager alarmManager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));

        for (Scheduling scheduling : Scheduling.values()) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 1000002, scheduling.getIntent(), 0 );
            if (scheduling == Scheduling.SHORT) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), scheduling.getFrequencyInMillis(), pendingIntent);
            }
        }
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        onDestroy();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        sendNotificationToUser("Scheduling Service stopped", "The MyActivityLog scheduling service has been stopped, please go to the applicaton to wake it", MessageType.bg_process_restart, true);
        unregisterReceiver(wifiListener);
        unregisterReceiver(alarmListener);
        unregisterReceiver(smsListener);
    }

    public void sendNotificationToUser(String title, String message, MessageType messageType, boolean vibrate) {
        try {
            long[] vibrations = new long[0];
            if (vibrate) vibrations = new long[] {500, 500, 500};
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(messageType.icon)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setVibrate(vibrations);
// Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, Main.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(Main.class);
// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            Notification notification = mBuilder.getNotification();
            int code = messageType.code;
            if (messageType == MessageType.error) {
                code = errorCounter;
                synchronized (counterLocker) {
                    errorCounter++;
                }
            }
            mNotificationManager.notify(code, notification);
        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            Log.e(getClass().getName(), stackTrace);
        }
    }

    public void updatePreferences(Preferences newPreferences) {
        preferences = newPreferences;
        PreferencesUtils.setSharedPreferences(this, newPreferences);
        for (ITask iTask : TaskManager.getTasks()) {
            iTask.setPreferences(newPreferences);
        }
    }

    public void updateMainView() {
        Intent intent = new Intent("fr.untitled2.MainViewUpdater");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public static enum MessageType {

        arrival(10001, R.drawable.skip_forward),
        departure(10002, R.drawable.skip_backward),
        error(10002, R.drawable.flag),
        bg_process_restart(10003,  R.drawable.recycle_bin),
        keep_alive(10004, R.drawable.disc);

        private int code;

        private int icon;

        private MessageType(int code, int icon) {
            this.code = code;
            this.icon = icon;
        }

        public int getCode() {
            return code;
        }

        public int getIcon() {
            return icon;
        }
    }

    public static enum Scheduling {

        SHORT("fr.untitled2.alarm.short", 5, TimeUnit.SECONDS),
        MIDDLE("fr.untitled2.alarm.short", 1, TimeUnit.MINUTES),
        LONG("fr.untitled2.alarm.short", 30, TimeUnit.MINUTES),
        VERY_LONG("fr.untitled2.alarm.short", 1, TimeUnit.HOURS);

        private String intentName;

        private int frequency;

        private TimeUnit timeUnit;

        private Scheduling(String intentName, int frequency, TimeUnit timeUnit) {
            this.intentName = intentName;
            this.frequency = frequency;
            this.timeUnit = timeUnit;
        }

        public Intent getIntent() {
            return new Intent(intentName);
        }

        public long getFrequencyInMillis() {
            if (timeUnit.equals(TimeUnit.SECONDS)) {
                return frequency * 1000L;
            } else if (timeUnit.equals(TimeUnit.MINUTES)) {
                return frequency * 60 * 1000L;
            } else if (timeUnit.equals(TimeUnit.HOURS)) {
                return frequency * 3600 * 1000L;
            }
            return 24 * 3600 * 1000L;
        }

        public boolean mustRun(LocalDateTime lasRunDate) {
            if (lasRunDate == null) return true;
            LocalDateTime now = DateTimeUtils.getCurrentDateTimeInUTC();
            if (timeUnit.equals(TimeUnit.SECONDS)) {
                return lasRunDate.isBefore(now.minusSeconds(frequency));
            } else if (timeUnit.equals(TimeUnit.MINUTES)) {
                return lasRunDate.isBefore(now.minusMinutes(frequency));
            } else if (timeUnit.equals(TimeUnit.HOURS)) {
                return lasRunDate.isBefore(now.minusHours(frequency));
            }
            return false;
        }

        public int getFrequency() {
            return frequency;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public static Optional<Scheduling> getFromIntent(Intent intent) {
            for (Scheduling scheduling : values()) {
                if (intent.getAction().equals(scheduling.intentName)) return Optional.of(scheduling);
            }
            return Optional.absent();
        }

        public static IntentFilter getIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            for (Scheduling scheduling : values()) {
                intentFilter.addAction(scheduling.intentName);
            }
            return intentFilter;
        }


    }

}
