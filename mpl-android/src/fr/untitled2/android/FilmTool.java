package fr.untitled2.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.FilmCounter;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.utils.CollectionUtils;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/20/13
 * Time: 11:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class FilmTool extends MenuActivity {

    private boolean logStarted;

    private Preferences preferences;

    private FilmCounter filmCounter;

    private DbHelper dbHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            this.preferences = getPreferences();
            this.dbHelper = new DbHelper(getApplicationContext(), preferences);
            if (dbHelper.hasCurrentLog()) {
                logStarted = true;
            }
            super.onCreate(savedInstanceState);
            this.filmCounter = dbHelper.getCurrentFilmCounter();
            initView();
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
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!logStarted && getIntent() != null) logStarted = getIntent().getBooleanExtra(Main.log_started, false);
        initView();
    }

    private void initView() {
        try {
            setContentView(R.layout.filmtool);

            ImageButton homeButton = (ImageButton) findViewById(R.id.ButtonHome);
            homeButton.setOnClickListener(OnClickChangeToHome());

            ImageButton settingsButton = (ImageButton) findViewById(R.id.ButtonSettings);
            settingsButton.setOnClickListener(OnClickChangeToSettings());

            ImageButton logList = (ImageButton) findViewById(R.id.ButtonLogList);
            logList.setOnClickListener(OnClickChangeToLogList());


            EditText editText = (EditText) findViewById(R.id.FilmIdText);
            editText.setText(filmCounter.getFilmId());
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        filmCounter.setFilmId(v.getText().toString());
                        dbHelper.updateFilmCounter(filmCounter);
                        filmCounter = dbHelper.getCurrentFilmCounter();
                        initView();
                        return true;
                    }

                    return false;
                }
            });

            int currentPause = 0;
            if (CollectionUtils.isNotEmpty(filmCounter.getPauses())) {
                List<FilmCounter.Pause> pauses = FilmCounter.PAUSE_ORDERING.reverse().sortedCopy(filmCounter.getPauses());
                currentPause = pauses.get(0).getPosition();
            }

            TextView counter = (TextView) findViewById(R.id.CounterText);
            counter.setText(currentPause + "");

            ImageButton next = (ImageButton) findViewById(R.id.IncreaseCounterButton);
            next.setOnClickListener(OnClickOnNext());
            ImageButton previous = (ImageButton) findViewById(R.id.DecreaseCounterButton);
            previous.setOnClickListener(OnClickOnPrevious());

            ImageButton send = (ImageButton) findViewById(R.id.UploadButton);
            send.setOnClickListener(OnClickOnSend());
        } catch (Throwable t) {
            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
    }

    View.OnClickListener OnClickChangeToHome()
    {
        return new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Main.class);
                intent.putExtra(Main.log_started, logStarted);
                startActivity(intent);
            }
        };
    }

    View.OnClickListener OnClickChangeToSettings()
    {
        return new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                intent.putExtra(Main.log_started, logStarted);
                startActivity(intent);
            }
        };
    }

    View.OnClickListener OnClickChangeToLogList()
    {
        return new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LogList.class);
                intent.putExtra(Main.log_started, logStarted);
                startActivity(intent);
            }
        };
    }

    View.OnClickListener OnClickOnSend() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.markFilmCountAsToBeSent(filmCounter.getId());
                AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(preferences.getOauth2Key(), preferences.getOauthSecret());
                try {
                    appEngineOAuthClient.pushFilmCounter(filmCounter);
                    dbHelper.markFilmCountAsInCloud(filmCounter.getId());
                    filmCounter = dbHelper.getCurrentFilmCounter();
                    initView();
                } catch (Throwable t) {
                    Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
                }

            }
        };

    }

    View.OnClickListener OnClickOnNext() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<FilmCounter.Pause> pauses = FilmCounter.PAUSE_ORDERING.reverse().sortedCopy(filmCounter.getPauses());
                int counter = 1;
                if (CollectionUtils.isNotEmpty(pauses)) counter = pauses.get(0).getPosition() + 1;
                FilmCounter.Pause pause = new FilmCounter.Pause();
                pause.setPosition(counter);
                pause.setPauseDateTime(DateTime.now().toDateTime(preferences.getCameraTimeZone()).toLocalDateTime());
                if (logStarted) {
                    Optional<LogRecording> logRecordingOptional = dbHelper.getCurrentLog();
                    if (!logRecordingOptional.isPresent()) return;
                    List<LogRecording.LogRecord> logRecords = LogRecording.DATE_ORDERING.reverse().sortedCopy(logRecordingOptional.get().getRecords());
                    if (CollectionUtils.isNotEmpty(logRecords)) {
                        pause.setLatitude(logRecords.get(0).getLatitude());
                        pause.setLongitude(logRecords.get(0).getLongitude());
                    } else {
                        pause.setLatitude(-1.0);
                        pause.setLongitude(-1.0);
                    }
                } else {
                    pause.setLatitude(-1.0);
                    pause.setLongitude(-1.0);
                }
                filmCounter.getPauses().add(pause);
                dbHelper.updateFilmCounter(filmCounter);
                filmCounter = dbHelper.getCurrentFilmCounter();

                initView();
            }
        };
    }

    View.OnClickListener OnClickOnPrevious() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<FilmCounter.Pause> pauses = FilmCounter.PAUSE_ORDERING.reverse().sortedCopy(filmCounter.getPauses());
                pauses.remove(0);
                filmCounter.setPauses(pauses);
                dbHelper.updateFilmCounter(filmCounter);
                filmCounter = dbHelper.getCurrentFilmCounter();
                initView();
            }
        };

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(Main.log_started)) logStarted = savedInstanceState.getBoolean(Main.log_started);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Main.log_started, logStarted);
    }

    private Preferences getPreferences() {
        return PreferencesUtils.getPreferences(this);
    }

    private void savePreferences() {
        PreferencesUtils.setSharedPreferences(this, preferences);
    }
}
