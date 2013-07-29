package fr.untitled2.android.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Throwables;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/29/13
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetUtils {

    private static HttpHost httpHost = new HttpHost(AppEngineOAuthClient.appHost, 443, "https");;


    public static boolean isConnected(Activity activity) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return pingRemoteApp();
            }
            netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return pingRemoteApp();
            }
        } catch (Throwable t) {
            Log.e(activity.getLocalClassName(), Throwables.getStackTraceAsString(t));
            Toast.makeText(activity.getApplicationContext(), "Unable to determine network status", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private static boolean pingRemoteApp() {
        try {
            HttpGet httpGet = new HttpGet("/api/ping");

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpHost, httpGet);
            String response = IOUtils.toString(httpResponse.getEntity().getContent());
            if ("OK".equals(response)) return true;
        } catch (Throwable t) {
        }

        return false;
    }

}
