package fr.untitled2.android.service.task;

import com.beust.jcommander.internal.Lists;
import fr.untitled2.android.service.SchedulingService;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.utils.CollectionUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 11/12/13
 * Time: 21:21
 * To change this template use File | Settings | File Templates.
 */
public class PreferencesSynchronizer extends ITask {

    @Override
    protected void executeTask(DbHelper dbHelper, Preferences preferences) throws Exception {
/*
        if (isConnected()) {
            AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(preferences.getOauth2Key(), preferences.getOauthSecret());

            UserPreferences userPreferences = appEngineOAuthClient.getUserPreferences();
            Preferences updatedPreferences = preferences.clone();
            updatedPreferences.setKnownLocations(Lists.newArrayList(userPreferences.getKnownLocations()));
            updatePreferences(updatedPreferences);
        }
*/
    }

    @Override
    public boolean isAvailableOnSleepMode() {
        return true;
    }

    @Override
    public SchedulingService.Scheduling getScheduling() {
        return SchedulingService.Scheduling.SHORT;
    }
}
