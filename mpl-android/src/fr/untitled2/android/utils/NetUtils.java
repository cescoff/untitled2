package fr.untitled2.android.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Throwables;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.DateTimeUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.LocalDateTime;

import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/29/13
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetUtils {

    private static HttpHost httpHost = new HttpHost(AppEngineOAuthClient.appHost, 443, "https");;

    private static NetUtils instance;

    private static final ExecutorService threapool = Executors.newCachedThreadPool();

    private LocalDateTime lastPingDate;

    private NetUtils() {
    }

    private static synchronized NetUtils getInstance() {
        if (instance == null) {
            instance = new NetUtils();
        }
        return instance;
    }

    public static void notifyReconnected() {
        getInstance().lastPingDate = null;
    }

    public static String getCurrentWifiSSID(Activity activity) {
        if (!isConnected(activity)) return null;
        return getWifiSSIDWithoutConnexionCheck(activity);
    }

    public static String getWifiSSIDWithoutConnexionCheck(Activity activity) {
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    public static String getWifiSSIDWithoutConnexionCheck(Service service) {
        WifiManager wifiManager = (WifiManager) service.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    public static String getCurrentWifiSSID(Service service) {
        if (!isConnected(service)) return null;
        WifiManager wifiManager = (WifiManager) service.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    public static boolean isWifiConnected(Activity activity) {
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return netInfo != null && netInfo.isConnected();
    }

    public static boolean isWifiConnected(Service service) {
        ConnectivityManager cm =
                (ConnectivityManager) service.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return netInfo != null && netInfo.isConnected();
    }

    public static boolean isConnected(Activity activity) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            return isConnected(cm);
        } catch (Throwable t) {
            Log.e(activity.getLocalClassName(), Throwables.getStackTraceAsString(t));
            Toast.makeText(activity.getApplicationContext(), "Unable to determine network status", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public static boolean isConnected(Service service) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) service.getSystemService(Context.CONNECTIVITY_SERVICE);
            return isConnected(cm);
        } catch (Throwable t) {
            Log.e(service.getClass().getName(), Throwables.getStackTraceAsString(t));
        }
        return false;
    }

    private static boolean isConnected(ConnectivityManager connectivityManager) {
        if (connectivityManager == null) return false;
        NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (netInfo != null && netInfo.isConnected()) {
            return getInstance().pingRemoteApp();
        }
        netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (netInfo != null && netInfo.isConnected()) {
            return getInstance().pingRemoteApp();
        }
        return false;
    }

    public static boolean forcePingRemoteApp() {
        try {
            Future<Boolean> networkstatus = threapool.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    HttpGet httpGet = new HttpGet("/api/ping");

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse httpResponse = httpClient.execute(httpHost, httpGet);
                    String response = IOUtils.toString(httpResponse.getEntity().getContent());
                    if ("OK".equals(response)) {
                        return true;
                    }
                    return false;
                }
            });
            return networkstatus.get(2000, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
        }

        return false;
    }

    private boolean pingRemoteApp() {
        try {
            if (lastPingDate != null && lastPingDate.minusMinutes(10).isBefore(DateTimeUtils.getCurrentDateTimeInUTC())) return true;

            if (forcePingRemoteApp()) {
                lastPingDate = DateTimeUtils.getCurrentDateTimeInUTC();
                return true;
            }
        } catch (Throwable t) {
        }

        return false;
    }

}
