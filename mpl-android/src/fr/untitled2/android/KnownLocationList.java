package fr.untitled2.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.sqlilite.ErrorReport;
import fr.untitled2.android.sqlilite.KnownLocationWithDatetime;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.Journey;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.utils.DateTimeUtils;
import fr.untitled2.common.utils.DistanceUtils;
import fr.untitled2.common.utils.JourneyUtils;
import fr.untitled2.common.utils.NumberFormattingUtils;
import fr.untitled2.utils.CollectionUtils;
import org.javatuples.Pair;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KnownLocationList extends MenuActivity {

    private static final String current_view = "currentView";
    private static final String log_to_delete = "currentLogToDelete";

    private Preferences preferences;

    private DbHelper dbHelper;

    private long logIdToBeDeleted;

    private int currentViewId = R.layout.knownlocationlist;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.preferences = getPreferences();
            this.dbHelper = new DbHelper(getApplicationContext(), preferences);
        } catch (Throwable t) {
            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
    }

    @Override
    protected String getPageTitle(Preferences preferences) {
        return preferences.getTranslation(I18nConstants.knownlocationlist_title);
    }

    @Override
    protected boolean displayMenuBar() {
        return isManageMode();
    }

    private void initListView(boolean manageMode, String ssid) {
        try {
            setContentView(R.layout.knownlocationlist);

            ListView logListView = (ListView) findViewById(R.id.KnownLocationsListView);
            logListView.setAdapter(Statuses(getKnownLocations(), manageMode, ssid));
        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            try {
                dbHelper.addErrorReport(ErrorReport.fromThrowable(getClass(), "Error displaying journey list", t));
            } catch (Throwable tt) {
            }
            Log.e(getLocalClassName(), stackTrace);
        }
    }

    private KnownLocationWithDistance[] getKnownLocations() throws FileNotFoundException {
        if (CollectionUtils.isNotEmpty(preferences.getKnownLocations())) {
            final Optional<LogRecording> currentLogRecording = dbHelper.getCurrentLog();

            KnownLocationWithDistance[] result = new KnownLocationWithDistance[preferences.getKnownLocations().size()];
            List<KnownLocation> knownLocations = preferences.getKnownLocations();

            List<KnownLocationWithDistance> knownLocationWithDistances = Lists.newArrayList();
            if (currentLogRecording.isPresent()) {
                knownLocationWithDistances = Lists.newArrayList(Iterables.transform(knownLocations, new Function<KnownLocation, KnownLocationWithDistance>() {
                    @Override
                    public KnownLocationWithDistance apply(KnownLocation knownLocation) {
                        KnownLocationWithDistance knownLocationWithDistance = new KnownLocationWithDistance();
                        knownLocationWithDistance.setKnownLocation(knownLocation);
                        knownLocationWithDistance.setDistance(DistanceUtils.getDistance(currentLogRecording.get().getLastLogRecord().getLatitudeAndLongitude(), knownLocation.getLatitudeLongitude()));
                        return knownLocationWithDistance;
                    }
                }));
                knownLocationWithDistances = Ordering.natural().onResultOf(new Function<KnownLocationWithDistance, Double>() {
                    @Override
                    public Double apply(KnownLocationWithDistance knownLocation) {
                        return knownLocation.distance;
                    }
                }).sortedCopy(knownLocationWithDistances);
            } else {
                knownLocationWithDistances = Lists.newArrayList(Iterables.transform(knownLocations, new Function<KnownLocation, KnownLocationWithDistance>() {
                    @Override
                    public KnownLocationWithDistance apply(KnownLocation knownLocation) {
                        KnownLocationWithDistance knownLocationWithDistance = new KnownLocationWithDistance();
                        knownLocationWithDistance.setKnownLocation(knownLocation);
                        knownLocationWithDistance.setDistance(-1.0);
                        return knownLocationWithDistance;
                    }
                }));
            }

            for (int index = 0; index < knownLocationWithDistances.size(); index++) {
                result[index] = knownLocationWithDistances.get(index);
            }
            return result;
        }

        return new KnownLocationWithDistance[0];
    }

    ArrayAdapter<KnownLocationWithDistance> Statuses(final KnownLocationWithDistance[] knownLocations, final boolean manageMode, final String ssid) {

        return new ArrayAdapter<KnownLocationWithDistance>(getApplicationContext(), R.layout.knownlocationlistrow, knownLocations) {

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.knownlocationlistrow, parent, false);

                RadioButton radioButton = (RadioButton) rowView.findViewById(R.id.KnownLocationSelectRadio);
                ImageButton deleteButton = (ImageButton) rowView.findViewById(R.id.KnownLocationDeleteButton);
                TextView knownLocationLabel = (TextView) rowView.findViewById(R.id.KnownLocationDisplay);

                if (manageMode) {
                    radioButton.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<KnownLocation> newKnownlocations = Lists.newArrayList();
                            for (KnownLocation knownLocation : preferences.getKnownLocations()) {
                                if (!knownLocation.equals(knownLocations[position].getKnownLocation())) {
                                    newKnownlocations.add(knownLocation);
                                }
                            }
                            preferences.setKnownLocations(newKnownlocations);
                            setPreferences(preferences);
                            onResume();
                        }
                    });
                } else {
                    radioButton.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.GONE);
                    radioButton.setSelected(false);
                    radioButton.setOnClickListener(OnClickKnownLocationSelected(manageMode, ssid, knownLocations[position].getKnownLocation()));
                }

                knownLocationLabel.setText(knownLocations[position].getKnownLocation().getName() + " (" + NumberFormattingUtils.toDistance(knownLocations[position].getDistance(), NumberFormattingUtils.DistanceUnit.metric) + ")");

                return rowView;
            }

        };

    }

    private void setPreferences(Preferences preferences) {
        PreferencesUtils.setSharedPreferences(this, preferences);
    }

    private Preferences getPreferences() {
        return PreferencesUtils.getPreferences(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (logIdToBeDeleted >= 0) outState.putLong(log_to_delete, logIdToBeDeleted);
        outState.putInt(current_view, currentViewId);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.dbHelper = new DbHelper(getApplicationContext(), preferences);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(current_view)) currentViewId = savedInstanceState.getInt(current_view);
            if (savedInstanceState.containsKey(log_to_delete)) logIdToBeDeleted = savedInstanceState.getLong(log_to_delete);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String ssid = null;
        if (intent != null) {
            ssid = intent.getStringExtra("ssid");
        }
        if (currentViewId == R.layout.knownlocationlist) initListView(isManageMode(), ssid);
    }

    private boolean isManageMode() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getBooleanExtra("manageMode", false);
        }
        return false;
    }

    AdapterView.OnClickListener OnClickKnownLocationSelected(final boolean manageMode, final String ssid, final KnownLocation knownLocation) {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markCurrentWifi(manageMode, ssid, knownLocation).show();
            }
        };

    }


    private AlertDialog markCurrentWifi(final boolean manageMode, final String ssid, final KnownLocation knownLocation) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(preferences.getTranslation(I18nConstants.knownlocationlist_add_wifi_to_knownlocation_label, new String[] {ssid, knownLocation.getName()}));
        alertDialogBuilder.setPositiveButton(preferences.getTranslation(I18nConstants.loglist_uploadyes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (KnownLocation location : preferences.getKnownLocations()) {
                    if (location.getLatitude() == knownLocation.getLatitude() && location.getLongitude() == knownLocation.getLongitude()) {
                        location.getWifiSSIDs().add(ssid);
                    }
                }
                updatePreferences(preferences);
                Optional<LogRecording> currentLogRecordingOptional = dbHelper.getCurrentLog();
                if (currentLogRecordingOptional.isPresent()) {
                    LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                    logRecord.setLatitude(knownLocation.getLatitude());
                    logRecord.setLongitude(knownLocation.getLongitude());
                    logRecord.setAltitude(knownLocation.getAltitude());
                    logRecord.setDateTime(DateTimeUtils.getCurrentDateTimeInUTC());
                    logRecord.setKnownLocation(knownLocation);

                    try {
                        dbHelper.addRecordToCurrentLog(logRecord, Optional.of(knownLocation));
                    } catch (Throwable t) {
                        try {
                            dbHelper.addErrorReport(ErrorReport.fromThrowable(getClass(), "An error has occured while adding wifi to known location", t));
                        } catch (Throwable tt) {
                        }
                    }
                    redirectToHome();
                }
            }
        });
        alertDialogBuilder.setNegativeButton(preferences.getTranslation(I18nConstants.loglist_uploadno), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initListView(manageMode, ssid);
            }
        });
        return alertDialogBuilder.create();
    }

    private void redirectToHome() {
        Intent intent = new Intent(getApplicationContext(), Main.class);
        startActivity(intent);
    }

    private void updatePreferences(Preferences preferences) {
        PreferencesUtils.setSharedPreferences(this, preferences);
    }

    public static class KnownLocationWithDistance {

        private double distance;

        private String ssid;

        private KnownLocation knownLocation;

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public KnownLocation getKnownLocation() {
            return knownLocation;
        }

        public void setKnownLocation(KnownLocation knownLocation) {
            this.knownLocation = knownLocation;
        }
    }

}
