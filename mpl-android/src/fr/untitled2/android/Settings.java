package fr.untitled2.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.common.base.Throwables;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.service.LogRecorder;
import fr.untitled2.android.service.LogSynchronizer;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.utils.PreferencesUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;

import java.util.Locale;
import java.util.TimeZone;

public class Settings extends Activity {

    private static final String selected_region = "selectedRegion";

    private Preferences preferences;

    private String selectedTimeZoneRegion;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(selected_region)) selectedTimeZoneRegion = savedInstanceState.getString(selected_region);
        try {
            super.onCreate(savedInstanceState);

            this.preferences = getPreferences();
        } catch (Throwable t) {
            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
    }



    private Preferences getPreferences() {
        return PreferencesUtils.getPreferences(this);
    }

    private void savePreferences() {
        PreferencesUtils.setSharedPreferences(this, preferences);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(selected_region, selectedTimeZoneRegion);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            preferences = PreferencesUtils.getPreferences(this);
            initCurrentView();
        } catch (Throwable t) {
            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
    }

    private void initCurrentView() {
        try {
            setContentView(R.layout.settings);

            Spinner applicationModeSpinner = (Spinner) findViewById(R.id.ApplicationModeSpinner);
            ArrayAdapter<String> applicationModeAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, preferences.getTranslation(Preferences.sorted_application_modes));
            applicationModeSpinner.setAdapter(applicationModeAdapter);
            applicationModeSpinner.setSelection(preferences.getApplicationSettingsModePosition());
            applicationModeSpinner.setOnItemSelectedListener(OnSelectApplicationMode());

            CheckBox autoModeCheckBox = (CheckBox) findViewById(R.id.AutoModeCheckBox);
            autoModeCheckBox.setChecked(preferences.isAuto());
            autoModeCheckBox.setOnCheckedChangeListener(OnAutoMode());

            Spinner autoModeSyncHourOfDaySpinner = (Spinner) findViewById(R.id.AutoModeSyncHourOfDaySpinner);
            ArrayAdapter<Integer> autoModeSyncHourOfDayAdapter = new ArrayAdapter<Integer>(getApplicationContext(), android.R.layout.simple_spinner_item, new Integer[]{0, 1, 2, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23});
            autoModeSyncHourOfDaySpinner.setAdapter(autoModeSyncHourOfDayAdapter);
            autoModeSyncHourOfDaySpinner.setOnItemSelectedListener(OnSelectSyncHourOfDay());
            autoModeSyncHourOfDaySpinner.setSelection(preferences.getAutoModeSyncHourOfDay());
            if (!preferences.isAuto()) autoModeSyncHourOfDaySpinner.setVisibility(View.GONE);

            Spinner cameraTimeZoneRegionSpinner = (Spinner) findViewById(R.id.CameraTimeZoneRegionSpinner);
            ArrayAdapter<String> cameraTimeZoneRegionAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, Preferences.available_time_zone_regions);
            cameraTimeZoneRegionSpinner.setAdapter(cameraTimeZoneRegionAdapter);
            cameraTimeZoneRegionSpinner.setSelection(preferences.getCameraTimeZoneRegionPosition());
            cameraTimeZoneRegionSpinner.setOnItemSelectedListener(OnClickToChangeDateTimeZoneRegion());

            Spinner cameraTimeZoneSpinner = (Spinner) findViewById(R.id.CameraTimeZoneSpinner);
            ArrayAdapter<String> cameraTimeZoneAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, Preferences.available_time_zones.get(preferences.getCameraTimeZoneRegion()));
            cameraTimeZoneSpinner.setAdapter(cameraTimeZoneAdapter);
            cameraTimeZoneSpinner.setSelection(preferences.getCameraTimeZonePosition());
            cameraTimeZoneSpinner.setOnItemSelectedListener(OnSelectCameraTimeZone());

            Spinner dateFormatSpinner = (Spinner) findViewById(R.id.DateFormatSpinner);
            ArrayAdapter<String> dateFormatAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, preferences.getTranslation(Preferences.available_date_format));
            dateFormatSpinner.setAdapter(dateFormatAdapter);
            dateFormatSpinner.setSelection(preferences.getDateFormatPosition());
            dateFormatSpinner.setOnItemSelectedListener(OnSelectDateFormat());

            Spinner userLocaleSpinner = (Spinner) findViewById(R.id.UserLanguageSpinner);
            ArrayAdapter<String> userLocaleAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, Preferences.available_locales_translations);
            userLocaleSpinner.setAdapter(userLocaleAdapter);
            userLocaleSpinner.setSelection(preferences.getUserLocalePosition());
            userLocaleSpinner.setOnItemSelectedListener(OnSelectUserLocale());

            Button accountConnectionButton = (Button) findViewById(R.id.ConnectedAccountButton);
            if (preferences.isConnected()) {
                accountConnectionButton.setText(preferences.getTranslation(I18nConstants.settings_disconnect_account));
                accountConnectionButton.setOnClickListener(OnClickToDisconnect());
            } else {
                accountConnectionButton.setText(preferences.getTranslation(I18nConstants.settings_connectaccount));
                accountConnectionButton.setOnClickListener(OnClickToConnect());
            }

            CheckBox filmToolCheckBox = (CheckBox) findViewById(R.id.FilmToolCheckBox);
            filmToolCheckBox.setOnCheckedChangeListener(OnFilmToolEnabled());

            TextView logPrecisionLabel = (TextView) findViewById(R.id.ApplicationModeText);
            logPrecisionLabel.setText(preferences.getTranslation(I18nConstants.settings_log_precision) + " : ");

            TextView autoModeLabel = (TextView) findViewById(R.id.AutoModeText);
            autoModeLabel.setText(preferences.getTranslation(I18nConstants.settings_autolog) + " : ");

            TextView cameraTimeZoneLabel = (TextView) findViewById(R.id.CameraTimeZoneText);
            cameraTimeZoneLabel.setText(preferences.getTranslation(I18nConstants.settings_camera_time_zone) + " : ");

            TextView dateFormatLabel = (TextView) findViewById(R.id.DateFormatText);
            dateFormatLabel.setText(preferences.getTranslation(I18nConstants.settings_date_format) + " : ");

            TextView preferedLanguage = (TextView) findViewById(R.id.UserLanguageText);
            preferedLanguage.setText(preferences.getTranslation(I18nConstants.settings_language) + " : ");

            TextView autoModeSyncHourOfDay = (TextView) findViewById(R.id.AutoModeSyncHourOfDayText);
            autoModeSyncHourOfDay.setText(preferences.getTranslation(I18nConstants.settings_autosynchourofday) + " : ");
            if (!preferences.isAuto()) autoModeSyncHourOfDay.setVisibility(View.GONE);

            TextView filmToolText = (TextView) findViewById(R.id.FilmToolText);
            filmToolText.setText(preferences.getTranslation(I18nConstants.settings_filmmode) + " : ");

            ImageButton homeButton = (ImageButton) findViewById(R.id.ButtonHome);
            homeButton.setOnClickListener(OnClickChangeToHome());

            ImageButton logList = (ImageButton) findViewById(R.id.ButtonLogList);
            logList.setOnClickListener(OnClickChangeToLogList());

            ImageButton filmToolButton = (ImageButton) findViewById(R.id.ButtonFilmTools);
            if (!preferences.isFilmToolEnabled()) filmToolButton.setVisibility(View.GONE);
            else filmToolButton.setOnClickListener(OnClickToFilmTool());

        } catch (Throwable t) {
            Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
        }
    }

    AdapterView.OnItemSelectedListener OnClickToChangeDateTimeZoneRegion() {

        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String region = Preferences.available_time_zone_regions[position];
                selectedTimeZoneRegion = region;
                Spinner cameraTimeZoneSpinner = (Spinner) findViewById(R.id.CameraTimeZoneSpinner);
                ArrayAdapter<String> cameraTimeZoneAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, Preferences.available_time_zones.get(region));
                cameraTimeZoneSpinner.setAdapter(cameraTimeZoneAdapter);
                cameraTimeZoneSpinner.setSelection(preferences.getCameraTimeZonePosition(region));
                cameraTimeZoneSpinner.setOnItemSelectedListener(OnSelectCameraTimeZone());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

    }

    View.OnClickListener OnClickToConnect() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Connect.class);
                startActivity(intent);
            }
        };

    }

    View.OnClickListener OnClickToDisconnect() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.setTokenDate(null);
                preferences.setOauth2Key(null);
                preferences.setOauthSecret(null);
                savePreferences();
                Button accountConnectionButton = (Button) findViewById(R.id.ConnectedAccountButton);
                accountConnectionButton.setText(preferences.getTranslation(I18nConstants.settings_connectaccount));
                accountConnectionButton.setOnClickListener(OnClickToConnect());
            }
        };

    }

    View.OnClickListener OnClickChangeToHome()
    {
        return new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Main.class);
                startActivity(intent);
            }
        };
    }

    AdapterView.OnItemSelectedListener OnSelectApplicationMode() {

        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Preferences.ApplicationSettingsMode applicationSettingsMode = Preferences.sorted_application_modes[position];
                preferences.setFrequency(applicationSettingsMode.getFrequency());
                preferences.setMinDistance(applicationSettingsMode.getMinDistance());
                savePreferences();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                preferences.setFrequency(Preferences.default_settings_modes.getFrequency());
                preferences.setMinDistance(Preferences.default_settings_modes.getMinDistance());
                savePreferences();
            }
        };

    }

    CompoundButton.OnCheckedChangeListener OnAutoMode() {

        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView autoModeSyncHourOfDay = (TextView) findViewById(R.id.AutoModeSyncHourOfDayText);
                autoModeSyncHourOfDay.setText(preferences.getTranslation(I18nConstants.settings_autosynchourofday) + " : ");
                if (isChecked) autoModeSyncHourOfDay.setVisibility(View.VISIBLE);
                else autoModeSyncHourOfDay.setVisibility(View.GONE);
                Spinner autoModeSyncHourOfDaySpinner = (Spinner) findViewById(R.id.AutoModeSyncHourOfDaySpinner);
                ArrayAdapter<Integer> autoModeSyncHourOfDayAdapter = new ArrayAdapter<Integer>(getApplicationContext(), android.R.layout.simple_spinner_item, new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23});
                autoModeSyncHourOfDaySpinner.setAdapter(autoModeSyncHourOfDayAdapter);
                if (isChecked) autoModeSyncHourOfDaySpinner.setVisibility(View.VISIBLE);
                else autoModeSyncHourOfDaySpinner.setVisibility(View.GONE);
                autoModeSyncHourOfDaySpinner.setSelection(preferences.getAutoModeSyncHourOfDay());
                autoModeSyncHourOfDaySpinner.setOnItemSelectedListener(OnSelectSyncHourOfDay());

                preferences.setAuto(isChecked);
                savePreferences();
            }
        };

    }

    View.OnClickListener OnClickToFilmTool() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FilmTool.class);
                startActivity(intent);
            }
        };

    }

    CompoundButton.OnCheckedChangeListener OnFilmToolEnabled() {

        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.setFilmToolEnabled(isChecked);
                savePreferences();
                if (isChecked) findViewById(R.id.ButtonFilmTools).setVisibility(View.VISIBLE);
            }
        };

    }
    AdapterView.OnItemSelectedListener OnSelectSyncHourOfDay() {

        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setAutoModeSyncHourOfDay(position);
                savePreferences();
                Intent stopServiceIntent = new Intent(getApplicationContext(), LogSynchronizer.class);
                stopService(stopServiceIntent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

    }

    AdapterView.OnItemSelectedListener OnSelectCameraTimeZone() {

        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String cameraTimeZone = Preferences.available_time_zones.get(selectedTimeZoneRegion)[position];
                preferences.setCameraTimeZone(DateTimeZone.forID(cameraTimeZone));
                savePreferences();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                TimeZone timeZone = TimeZone.getDefault();
                preferences.setCameraTimeZone(DateTimeZone.forTimeZone(timeZone));
                savePreferences();
            }
        };

    }

    View.OnClickListener OnClickChangeToLogList()
    {
        return new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LogList.class);
                startActivity(intent);
            }
        };
    }

    AdapterView.OnItemSelectedListener OnSelectDateFormat() {

        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setDateFormat(Preferences.available_date_format[position]);
                savePreferences();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                preferences.setDateFormat(Preferences.default_date_format);
                savePreferences();
            }
        };

    }

    AdapterView.OnItemSelectedListener OnSelectUserLocale() {

        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setUserLocale(Preferences.available_locales[position]);
                savePreferences();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                preferences.setUserLocale(Preferences.default_locale);
                savePreferences();
            }
        };

    }
}
