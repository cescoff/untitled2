package fr.untitled2.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import com.google.common.base.Throwables;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserInfos;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/12/13
 * Time: 6:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class Connect extends MenuActivity {

    public static final String token_validation_url = "tokenValidationUrl";

    public static final String verification_code = "verificationCode";

    private String verificationCode;

    private long logRecordingId;

    private Preferences preferences;

    private String tokenValidationUrl;

    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        Intent intent = getIntent();
        if (intent != null) {
            logRecordingId = intent.getIntExtra(LogList.selected_log_id, -1);
        }
        this.dbHelper = new DbHelper(getApplicationContext(), preferences);

        preferences = PreferencesUtils.getPreferences(this);
    }

    @Override
    protected String getPageTitle(Preferences preferences) {
        return preferences.getTranslation(I18nConstants.connect_title);
    }

    @Override
    protected boolean displayMenuBar() {
        return false;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.dbHelper = new DbHelper(getApplicationContext(), preferences);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(token_validation_url)) tokenValidationUrl = savedInstanceState.getString(token_validation_url);
            if (savedInstanceState.containsKey(LogList.selected_log_id)) logRecordingId = savedInstanceState.getLong(LogList.selected_log_id);
            if (savedInstanceState.containsKey(verification_code)) verificationCode = savedInstanceState.getString(verification_code);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(token_validation_url, tokenValidationUrl);
        outState.putLong(LogList.selected_log_id, logRecordingId);
        outState.putString(verification_code, verificationCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initConnectWebView();
    }

    private void initConnectWebView() {
        try {
            getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
            final AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient();
            String tokenValidationUrl = appEngineOAuthClient.getTokenValidationUrl();

            setContentView(R.layout.connectweb);
            WebView webView = (WebView) findViewById(R.id.ConnectWebView);

            webView.getSettings().setJavaScriptEnabled(true);

            webView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    setProgress(newProgress * 100);
                }

            });

            webView.getSettings().setJavaScriptEnabled(true);
            webView.addJavascriptInterface(new MyJavaScriptInterface(appEngineOAuthClient), "HTMLOUT");
            webView.setWebViewClient(new WebViewClient() {
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Toast.makeText(getApplicationContext(), "Oh no! " + description, Toast.LENGTH_LONG).show();
                    redirectToLogList();
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (StringUtils.startsWith(url, "https://accounts.google.com/o/oauth1/approval")) {
                        view.loadUrl("javascript:window.HTMLOUT.showHTML('<html>'+document.getElementsByTagName('body')[0].innerHTML+'</html>');");
                    }

                }
            });
            webView.loadUrl(tokenValidationUrl);
        } catch (Throwable t) {
            Log.e(getCallingPackage() + getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
    }

    private void redirectToLogList() {
        Intent intent = new Intent(getApplicationContext(), LogList.class);
        startActivity(intent);
    }

    private void redirectToSettings() {
        Intent intent = new Intent(getApplicationContext(), Settings.class);
        startActivity(intent);
    }

    private void savePreferences() {
        PreferencesUtils.setSharedPreferences(this, preferences);
    }


    class MyJavaScriptInterface {

        private AppEngineOAuthClient appEngineOAuthClient;

        MyJavaScriptInterface(AppEngineOAuthClient appEngineOAuthClient) {
            this.appEngineOAuthClient = appEngineOAuthClient;
        }

        public void showHTML(String html) {
            LineIterator lineIterator = new LineIterator(new StringReader(html));
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();
                if (StringUtils.contains(line, "code: <b>")) {
                    verificationCode = StringUtils.substring(line, StringUtils.indexOf(line, "code: <b>") + "code: <b>".length(), StringUtils.indexOf(line, "</b>"));
                } else if (StringUtils.contains(line, "<span id=\"verification_code\">")) {
                    verificationCode = StringUtils.substring(line, StringUtils.indexOf(line, "<span id=\"verification_code\">") + "<span id=\"verification_code\">".length(), StringUtils.indexOf(line, "</span>", StringUtils.indexOf(line, "<span id=\"verification_code\">")));
                }
            }
            if (StringUtils.isNotEmpty(verificationCode)) {
                try {
                    appEngineOAuthClient.validateTokens(verificationCode);
                    preferences.setOauth2Key(appEngineOAuthClient.getAccessToken());
                    preferences.setOauthSecret(appEngineOAuthClient.getTokenSecret());
                    preferences.setTokenDate(LocalDateTime.now());
                    UserInfos userInfos = appEngineOAuthClient.getUserInfos(new UserPreferences(preferences.getDateFormat(), preferences.getUserLocale().getLanguage(), preferences.getCameraTimeZone().getID()));
                    if (userInfos != null) {
                        preferences.setUserId(userInfos.getUserId());
                    }
                    savePreferences();
                    if (logRecordingId >= 0) {
                        LogRecording logRecording = dbHelper.getLogRecordingFromId(logRecordingId);
                        appEngineOAuthClient.pushLogRecording(logRecording);
                        dbHelper.markLogRecordingAsSynchronizedWithCloud(logRecordingId);
                        Toast.makeText(getApplicationContext(), "Log has been sent to cloud", Toast.LENGTH_LONG).show();
                        redirectToLogList();
                    } else {
                        redirectToSettings();
                    }
                } catch (Throwable t) {
                    Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
                    Toast.makeText(getApplicationContext(), "Error : Impossible to validate key", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Verification code cannot be loaded", Toast.LENGTH_LONG);
            }
        }
    }

}
