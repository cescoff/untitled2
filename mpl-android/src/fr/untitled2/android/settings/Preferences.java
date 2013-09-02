package fr.untitled2.android.settings;

import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.i18n.I18nResourceKey;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/11/13
 * Time: 12:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class Preferences {

    public static Locale[] available_locales = new Locale[] {Locale.FRENCH, Locale.ENGLISH, Locale.GERMAN};

    public static String[] available_locales_translations = new String[] {"Français", "English", "Deutsch"};

    public static String[] available_date_format = new String[] {"yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy"};

    public static String[] available_time_zone_regions = getSortedTimeZoneRegions();

    public static Map<String, String[]> available_time_zones = getSortedTimeZones();

    public static ApplicationSettingsMode[] sorted_application_modes = new ApplicationSettingsMode[] {ApplicationSettingsMode.very_high, ApplicationSettingsMode.high, ApplicationSettingsMode.medium, ApplicationSettingsMode.low, ApplicationSettingsMode.degraded};

    public static Locale default_locale = getUserDefaultLocale();

    public static String default_camera_time_zone = DateTimeZone.forTimeZone(TimeZone.getDefault()).getID();

    public static String default_date_format = "yyyy-MM-dd";

    public static String default_oauth2_key = "NO_KEY";

    public static String default_account_name = "NO_ACCOUNT";

    public static String default_token_date = "NO_DATE";

    public static String default_user_id = "unknown";

    public static int default_auto_mode_sync_hour_of_day = 7;

    public static ApplicationSettingsMode default_settings_modes = ApplicationSettingsMode.high;

    private Locale userLocale;

    private String dateFormat;

    private DateTimeZone cameraTimeZone;

    private long frequency;

    private float minDistance;

    private boolean auto = true;

    private String oauth2Key;

    private String oauthSecret;

    private LocalDateTime tokenDate;

    private String userId;

    private Integer autoModeSyncHourOfDay;

    private boolean filmToolEnabled;

    public Preferences(Locale userLocale, String dateFormat, DateTimeZone cameraTimeZone, boolean auto, ApplicationSettingsMode applicationSettingsModes, String oauth2Key, String oauthSecret, LocalDateTime tokenDate, String userId, Integer autoModeSyncHourOfDay, boolean filmToolEnabled) {
        this.userLocale = userLocale;
        this.dateFormat = dateFormat;
        this.cameraTimeZone = cameraTimeZone;
        this.auto = auto;
        this.frequency = applicationSettingsModes.frequency;
        this.minDistance = applicationSettingsModes.minDistance;
        this.oauth2Key = oauth2Key;
        this.oauthSecret = oauthSecret;
        this.tokenDate = tokenDate;
        this.userId = userId;
        this.autoModeSyncHourOfDay = autoModeSyncHourOfDay;
        this.filmToolEnabled = filmToolEnabled;
    }

    public Locale getUserLocale() {
        return userLocale;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public DateTimeZone getCameraTimeZone() {
        return cameraTimeZone;
    }

    public long getFrequency() {
        return frequency;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setCameraTimeZone(DateTimeZone cameraTimeZone) {
        this.cameraTimeZone = cameraTimeZone;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getOauth2Key() {
        return oauth2Key;
    }

    public void setOauth2Key(String oauth2Key) {
        this.oauth2Key = oauth2Key;
    }

    public String getOauthSecret() {
        return oauthSecret;
    }

    public void setOauthSecret(String oauthSecret) {
        this.oauthSecret = oauthSecret;
    }

    public boolean isConnected() {
        return StringUtils.isNotEmpty(oauth2Key) && !default_oauth2_key.equals(oauth2Key) && StringUtils.isNotEmpty(userId);
    }

    public LocalDateTime getTokenDate() {
        return tokenDate;
    }

    public void setTokenDate(LocalDateTime tokenDate) {
        this.tokenDate = tokenDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getAutoModeSyncHourOfDay() {
        return autoModeSyncHourOfDay;
    }

    public void setAutoModeSyncHourOfDay(Integer autoModeSyncHourOfDay) {
        this.autoModeSyncHourOfDay = autoModeSyncHourOfDay;
    }

    public String getCameraTimeZoneRegion() {
        return getRegion(cameraTimeZone.getID());
    }

    public boolean isFilmToolEnabled() {
        return filmToolEnabled;
    }

    public void setFilmToolEnabled(boolean filmToolEnabled) {
        this.filmToolEnabled = filmToolEnabled;
    }

    public int getApplicationSettingsModePosition() {
        for (int index = 0; index < sorted_application_modes.length; index++) {
            if (frequency == sorted_application_modes[index].getFrequency() && minDistance == sorted_application_modes[index].getMinDistance()) {
                return index;
            }
        }
        return 0;
    }

    public int getCameraTimeZoneRegionPosition() {
        String region = getRegion(cameraTimeZone.getID());
        for (int index = 0; index < available_time_zone_regions.length; index++) {
            if (region.equals(available_time_zone_regions[index])) return index;
        }
        return 0;
    }

    public int getCameraTimeZonePosition(String region) {
        int result = 0;
        for (int index = 0; index < available_time_zones.get(region).length; index++) {
            String availableTimeZoneID = available_time_zones.get(region)[index];
            if (cameraTimeZone.getID().equals(availableTimeZoneID)) {
                Log.i(getClass().getName(), availableTimeZoneID);
                result = index;
            }
        }
        return result;
    }

    public int getCameraTimeZonePosition() {
        String region = getRegion(cameraTimeZone.getID());
        return getCameraTimeZonePosition(region);
    }

    public int getDateFormatPosition() {
        for (int index = 0; index < available_date_format.length; index++) {
            if (dateFormat.equals(available_date_format[index])) {
                return index;
            }
        }
        return 0;
    }

    public int getUserLocalePosition() {
        for (int index = 0; index < available_locales.length; index++) {
            if (userLocale.equals(available_locales[index])) {
                return index;
            }
        }
        return 0;
    }

    public DateTimeFormatter getDateFormatter() {
        return DateTimeFormat.forPattern(dateFormat);
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return  DateTimeFormat.forPattern(dateFormat + " HH:mm");
    }

    public <T> String[] getTranslation(T[] objectsToBeTranslated) {
        String[] result = new String[objectsToBeTranslated.length];
        for (int index = 0; index < objectsToBeTranslated.length; index++) {
            result[index] = getTranslation(new I18nResourceKey(I18nConstants.settings_bundle_name, objectsToBeTranslated[index].toString(), objectsToBeTranslated[index].toString()));
        }
        return result;
    }

    public String getTranslation(I18nResourceKey i18nResourceKey) {
        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("fr.untitled2.android.i18n." + i18nResourceKey.getBundleName(), userLocale);
            return resourceBundle.getString(i18nResourceKey.getKey());
        } catch (Throwable t) {
            return i18nResourceKey.getDefaultValue();
        }
    }

    public enum ApplicationSettingsMode {

        very_high(1000L, 10f),
        high(20 * 1000L, 25f),
        medium(60 * 1000L, 50f),
        low(5 * 60 * 1000L, 50f),
        degraded(15 * 60 * 1000L, 100f);

        private long frequency;

        private float minDistance;

        private ApplicationSettingsMode(long frequency, float minDistance) {
            this.frequency = frequency;
            this.minDistance = minDistance;
        }

        public long getFrequency() {
            return frequency;
        }

        public float getMinDistance() {
            return minDistance;
        }
    }

    private static Locale getUserDefaultLocale() {
        Locale userLocale = Locale.getDefault();
        for (Locale availableLocale : available_locales) {
            if (userLocale.getLanguage().equals(availableLocale.getLanguage())) return availableLocale;
        }
        return Locale.ENGLISH;
    }

    private static String getRegion(String timeZoneID) {
        if (StringUtils.contains(timeZoneID, "/")) {
            return StringUtils.split(timeZoneID, "/")[0];
        }
        return "";
    }

    public static String[] getSortedTimeZoneRegions() {
        Collection<String> timeZones = DateTimeZone.getAvailableIDs();
        List<String> regions = Lists.newArrayList();
        for (String timeZone : timeZones) {
            String region = getRegion(timeZone);
            if (!regions.contains(region)) {
                regions.add(region);
            }
        }
        regions = Ordering.natural().sortedCopy(regions);
        String[] result = new String[regions.size()];
        for (int index = 0; index < regions.size(); index++) {
            result[index] = regions.get(index);
        }
        return result;
    }

    private static Map<String, String[]> getSortedTimeZones() {
        Map<String, List<String>> sortedTimeZones = Maps.newHashMap();

        for (String timeZoneID : DateTimeZone.getAvailableIDs()) {
            String region = getRegion(timeZoneID);
            if (!sortedTimeZones.containsKey(region)) {
                List<String> regionList = Lists.newArrayList();
                sortedTimeZones.put(region, regionList);
            }
            sortedTimeZones.get(region).add(timeZoneID);
        }

        Map<String, String[]> result = Maps.newHashMap();
        for (String region : sortedTimeZones.keySet()) {
            List<String> timeZones = Ordering.natural().sortedCopy(sortedTimeZones.get(region));
            String[] timeZonesArray = new String[timeZones.size()];
            for (int index = 0; index < timeZones.size(); index++) {
                timeZonesArray[index] = timeZones.get(index);
            }
            result.put(region, timeZonesArray);
        }

        return result;
    }

}
