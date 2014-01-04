package fr.untitled2.android.service.task;

import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import fr.untitled2.android.service.LogRecorder;
import fr.untitled2.android.service.SchedulingService;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.sqlilite.ErrorReport;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.utils.DateTimeUtils;
import org.joda.time.LocalDateTime;

import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 11/12/13
 * Time: 21:03
 * To change this template use File | Settings | File Templates.
 */
public abstract class ITask implements Callable<Boolean> {

    private DbHelper dbHelper;

    private Preferences preferences;

    private SchedulingService service;

    private LocalDateTime lasRunDate;

    @Override
    public Boolean call() throws Exception {
        lasRunDate = DateTimeUtils.getCurrentDateTimeInUTC();
        try {
            executeTask(dbHelper, preferences);
            return true;
        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            sendNotificationToUser("[ERROR]" + getClass().getSimpleName(), stackTrace, SchedulingService.MessageType.error, true);
            return false;
        }
    }

    public void init() {
        lasRunDate = DateTimeUtils.getCurrentDateTimeInUTC();
    }

    protected abstract void executeTask(DbHelper dbHelper, Preferences preferences) throws Exception;

    public abstract SchedulingService.Scheduling getScheduling();

    public abstract boolean isAvailableOnSleepMode();

    public LocalDateTime getLasRunDate() {
        return lasRunDate;
    }

    public void sendNotificationToUser(String title, String message, SchedulingService.MessageType messageType, boolean vibrate) {
        service.sendNotificationToUser(title, message, messageType, vibrate);
    }


    protected void startRecorderService() {
        service.startRecorderService();
    }

    protected void stopRecorderService() {
        service.stopRecorderService();
    }

    public boolean isConnected() {
        return NetUtils.isConnected(service);
    }

    protected Optional<String> getWIFISSID() {
        return service.getWIFISSID();
    }

    protected void updatePreferences(Preferences newPreferences) {
        this.preferences = newPreferences;
        service.updatePreferences(newPreferences);
    }

    public void setService(SchedulingService service) {
        this.service = service;
    }

    public void setDbHelper(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void setPreferences(Preferences preferences) {
        synchronized (preferences) {
            this.preferences = preferences;
        }
    }

    protected void updateMainView() {
        service.updateMainView();
    }

}
