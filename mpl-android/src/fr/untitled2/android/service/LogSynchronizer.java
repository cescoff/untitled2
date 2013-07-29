package fr.untitled2.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/18/13
 * Time: 4:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class LogSynchronizer extends Service {

    private Preferences preferences;

    private Timer timer;

    private DbHelper dbHelper;

    private AppEngineOAuthClient appEngineOAuthClient;

    @Override
    public IBinder onBind(Intent intent) {
        Binder binder = new Binder();
        startService();
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        timer.cancel();
    }

    private void startService() {
        preferences = PreferencesUtils.getPreferences(this);
        dbHelper = new DbHelper(getApplicationContext(), preferences);

        appEngineOAuthClient = new AppEngineOAuthClient(preferences.getOauth2Key(), preferences.getOauthSecret());

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Optional<LogRecording> currentLogRecordingOptional = dbHelper.getCurrentLog();
                    if (!currentLogRecordingOptional.isPresent()) return;

                    LogRecording currentLog = currentLogRecordingOptional.get();
                    LocalDateTime currentDate = LocalDateTime.now();
                    if (currentDate.getHourOfDay() == preferences.getAutoModeSyncHourOfDay() && new Period(currentLog.getStartPointDate(), currentDate).toStandardDuration().getMillis() >= 24 * 3600 * 1000) {
                        dbHelper.markCurrentLogAsToBeSent();
                    }
                    if (isConnected()) {
                        Collection<LogRecording> pendingLogRecordings = dbHelper.getLogRecordingToBeSentToCloud();
                        for (LogRecording pendingLogRecording : pendingLogRecordings) {
                            appEngineOAuthClient.pushLogRecording(pendingLogRecording);
                            dbHelper.markLogRecordingAsSynchronizedWithCloud(pendingLogRecording.getId());
                        }
                    }
                } catch (Throwable t) {
                    Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
                }
            }
        }, 0, 5 * 60 * 1000L);

    }

    private boolean isConnected() {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
            netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
        } catch (Throwable t) {
            Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
            Toast.makeText(getApplicationContext(), "Unable to determine network status", Toast.LENGTH_LONG).show();
        }
        return false;
    }

}
