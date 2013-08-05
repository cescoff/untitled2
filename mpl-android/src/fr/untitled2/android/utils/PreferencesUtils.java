package fr.untitled2.android.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import fr.untitled2.android.settings.Preferences;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/8/13
 * Time: 7:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PreferencesUtils {

    private static final String preferences_name = "fr.untitled2.MyPictureLogPreferences";

    private static final String frequency_key = "fr.untitled2.frequency";

    private static final String min_distance_key = "fr.untitled2.minDistance";

    private static final String camera_datetime_zone_key = "fr.untitled2.dateTimeZone";

    private static final String date_format_key = "fr.untitled2.dateFormat";

    private static final String locale_key = "fr.untitled2.locale";

    private static final String auto_mode_key = "fr.untitled2.auto";

    private static final String auto_mode_sync_hour_of_day_key = "fr.untitled2.autoModeSyncHourOfDay";

    private static final String oauth2_key = "fr.untitled2.oauthkey";

    private static final String oauth_secret = "fr.untitled2.oauthsecret";

    private static final String account_token_date = "fr.untitled2.AccountLoginDate";

    private static final String film_tool_enabled = "fr.untitled2.FilmTool";

    private static final String user_id = "fr.untitled2.UserId";

    public static Preferences getPreferences(Service service) {
        SharedPreferences sharedPreferences = service.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        return getPreferences(sharedPreferences);
    }

    public static Preferences getPreferences(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        return getPreferences(sharedPreferences);
    }

    public static void setSharedPreferences(Activity activity, Preferences preferences) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        setPreferences(sharedPreferences, preferences);
    }

    public static void setSharedPreferences(Service service, Preferences preferences) {
        SharedPreferences sharedPreferences = service.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        setPreferences(sharedPreferences, preferences);
    }

    public static void setPreferences(SharedPreferences sharedPreferences, Preferences preferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(frequency_key, new Long(preferences.getFrequency()).intValue());
        editor.putInt(min_distance_key, new Float(preferences.getMinDistance()).intValue());
        editor.putString(camera_datetime_zone_key, preferences.getCameraTimeZone().getID());
        editor.putString(date_format_key, preferences.getDateFormat());
        editor.putString(locale_key, preferences.getUserLocale().getLanguage());
        editor.putBoolean(auto_mode_key, preferences.isAuto());
        editor.putInt(auto_mode_sync_hour_of_day_key, preferences.getAutoModeSyncHourOfDay());
        editor.putBoolean(film_tool_enabled, preferences.isFilmToolEnabled());
        if (StringUtils.isNotEmpty(preferences.getOauth2Key())) editor.putString(oauth2_key, preferences.getOauth2Key());
        if (StringUtils.isNotEmpty(preferences.getOauthSecret())) editor.putString(oauth_secret, preferences.getOauthSecret());
        if (preferences.getTokenDate() != null) editor.putString(account_token_date, preferences.getTokenDate().toString());
        if (StringUtils.isNotEmpty(preferences.getUserId())) editor.putString(user_id, preferences.getUserId());

        editor.commit();
    }

    public static Preferences getPreferences(SharedPreferences sharedPreferences) {
        long frequency = new Integer(sharedPreferences.getInt(frequency_key, new Long(Preferences.default_settings_modes.getFrequency()).intValue())).longValue();
        float minDistance = new Integer(sharedPreferences.getInt(min_distance_key, new Float(Preferences.default_settings_modes.getMinDistance()).intValue())).floatValue();
        DateTimeZone cameraDateTimeZone = DateTimeZone.forID(sharedPreferences.getString(camera_datetime_zone_key, Preferences.default_camera_time_zone));
        String dateFormat = sharedPreferences.getString(date_format_key, Preferences.default_date_format);
        Locale locale = new Locale(sharedPreferences.getString(locale_key, Preferences.default_locale.getLanguage()));
        Boolean auto = sharedPreferences.getBoolean(auto_mode_key, true);
        String oauth2Key = sharedPreferences.getString(oauth2_key, Preferences.default_oauth2_key);
        if (oauth2Key.equals(Preferences.default_oauth2_key)) oauth2Key = null;
        String oauthSecret = sharedPreferences.getString(oauth_secret, Preferences.default_account_name);
        if (oauthSecret.equals(Preferences.default_account_name)) oauthSecret = null;
        String tokenDate = sharedPreferences.getString(account_token_date, Preferences.default_token_date);
        String userId = sharedPreferences.getString(user_id, Preferences.default_user_id);
        if (userId.equals(Preferences.default_user_id)) userId = null;
        LocalDateTime tokenDateTime = null;
        if (!tokenDate.equals(Preferences.default_token_date)) tokenDateTime = new LocalDateTime(tokenDate);
        Integer autoModeSyncHourOfDay = sharedPreferences.getInt(auto_mode_sync_hour_of_day_key, Preferences.default_auto_mode_sync_hour_of_day);
        boolean filmToolEnabled = sharedPreferences.getBoolean(film_tool_enabled, false);

        Preferences.ApplicationSettingsMode applicationSettingsMode = Preferences.default_settings_modes;

        for (Preferences.ApplicationSettingsMode settingsMode : Preferences.ApplicationSettingsMode.values()) {
            if (settingsMode.getFrequency() == frequency && settingsMode.getMinDistance() == minDistance) applicationSettingsMode = settingsMode;
        }

        return new Preferences(locale, dateFormat, cameraDateTimeZone, auto, applicationSettingsMode, oauth2Key,oauthSecret, tokenDateTime, userId, autoModeSyncHourOfDay, filmToolEnabled);

    }

}
