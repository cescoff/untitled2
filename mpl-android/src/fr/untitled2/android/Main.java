package fr.untitled2.android;

import android.app.AlertDialog;
import android.content.*;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import fr.untitled2.android.service.LogRecorder;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.service.SchedulingService;
import fr.untitled2.android.service.SensorListener;
import fr.untitled2.android.service.task.*;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.sqlilite.ErrorReport;
import fr.untitled2.android.sqlilite.KnownLocationWithDatetime;
import fr.untitled2.android.sqlilite.WifiDetectionHolder;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.DateTimeUtils;
import fr.untitled2.common.utils.DistanceUtils;
import fr.untitled2.common.utils.NumberFormattingUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;


public class Main extends MenuActivity {

    public static final String log_started = "fr.untitled2.logStarted";

    private Preferences preferences;

    private DbHelper dbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.preferences = getPreferences();

            this.dbHelper = new DbHelper(getApplicationContext(), preferences);

/*
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            Sensor temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            if (temperatureSensor == null) temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
            if (temperatureSensor != null) {
                sensorManager.registerListener(new SensorListener(dbHelper, SensorListener.SensorType.temperature), temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            if (pressureSensor != null) {
                sensorManager.registerListener(new SensorListener(dbHelper, SensorListener.SensorType.pressure), pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

*/
        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            Log.e(getLocalClassName(), stackTrace);
        }
    }

    @Override
    protected String getPageTitle(Preferences preferences) {
        return preferences.getTranslation(I18nConstants.main_title);
    }

    @Override
    protected boolean displayMenuBar() {
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.preferences = getPreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean(log_started, dbHelper.hasCurrentLog());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            preferences = getPreferences();
            initHomeView();
            ManageRecordingService().execute(0L);

            startService(new Intent(getApplicationContext(), SchedulingService.class));

            if (!preferences.isConnected()) {
                getNotConnectedAlert().show();
                return;
            }

            LocalBroadcastManager.getInstance(this).registerReceiver(viewUpdaterMessageReciever, new IntentFilter("fr.untitled2.MainViewUpdater"));
        } catch (Throwable t) {
            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
    }


    AsyncTask<Long, Integer, Integer> ManageRecordingService() {

        return new AsyncTask<Long, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Long... params) {
                Looper.prepare();
                String wifiSSID = getWifiSSID();
                if (StringUtils.isNotEmpty(wifiSSID)) {
                    if (isKnownWIFI(wifiSSID)) {
                        stopLogService();
                    } else {
                        Optional<WifiDetectionHolder> wifiDetectionHolderOptional = dbHelper.getDetectedWifiBySSID(wifiSSID);
                        if (wifiDetectionHolderOptional.isPresent() && wifiDetectionHolderOptional.get().isStable()) {
                            Looper.loop();
                            return 1;
                        }
                    }
                } else {
                    if (preferences.isAuto()) {
                        startLogService();
                    }
                }
                Looper.loop();
                return 0;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
            }

            @Override
            protected void onPostExecute(Integer integer) {
                if (integer == 1) {
                    markCurrentWifi(getWifiSSID());
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(viewUpdaterMessageReciever);
    }

    private Preferences getPreferences() {
        return PreferencesUtils.getPreferences(this);
    }


    private void initHomeView() {
        setContentView(R.layout.main);
        // get all the view components
        ImageButton logstopstart = (ImageButton) findViewById(R.id.ImageButtonLogStopStart);
        Button logstopstarttext = (Button) findViewById(R.id.ButtonLogStopStart);

        // hook up all the buttons with a table color change on click listener
        if (!isLogServiceAlive()) {
            logstopstart.setOnClickListener(OnClickStartNewLog());
            logstopstart.setContentDescription(preferences.getTranslation(I18nConstants.main_start_log));
            logstopstarttext.setText(preferences.getTranslation(I18nConstants.main_start_log));
        } else {
            logstopstart.setOnClickListener(OnClickStopCurrentLog());
            logstopstart.setContentDescription(preferences.getTranslation(I18nConstants.logstart_stoplog));
            logstopstarttext.setText(preferences.getTranslation(I18nConstants.logstart_stoplog));
        }

        if (isLogServiceAlive()) logstopstart.setImageResource(R.drawable.stop);
        else logstopstart.setImageResource(R.drawable.disc);

        initLogView();
    }

    private BroadcastReceiver viewUpdaterMessageReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
//                String ssid = getWifiSSID();
/*                if (StringUtils.isEmpty(ssid) || isKnownWIFI(ssid))*/ initHomeView();
                ManageRecordingService().execute(0L);
            } catch (Throwable t) {
                try {
                    dbHelper.addErrorReport(ErrorReport.fromThrowable(getClass(), "Error occured while loading home view from event", t));
                } catch (Throwable tt) {
                }
            }
        }
    };

    private boolean isLogServiceAlive() {
        return dbHelper.hasCurrentLog();
    }

    private void initLogView() {
        try {
            TextView logName = (TextView) findViewById(R.id.LogName);
            TextView timeZone = (TextView) findViewById(R.id.TimeZone);
            TextView pointCount = (TextView) findViewById(R.id.PointCount);
            TextView lastLatitude = (TextView) findViewById(R.id.LastLatitude);
            TextView lastLongitude = (TextView) findViewById(R.id.LastLongitude);
            TextView lastAltitude = (TextView) findViewById(R.id.LastAltitude);
            TextView lastPointDate = (TextView) findViewById(R.id.LastPointDate);
            TextView distance = (TextView) findViewById(R.id.Distance);

            TextView logNameLabel = (TextView) findViewById(R.id.LogNameLabel);
            TextView timeZoneLabel = (TextView) findViewById(R.id.TimeZoneLabel);
            TextView pointCountLabel = (TextView) findViewById(R.id.PointCountLabel);
            TextView lastLatitudeLabel = (TextView) findViewById(R.id.LastLatitudeLabel);
            TextView lastLongitudeLabel = (TextView) findViewById(R.id.LastLongitudeLabel);
            TextView lastAltitudeLabel = (TextView) findViewById(R.id.LastAltitudeLabel);
            TextView lastPointDateLabel = (TextView) findViewById(R.id.LastPointDateLabel);
            TextView distanceLabel = (TextView) findViewById(R.id.DistanceLabel);

            TextView lastKnownLocationName = (TextView) findViewById(R.id.LastKnownLocationName);
            TextView lastKnownLocationDate = (TextView) findViewById(R.id.LastKnownLocationDate);
            TextView lastKnownLocationNameForDistance = (TextView) findViewById(R.id.LastKnownLocationNameForDistance);
            TextView lastKnownLocationDistance = (TextView) findViewById(R.id.LastKnownLocationDistance);

            lastKnownLocationName.setVisibility(View.GONE);
            lastKnownLocationDate.setVisibility(View.GONE);
            lastKnownLocationNameForDistance.setVisibility(View.GONE);
            lastKnownLocationDistance.setVisibility(View.GONE);

            logNameLabel.setText(preferences.getTranslation(I18nConstants.logstart_name) + " : ");
            timeZoneLabel.setText(preferences.getTranslation(I18nConstants.logstart_time_zone) + " : ");
            pointCountLabel.setText(preferences.getTranslation(I18nConstants.logstart_point_count) + " : ");
            lastLatitudeLabel.setText(preferences.getTranslation(I18nConstants.logstart_latitude) + " : ");
            lastLongitudeLabel.setText(preferences.getTranslation(I18nConstants.logstart_longitude) + " : ");
            lastAltitudeLabel.setText(preferences.getTranslation(I18nConstants.logstart_altitude) + " : ");
            lastPointDateLabel.setText(preferences.getTranslation(I18nConstants.logstart_date) + " : ");
            distanceLabel.setText(preferences.getTranslation(I18nConstants.logstart_distance) + " : ");

            Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();
            if (isLogServiceAlive() && logRecordingOptional.isPresent()) {

                LogRecording logRecording = logRecordingOptional.get();
                logNameLabel.setVisibility(View.VISIBLE);
                timeZoneLabel.setVisibility(View.VISIBLE);

                logName.setText(logRecording.getName());
                logName.setVisibility(View.VISIBLE);
                timeZone.setText(logRecording.getDateTimeZone());
                timeZone.setVisibility(View.VISIBLE);

                LogRecording.LogRecord lastPoint = logRecording.getLastLogRecord();
                if (lastPoint != null) {
                    pointCountLabel.setVisibility(View.VISIBLE);
                    pointCount.setVisibility(View.VISIBLE);
                    pointCount.setText(logRecording.getPointCount() + "");

                    lastLatitude.setText(NumberFormattingUtils.toLatitudeInDegreesMinutesSeconds(lastPoint.getLatitude()));
                    lastLatitude.setVisibility(View.VISIBLE);
                    lastLatitudeLabel.setVisibility(View.VISIBLE);

                    lastLongitude.setText(NumberFormattingUtils.toLongitudeInDegreesMinutesSeconds(lastPoint.getLongitude()));
                    lastLongitude.setVisibility(View.VISIBLE);
                    lastLongitudeLabel.setVisibility(View.VISIBLE);

                    if (lastPoint.getAltitude() > 0) {
                        lastAltitude.setText(NumberFormattingUtils.toDisplayableDouble(lastPoint.getAltitude()));
                        lastAltitude.setVisibility(View.VISIBLE);
                        lastAltitudeLabel.setVisibility(View.VISIBLE);
                    } else {
                        lastAltitude.setVisibility(View.GONE);
                        lastAltitudeLabel.setVisibility(View.GONE);
                    }

                    lastPointDate.setText(preferences.getDateTimeFormatter().print(DateTimeUtils.getDateTimeInTimeZone(lastPoint.getDateTime(), logRecording.getDateTimeZone())));
                    lastPointDateLabel.setVisibility(View.VISIBLE);
                    lastPointDate.setVisibility(View.VISIBLE);

                    if (logRecording.getDistance() > 0) {
                        distance.setVisibility(View.VISIBLE);
                        distanceLabel.setVisibility(View.VISIBLE);
                        distance.setText(" " + NumberFormattingUtils.toDistance(logRecording.getDistance(), NumberFormattingUtils.DistanceUnit.metric));
                    } else {
                        distance.setVisibility(View.GONE);
                        distanceLabel.setVisibility(View.GONE);
                    }

                } else {
                    pointCount.setVisibility(View.GONE);
                    lastLatitude.setVisibility(View.GONE);
                    lastLongitude.setVisibility(View.GONE);
                    lastAltitude.setVisibility(View.GONE);
                    lastPointDate.setVisibility(View.GONE);
                    distance.setVisibility(View.GONE);

                    pointCountLabel.setVisibility(View.GONE);
                    lastLatitudeLabel.setVisibility(View.GONE);
                    lastLongitudeLabel.setVisibility(View.GONE);
                    lastAltitudeLabel.setVisibility(View.GONE);
                    lastPointDateLabel.setVisibility(View.GONE);
                    distanceLabel.setVisibility(View.GONE);
                }

                Optional<KnownLocationWithDatetime> lasKnownLocationWithDatetimeOptional = dbHelper.getLastKnownLocation();

                if (lasKnownLocationWithDatetimeOptional.isPresent()) {
                    KnownLocationWithDatetime knownLocationWithDatetime = lasKnownLocationWithDatetimeOptional.get();
                    lastKnownLocationName.setVisibility(View.VISIBLE);
                    lastKnownLocationDate.setVisibility(View.VISIBLE);

                    lastKnownLocationName.setText(knownLocationWithDatetime.getKnownLocation().getName());
                    if (logRecording.getLastLogRecord() != null) {
                        Optional<KnownLocation> currentKnownLocation = DistanceUtils.getKnownLocation(logRecording.getLastLogRecord().getLatitudeAndLongitude(), preferences.getKnownLocations());
                        if (currentKnownLocation.isPresent()) {
                            lastKnownLocationDate.setText(preferences.getDateTimeFormatter().print(knownLocationWithDatetime.getPointDate().toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID(logRecording.getDateTimeZone()))));
                        } else {
                            Period tripDuration = new Period(knownLocationWithDatetime.getPointDate(), DateTimeUtils.getCurrentDateTimeInUTC());
                            tripDuration = tripDuration.withMillis(0);
                            if (tripDuration.getHours() > 0) tripDuration = tripDuration.withSeconds(0);
                            PeriodFormatter periodFormatter = PeriodFormat.wordBased(preferences.getUserLocale());
                            lastKnownLocationDate.setText(periodFormatter.print(tripDuration));

                            if (knownLocationWithDatetime.getDistance() > 0) {
                                lastKnownLocationNameForDistance.setVisibility(View.VISIBLE);
                                lastKnownLocationDistance.setVisibility(View.VISIBLE);

                                lastKnownLocationNameForDistance.setText(knownLocationWithDatetime.getKnownLocation().getName());
                                lastKnownLocationDistance.setText(NumberFormattingUtils.toDistance(knownLocationWithDatetime.getDistance(), NumberFormattingUtils.DistanceUnit.metric));
                            }
                        }
                    } else {
                        lastKnownLocationDate.setText(preferences.getDateTimeFormatter().print(DateTimeUtils.getDateTimeInTimeZone(knownLocationWithDatetime.getPointDate(), logRecording.getDateTimeZone())));
                    }
                } else {
                    lastKnownLocationName.setVisibility(View.GONE);
                    lastKnownLocationDate.setVisibility(View.GONE);
                    lastKnownLocationNameForDistance.setVisibility(View.GONE);
                    lastKnownLocationDistance.setVisibility(View.GONE);
                }

            } else {
                logNameLabel.setVisibility(View.GONE);
                timeZoneLabel.setVisibility(View.GONE);
                pointCountLabel.setVisibility(View.GONE);
                lastLatitudeLabel.setVisibility(View.GONE);
                lastLongitudeLabel.setVisibility(View.GONE);
                lastAltitudeLabel.setVisibility(View.GONE);
                lastPointDateLabel.setVisibility(View.GONE);
                distance.setVisibility(View.GONE);

                logName.setVisibility(View.GONE);
                timeZone.setVisibility(View.GONE);
                pointCount.setVisibility(View.GONE);
                lastLatitude.setVisibility(View.GONE);
                lastLongitude.setVisibility(View.GONE);
                lastAltitude.setVisibility(View.GONE);
                lastPointDate.setVisibility(View.GONE);

                lastKnownLocationName.setVisibility(View.GONE);
                lastKnownLocationDate.setVisibility(View.GONE);
            }

        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            Log.e(getClass().getName(), stackTrace);
        }
    }

    private String getWifiSSID() {
        return NetUtils.getCurrentWifiSSID(this);
    }

    private boolean isKnownWIFI(String wifiSSID) {
        if (StringUtils.isEmpty(wifiSSID)) return false;
        for (KnownLocation knownLocation : preferences.getKnownLocations()) {
            if (knownLocation.getWifiSSIDs().contains(wifiSSID)) {
                return true;
            }
        }
        return false;
    }

    private void markCurrentWifi(final String ssid) {
        try {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(preferences.getTranslation(I18nConstants.knownlocationlist_add_ssid_alert_label));
            alertDialogBuilder.setPositiveButton(preferences.getTranslation(I18nConstants.loglist_uploadyes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    gotToKnownLocationList(false, ssid);
                }
            });
            alertDialogBuilder.setNegativeButton(preferences.getTranslation(I18nConstants.loglist_uploadno), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialogBuilder.create().show();
        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            Log.e(getLocalClassName(), stackTrace);
        }
    }

    private void gotToKnownLocationList(boolean manageMode, String ssid) {
        Intent intent = new Intent(getApplicationContext(), KnownLocationList.class);
        intent.putExtra("manageMode", manageMode);
        intent.putExtra("ssid", ssid);
        startActivity(intent);
    }

    private void startLogService() {
        Intent serviceIntent = new Intent(getApplicationContext(), LogRecorder.class);
        if (!dbHelper.hasCurrentLog()) dbHelper.createNewLogInProgress();
        startService(serviceIntent);
//        initHomeView();
    }

    private void stopLogService() {
        Intent stopServiceIntent = new Intent(this, LogRecorder.class);
        stopService(stopServiceIntent);
//        initHomeView();
    }

    View.OnClickListener OnClickChangeToSettings()
    {
        return new View.OnClickListener() {
            public void onClick(View view) {
                changeToSettingsView();
            }
        };
    }

    private void changeToSettingsView() {
        Intent intent = new Intent(getApplicationContext(), Settings.class);
        startActivity(intent);
    }

    View.OnClickListener OnClickToFilmTool() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FilmTool.class);
                startActivity(intent);
            }
        };

    }

    View.OnClickListener OnClickChangeToLogList()
    {
        return new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LogList.class);
                startActivity(intent);
            }
        };
    }

    View.OnClickListener OnClickStartNewLog() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogService();
            }
        };
    }

    View.OnClickListener OnClickStopCurrentLog() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    stopLogService();
                    dbHelper.markCurrentLogAsToBeSent();
                    initLogView();
                } catch (Exception e) {
                    Log.e(getLocalClassName(), Throwables.getStackTraceAsString(e));
                }
            }
        };

    }

    private AlertDialog getNotConnectedAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(preferences.getTranslation(I18nConstants.not_connected_alert_title));
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeToSettingsView();
            }
        });
        return alertDialogBuilder.create();
    }

}
