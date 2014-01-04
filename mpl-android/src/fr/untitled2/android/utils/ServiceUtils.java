package fr.untitled2.android.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import fr.untitled2.android.service.LogRecorder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 15/12/13
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class ServiceUtils {

    public static <T extends Service> boolean isServiceRunning(Activity activity, Class<T> serviceClass) {
        final ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        return isServiceRunning(activityManager, serviceClass);
    }

    public static <T extends Service> boolean isServiceRunning(Service service, Class<T> serviceClass) {
        final ActivityManager activityManager = (ActivityManager) service.getSystemService(Context.ACTIVITY_SERVICE);
        return isServiceRunning(activityManager, serviceClass);
    }

    private static <T extends Service> boolean isServiceRunning(ActivityManager activityManager, Class<T> serviceClass) {
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                return true;
            }
        }
        return false;
    }

}
