package fr.untitled2.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.utils.DistanceUtils;
import org.javatuples.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.util.TimeZone;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/9/13
 * Time: 2:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class LogRecorder extends Service {

    public static final String BROADCAST_ACTION = "fr.untitled2.gps.update";

    private LocationListener gpsLocationListener;

    private LocationListener networkLocationListener;

    private Preferences preferences;

    private DbHelper dbHelper;

    public LogRecorder() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogRecording logRecording = new LogRecording();
        logRecording.setDateTimeZone(TimeZone.getDefault().getID());
        logRecording.setName(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(DateTime.now()));
        this.preferences = PreferencesUtils.getPreferences(this);
        dbHelper = new DbHelper(getApplicationContext(), preferences);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationListener();

        return START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        Binder binder = new Binder();
        startLocationListener();
        return binder;
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        stopLocationListener();
    }

    private void startLocationListener() {
        if (!dbHelper.hasCurrentLog()) return;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsLocationListener = GetGPSListener();
        networkLocationListener = GetThirdPartyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, preferences.getFrequency(), preferences.getMinDistance(), gpsLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, preferences.getFrequency(), preferences.getMinDistance(), networkLocationListener);
        Toast.makeText(this, preferences.getTranslation(I18nConstants.log_started), Toast.LENGTH_SHORT).show();
    }

    public void stopLocationListener() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(gpsLocationListener);
        Toast.makeText(this, preferences.getTranslation(I18nConstants.log_stopped), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    LocationListener GetThirdPartyLocationListener() {

        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    LocalDateTime currentDate = DateTime.now().toDateTime(DateTimeZone.UTC).toLocalDateTime();
                    Optional<LogRecording> currentLogRecordingOptional = dbHelper.getCurrentLog();
                    if (!currentLogRecordingOptional.isPresent()) return;

                    LogRecording currentLogRecording = currentLogRecordingOptional.get();


                    double distance = preferences.getMinDistance() * 2;
                    if (currentLogRecording.getLastLogRecord() != null) DistanceUtils.getDistance(new Pair<Double, Double>(currentLogRecording.getLastLogRecord().getLatitude(), currentLogRecording.getLastLogRecord().getLongitude()), new Pair<Double, Double>(location.getLatitude(), location.getLongitude()));
                    if (currentLogRecording.getLastLogRecord() == null || new Period(currentLogRecording.getLastLogRecord().getDateTime(), currentDate).toStandardDuration().getMillis() >= 10 * preferences.getFrequency() && new Double(distance).floatValue() > preferences.getMinDistance()) {
                        LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                        logRecord.setDateTime(currentDate);
                        logRecord.setLatitude(location.getLatitude());
                        logRecord.setLongitude(location.getLongitude());
                        dbHelper.addRecordToCurrentLog(logRecord);
                    }
                } catch (Throwable t) {
                    Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

    }

    LocationListener GetGPSListener() {

        return new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                try {
                    LocalDateTime currentDate = DateTime.now().toDateTime(DateTimeZone.UTC).toLocalDateTime();
                    LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                    logRecord.setDateTime(currentDate);
                    logRecord.setLatitude(location.getLatitude());
                    logRecord.setLongitude(location.getLongitude());
                    dbHelper.addRecordToCurrentLog(logRecord);
                } catch (Throwable t) {
                    Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

    }

}
