package fr.untitled2.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import fr.untitled2.android.service.LogRecorder;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.service.LogSynchronizer;
import fr.untitled2.android.service.LogUploader;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.LogRecordingConstants;
import fr.untitled2.utils.CollectionUtils;
import org.joda.time.DateTimeZone;

import java.io.FileNotFoundException;
import java.util.List;


public class Main extends Activity {

    public static final String log_started = "fr.untitled2.logStarted";

    private Preferences preferences;

    private DbHelper dbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.preferences = getPreferences();

            if (!preferences.isConnected()) {
                getNotConnectedAlert().show();
                return;
            }

            this.dbHelper = new DbHelper(getApplicationContext(), preferences);
            if (preferences.isAuto() && !dbHelper.hasCurrentLog()) {
                startLogService();
            }

            if (preferences.isAuto()) {
                LogUploader.getInstance(dbHelper, preferences).start(this);
            }

        } catch (Throwable t) {
            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.preferences = getPreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.preferences = getPreferences();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            initHomeView();
        } catch (Throwable t) {
            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
        this.preferences = getPreferences();
    }

    private Preferences getPreferences() {
        return PreferencesUtils.getPreferences(this);
    }

    private void initHomeView() {
        setContentView(R.layout.main);
        // get all the view components
        ImageButton settings = (ImageButton) findViewById(R.id.ButtonSettings);
        ImageButton logstopstart = (ImageButton) findViewById(R.id.ImageButtonLogStopStart);
        Button logstopstarttext = (Button) findViewById(R.id.ButtonLogStopStart);
        ImageButton loglist = (ImageButton) findViewById(R.id.ButtonLogList);
        ImageButton filmToolButton = (ImageButton) findViewById(R.id.ButtonFilmTools);
        if (!preferences.isFilmToolEnabled()) filmToolButton.setVisibility(View.GONE);
        else filmToolButton.setOnClickListener(OnClickToFilmTool());

        settings.setContentDescription(preferences.getTranslation(I18nConstants.main_settings));
        loglist.setContentDescription(preferences.getTranslation(I18nConstants.main_list_logs));

        // hook up all the buttons with a table color change on click listener
        settings.setOnClickListener(OnClickChangeToSettings());
        if (!isLogServiceAlive()) {
            logstopstart.setOnClickListener(OnClickStartNewLog());
            logstopstart.setContentDescription(preferences.getTranslation(I18nConstants.main_start_log));
            logstopstarttext.setText(preferences.getTranslation(I18nConstants.main_start_log));
        } else {
            logstopstart.setOnClickListener(OnClickStopCurrentLog());
            logstopstart.setContentDescription(preferences.getTranslation(I18nConstants.logstart_stoplog));
            logstopstarttext.setText(preferences.getTranslation(I18nConstants.logstart_stoplog));
        }
        loglist.setOnClickListener(OnClickChangeToLogList());

        if (isLogServiceAlive()) logstopstart.setImageResource(R.drawable.stop);
        else logstopstart.setImageResource(R.drawable.disc);

        TextView textView = (TextView) findViewById(R.id.Welcome);
        textView.setText(preferences.getTranslation(I18nConstants.main_welcome));
        initLogView();
    }

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
            TextView lastPointDate = (TextView) findViewById(R.id.LastPointDate);

            TextView logNameLabel = (TextView) findViewById(R.id.LogNameLabel);
            TextView timeZoneLabel = (TextView) findViewById(R.id.TimeZoneLabel);
            TextView pointCountLabel = (TextView) findViewById(R.id.PointCountLabel);
            TextView lastLatitudeLabel = (TextView) findViewById(R.id.LastLatitudeLabel);
            TextView lastLongitudeLabel = (TextView) findViewById(R.id.LastLongitudeLabel);
            TextView lastPointDateLabel = (TextView) findViewById(R.id.LastPointDateLabel);

            logNameLabel.setText(preferences.getTranslation(I18nConstants.logstart_name) + " : ");
            timeZoneLabel.setText(preferences.getTranslation(I18nConstants.logstart_time_zone) + " : ");
            pointCountLabel.setText(preferences.getTranslation(I18nConstants.logstart_point_count) + " : ");
            lastLatitudeLabel.setText(preferences.getTranslation(I18nConstants.logstart_latitude) + " : ");
            lastLongitudeLabel.setText(preferences.getTranslation(I18nConstants.logstart_longitude) + " : ");
            lastPointDateLabel.setText(preferences.getTranslation(I18nConstants.logstart_date) + " : ");

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

                    lastLatitude.setText(lastPoint.getLatitude() + "");
                    lastLatitude.setVisibility(View.VISIBLE);
                    lastLatitudeLabel.setVisibility(View.VISIBLE);

                    lastLongitude.setText(lastPoint.getLongitude() + "");
                    lastLongitude.setVisibility(View.VISIBLE);
                    lastLongitudeLabel.setVisibility(View.VISIBLE);

                    lastPointDate.setText(preferences.getDateTimeFormatter().print(lastPoint.getDateTime().toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID(logRecording.getDateTimeZone()))));
                    lastPointDateLabel.setVisibility(View.VISIBLE);
                    lastPointDate.setVisibility(View.VISIBLE);
                } else {
                    pointCount.setVisibility(View.GONE);
                    lastLatitude.setVisibility(View.GONE);
                    lastLongitude.setVisibility(View.GONE);
                    lastPointDate.setVisibility(View.GONE);

                    pointCountLabel.setVisibility(View.GONE);
                    lastLatitudeLabel.setVisibility(View.GONE);
                    lastLongitudeLabel.setVisibility(View.GONE);
                    lastPointDateLabel.setVisibility(View.GONE);
                }
            } else {
                logNameLabel.setVisibility(View.GONE);
                timeZoneLabel.setVisibility(View.GONE);
                pointCountLabel.setVisibility(View.GONE);
                lastLatitudeLabel.setVisibility(View.GONE);
                lastLongitudeLabel.setVisibility(View.GONE);
                lastPointDateLabel.setVisibility(View.GONE);

                logName.setVisibility(View.GONE);
                timeZone.setVisibility(View.GONE);
                pointCount.setVisibility(View.GONE);
                lastLatitude.setVisibility(View.GONE);
                lastLongitude.setVisibility(View.GONE);
                lastPointDate.setVisibility(View.GONE);
            }

        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            Log.e(getClass().getName(), stackTrace);
        }
    }



    private void startLogService() {
        Intent serviceIntent = new Intent(getApplicationContext(), LogRecorder.class);
        dbHelper.createNewLogInProgress();
        startService(serviceIntent);
        initHomeView();
    }

    private void stopLogService() {
        Intent stopServiceIntent = new Intent(this, LogRecorder.class);
        stopService(stopServiceIntent);
        initHomeView();
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
