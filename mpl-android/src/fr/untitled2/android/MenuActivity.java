package fr.untitled2.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.common.base.Throwables;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.utils.PreferencesUtils;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 15/12/13
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */
public abstract class MenuActivity extends SherlockActivity {

    private static final int add_knownlocation_id = 1;
    private static final int global_settings_id = 2;
    private static final int location_setting_id = 3;
    private static final int sport_setting_id = 4;

    private static final int history_logs = 4;
    private static final int history_journeys = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(Window.FEATURE_PROGRESS);
            getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
            setTitle(getPageTitle(PreferencesUtils.getPreferences(this)));
            if (displayMenuBar()) {
                ActionBar actionBar = getSupportActionBar();

                if (!getClass().equals(Main.class)) {
                    actionBar.setIcon(R.drawable.house);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                } else {
                    actionBar.setIcon(R.drawable.icon);
                }
            }
        } catch (Throwable t) {
            Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
        }
    }

    protected abstract String getPageTitle(Preferences preferences);

    protected abstract boolean displayMenuBar();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            if (!displayMenuBar()) return false;
            Preferences preferences = getPreferences();
            SubMenu history = menu.addSubMenu(preferences.getTranslation(I18nConstants.knownlocationlist_menuhistory_label));
            history.add(0, history_logs, 1, preferences.getTranslation(I18nConstants.knownlocationlist_menuhistorylogs_label));
            history.add(0, history_journeys, 2, preferences.getTranslation(I18nConstants.knownlocationlist_menuhistoryjourneys_label));

            SubMenu settings = menu.addSubMenu(preferences.getTranslation(I18nConstants.knownlocationlist_menusettings_label));
            settings.add(1, add_knownlocation_id, 1, preferences.getTranslation(I18nConstants.knownlocationlist_add_knownlocation));
            settings.add(1, global_settings_id, 2, preferences.getTranslation(I18nConstants.knownlocationlist_menuglobalsettings_label));
            settings.add(1, location_setting_id, 3, preferences.getTranslation(I18nConstants.knownlocationlist_menulocationsettings_label));
            settings.add(1, sport_setting_id, 4, preferences.getTranslation(I18nConstants.knownlocationlist_menuknownsportsettings_label));

            settings.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            settings.getItem().setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_dark);
            history.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            return true;
        } catch (Throwable t) {
            Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item.getItemId() == android.R.id.home) goToHomeView();
            if (item.getGroupId() == 1) {
                switch (item.getItemId()) {
                    case add_knownlocation_id:
                        goToAddKnownlocationView();
                        break;
                    case global_settings_id:
                        goToSettingsView();
                        break;
                    case location_setting_id:
                        goToKnownlocationView();
                        break;
                    case sport_setting_id:
                        goToSettingsView();
                        break;
                }
            } else if (item.getGroupId() == 0) {
                switch (item.getItemId()) {
                    case history_logs:
                        goLogListView();
                        break;
                    case history_journeys:
                        goJourneyListView();
                        break;
                }
            }
            return super.onOptionsItemSelected(item);
        } catch (Throwable t) {
            Log.e(getClass().getName(), Throwables.getStackTraceAsString(t));
        }
        return false;
    }

    private void goLogListView() {
        Intent intent = new Intent(getApplicationContext(), LogList.class);
        startActivity(intent);
    }

    private void goJourneyListView() {
        Intent intent = new Intent(getApplicationContext(), LogList.class);
        startActivity(intent);
    }

    private void goToAddKnownlocationView() {
        Intent intent = new Intent(getApplicationContext(), KnownLocationAdd.class);
        startActivity(intent);
    }

    private void goToSettingsView() {
        Intent intent = new Intent(getApplicationContext(), Settings.class);
        startActivity(intent);
    }

    private void goToHomeView() {
        Intent intent = new Intent(getApplicationContext(), Main.class);
        startActivity(intent);
    }

    private void goToKnownlocationView() {
        Intent intent = new Intent(getApplicationContext(), KnownLocationList.class);
        intent.putExtra("manageMode", true);
        startActivity(intent);
    }

    private Preferences getPreferences() {
        return PreferencesUtils.getPreferences(this);
    }

}
