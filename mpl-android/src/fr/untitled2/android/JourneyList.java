package fr.untitled2.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.sqlilite.ErrorReport;
import fr.untitled2.android.sqlilite.KnownLocationWithDatetime;
import fr.untitled2.android.sqlilite.LogRecordingWithStatus;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.Journey;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.DateTimeUtils;
import fr.untitled2.common.utils.DistanceUtils;
import fr.untitled2.common.utils.JourneyUtils;
import fr.untitled2.common.utils.NumberFormattingUtils;
import fr.untitled2.utils.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

public class JourneyList extends MenuActivity {

    private static final String current_view = "currentView";
    private static final String log_to_delete = "currentLogToDelete";

    private Preferences preferences;

    private DbHelper dbHelper;

    private long logIdToBeDeleted;

    private int currentViewId = R.layout.journeylist;

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
        return preferences.getTranslation(I18nConstants.journeylist_title);
    }

    @Override
    protected boolean displayMenuBar() {
        return true;
    }

    private void initListView(long logRecordingId) {
        try {
            setContentView(R.layout.journeylist);
            if (logRecordingId > 0) {
                ListView logListView = (ListView) findViewById(R.id.JourneyListView);
                logListView.setAdapter(Statuses(getJourneys(logRecordingId)));
            }
            ImageButton homeButton = (ImageButton) findViewById(R.id.ButtonHome);
            homeButton.setOnClickListener(OnClickChangeToHome());

            ImageButton settingsButton = (ImageButton) findViewById(R.id.ButtonSettings);
            settingsButton.setOnClickListener(OnClickChangeToSettings());
        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            try {
                dbHelper.addErrorReport(ErrorReport.fromThrowable(getClass(), "Error displaying journey list", t));
            } catch (Throwable tt) {
            }
            Log.e(getLocalClassName(), stackTrace);
        }
    }

    private Journey[] getJourneys(long logRecordingId) throws FileNotFoundException {
        LogRecording logRecording = dbHelper.getLogRecordingFromId(logRecordingId);
        Collection<Journey> existingJourneys = JourneyUtils.getJourneys(logRecording, preferences.getKnownLocations());
        Optional<KnownLocationWithDatetime> lastKnownLocation = dbHelper.getLastKnownLocation();
        if (lastKnownLocation.isPresent() && lastKnownLocation.get().getDistance() > 0) {
            Journey currentJourney = new Journey();
            currentJourney.setStart(lastKnownLocation.get().getKnownLocation());
            currentJourney.setStartDatetime(lastKnownLocation.get().getPointDate());

            currentJourney.setDistance(lastKnownLocation.get().getDistance());

            KnownLocation here = new KnownLocation();
            here.setName(preferences.getTranslation(I18nConstants.journeylist_here_label));
            here.setLatitude(logRecording.getLastLogRecord().getLatitude());
            here.setLongitude(logRecording.getLastLogRecord().getLongitude());
            currentJourney.setEnd(here);
            currentJourney.setEndDateTime(logRecording.getLastLogRecord().getDateTime());
            currentJourney.setMaxSpeed(-1.0);
            existingJourneys.add(currentJourney);
        }
        List<Journey> journeys = Ordering.natural().onResultOf(new Function<Journey, LocalDateTime>() {
            @Override
            public LocalDateTime apply(Journey journey) {
                return journey.getStartDatetime();
            }
        }).sortedCopy(existingJourneys);

        if (CollectionUtils.isNotEmpty(journeys)) {
            journeys = Lists.partition(journeys, 30).get(0);
            Journey[] journeyArray = new Journey[journeys.size()];

            for (int index = 0; index < journeyArray.length; index++) {
                journeyArray[index] = journeys.get(index);
            }
            return journeyArray;
        }
        return new Journey[0];
    }

    ArrayAdapter<Journey> Statuses(final Journey[] journeys) {

        return new ArrayAdapter<Journey>(getApplicationContext(), R.layout.journeylistrow, journeys) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.journeylistrow, parent, false);
                TextView journeyLabel = (TextView) rowView.findViewById(R.id.JourneyDisplay);
                TextView distanceLabel = (TextView) rowView.findViewById(R.id.JourneyStatisticsDistance);
                TextView durationLabel = (TextView) rowView.findViewById(R.id.JourneyStatisticsDuration);
                TextView speedStatistics = (TextView) rowView.findViewById(R.id.JourneyStatisticsSpeed);

                StringBuilder labelBuilder = new StringBuilder();
                labelBuilder.append(preferences.getTranslation(I18nConstants.journeylist_start_label)).
                        append(" ").
                        append(journeys[position].getStart().getName()).
                        append(" ").
                        append(preferences.getTranslation(I18nConstants.journeylist_end_label)).
                        append(" ").
                        append(journeys[position].getEnd().getName());


                Period journeyDuration = new Period(journeys[position].getStartDatetime(), journeys[position].getEndDateTime());
                double speed = journeys[position].getDistance() / journeyDuration.toStandardSeconds().getSeconds();

                journeyDuration = journeyDuration.withMillis(0);
                if (journeyDuration.getHours() > 0) journeyDuration = journeyDuration.withSeconds(0);
                PeriodFormatter periodFormatter = PeriodFormat.wordBased(preferences.getUserLocale());


                StringBuilder statisticsBuilder = new StringBuilder();
                statisticsBuilder.append(preferences.getTranslation(I18nConstants.journeylist_distance_label)).
                        append(" ").
                        append(NumberFormattingUtils.toDistance(journeys[position].getDistance(), NumberFormattingUtils.DistanceUnit.metric));

                journeyLabel.setText(labelBuilder.toString());
                distanceLabel.setText(statisticsBuilder.toString());

                StringBuilder durationBuilder = new StringBuilder();
                durationBuilder.append(preferences.getTranslation(I18nConstants.journeylist_duration_label)).
                        append(" ").
                        append(periodFormatter.print(journeyDuration));

                durationLabel.setText(durationBuilder.toString());

                StringBuilder speedStatisticsBuilder = new StringBuilder();
                speedStatisticsBuilder.append(preferences.getTranslation(I18nConstants.journeylist_avgspeed_label)).
                        append(" ").
                        append(NumberFormattingUtils.toSpeed(speed, NumberFormattingUtils.DistanceUnit.metric));

                if (journeys[position].getMaxSpeed() > 0) {
                    speedStatisticsBuilder.
                            append(", ").
                            append(preferences.getTranslation(I18nConstants.journeylist_maxspeed_label)).
                            append(" ").
                            append(NumberFormattingUtils.toSpeed(journeys[position].getMaxSpeed(), NumberFormattingUtils.DistanceUnit.metric));
                }
                speedStatistics.setText(speedStatisticsBuilder.toString());

                return rowView;
            }

        };

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
        long logRecordingId = 0L;
        if (intent != null) {
            logRecordingId = intent.getLongExtra("logRecordingId", logRecordingId);
        } else {
            Optional<LogRecording> currentLogRecordingOptional = dbHelper.getCurrentLog();
            if (currentLogRecordingOptional.isPresent()) {
                logRecordingId = currentLogRecordingOptional.get().getId();
            }
        }
        if (currentViewId == R.layout.journeylist) initListView(logRecordingId);
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

    View.OnClickListener OnClickChangeToSettings()
    {
        return new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
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

}
