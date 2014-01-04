package fr.untitled2.android.service.task;

import fr.untitled2.android.service.SchedulingService;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import org.joda.time.DateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 28/12/13
 * Time: 23:37
 * To change this template use File | Settings | File Templates.
 */
public class KeepAlive extends ITask {

    @Override
    protected void executeTask(DbHelper dbHelper, Preferences preferences) throws Exception {
        sendNotificationToUser(preferences.getDateTimeFormatter().print(DateTime.now()) + " : alive", "Process is alive", SchedulingService.MessageType.keep_alive, false);
    }

    @Override
    public SchedulingService.Scheduling getScheduling() {
        return SchedulingService.Scheduling.MIDDLE;
    }

    @Override
    public boolean isAvailableOnSleepMode() {
        return true;
    }
}
