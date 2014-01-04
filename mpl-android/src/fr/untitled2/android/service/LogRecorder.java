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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.sqlilite.KnownLocationWithDatetime;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.utils.DateTimeUtils;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.common.utils.DistanceUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;
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
        this.preferences = PreferencesUtils.getPreferences(this);
        LogRecording logRecording = new LogRecording();
        logRecording.setDateTimeZone(TimeZone.getDefault().getID());
        logRecording.setName(preferences.getDateTimeFormatter().print(DateTime.now()));
        dbHelper = new DbHelper(getApplicationContext(), preferences);

        if (!dbHelper.hasCurrentLog()) return;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsLocationListener = GetGPSListener();
        networkLocationListener = GetThirdPartyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, preferences.getFrequency(), preferences.getMinDistance(), gpsLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, preferences.getFrequency(), preferences.getMinDistance(), networkLocationListener);
        Toast.makeText(this, preferences.getTranslation(I18nConstants.log_started), Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(gpsLocationListener);
        Toast.makeText(this, preferences.getTranslation(I18nConstants.log_stopped), Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        Binder binder = new Binder();
        return binder;
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
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
                    LocalDateTime currentDate = DateTimeUtils.getCurrentDateTimeInUTC();
                    Optional<LogRecording> currentLogRecordingOptional = dbHelper.getCurrentLog();
                    if (!currentLogRecordingOptional.isPresent()) return;

                    LogRecording currentLogRecording = currentLogRecordingOptional.get();

                    Optional<KnownLocation> knownLocation = getPointKnownLocation(location.getLatitude(), location.getLongitude(), location.getAltitude(), getPreferences());

                    if (knownLocation.isPresent()) {
                        Optional<KnownLocationWithDatetime> lastKnownLocationOptional = dbHelper.getLastKnownLocation();
                        if (lastKnownLocationOptional.isPresent() && lastKnownLocationOptional.get().equals(knownLocation.get())) {
                            KnownLocationWithDatetime knownLocationWithDatetime = lastKnownLocationOptional.get();
                            knownLocationWithDatetime.setPointDate(DateTimeUtils.getCurrentDateTimeInUTC());
                            dbHelper.updateKnownLocation(knownLocationWithDatetime);
                        }
                    }

                    double distance = preferences.getMinDistance() * 2;
                    if (currentLogRecording.getLastLogRecord() == null || new Period(currentLogRecording.getLastLogRecord().getDateTime(), currentDate).toStandardDuration().getMillis() >= 10 * preferences.getFrequency() && new Double(distance).floatValue() > preferences.getMinDistance()) {
                        LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                        logRecord.setDateTime(currentDate);
                        logRecord.setLatitude(location.getLatitude());
                        logRecord.setLongitude(location.getLongitude());
                        logRecord.setAltitude(location.getAltitude());

                        if (knownLocation.isPresent()) {
                            dbHelper.addKnownLocation(currentDate, knownLocation.get());
                        }

                        if (knownLocation.isPresent()) logRecord.setKnownLocation(knownLocation.get());
                        dbHelper.addRecordToCurrentLog(logRecord, knownLocation);

                    }
                    updateMainView();


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
                    Optional<KnownLocation> knownLocation = getPointKnownLocation(location.getLatitude(), location.getLongitude(), location.getAltitude(), getPreferences());

                    if (knownLocation.isPresent()) {
                        Optional<KnownLocationWithDatetime> lastKnownLocationOptional = dbHelper.getLastKnownLocation();
                        if (lastKnownLocationOptional.isPresent() && lastKnownLocationOptional.get().equals(knownLocation.get())) {
                            KnownLocationWithDatetime knownLocationWithDatetime = lastKnownLocationOptional.get();
                            knownLocationWithDatetime.setPointDate(DateTimeUtils.getCurrentDateTimeInUTC());
                            dbHelper.updateKnownLocation(knownLocationWithDatetime);
                        }
                    }

                    LocalDateTime currentDate = DateTimeUtils.getCurrentDateTimeInUTC();
                    LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                    logRecord.setDateTime(currentDate);
                    logRecord.setLatitude(location.getLatitude());
                    logRecord.setLongitude(location.getLongitude());
                    logRecord.setAltitude(location.getAltitude());

                    if (knownLocation.isPresent()) {
                        dbHelper.addKnownLocation(currentDate, knownLocation.get());
                    }
                    if (knownLocation.isPresent()) {
                        logRecord.setKnownLocation(knownLocation.get());
                    }
                    dbHelper.addRecordToCurrentLog(logRecord, knownLocation);
                    updateMainView();
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

    private void updateMainView() {
        Intent intent = new Intent("fr.untitled2.MainViewUpdater");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private Optional<KnownLocation> getPointKnownLocation(double latitude, double longitude, double altitude, Preferences preferences) {
        if (CollectionUtils.isEmpty(preferences.getKnownLocations())) return Optional.absent();
        return DistanceUtils.getKnownLocation(Triplet.with(latitude, longitude, altitude), preferences.getKnownLocations());
    }

    private Preferences getPreferences() {
        return PreferencesUtils.getPreferences(this);
    }

}
