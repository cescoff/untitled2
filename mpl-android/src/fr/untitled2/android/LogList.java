package fr.untitled2.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import fr.untitled2.android.sqlilite.LogRecordingWithStatus;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.ApiUtils;
import fr.untitled2.android.utils.NetUtils;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.utils.CollectionUtils;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.commons.lang.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class LogList extends Activity {

    private static final String current_view = "currentView";
    private static final String log_to_delete = "currentLogToDelete";
    public static final String selected_log_id = "selectedLogId";

    private Preferences preferences;

    private DbHelper dbHelper;

    private long logIdToBeDeleted;

    private int currentViewId = R.layout.loglist;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        try {
            super.onCreate(savedInstanceState);

            this.preferences = getPreferences();
            this.dbHelper = new DbHelper(getApplicationContext(), preferences);
        } catch (Throwable t) {
            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
    }

    private void initListView() {
        try {
            setContentView(R.layout.loglist);
            ListView logListView = (ListView) findViewById(R.id.LogListView);
            logListView.setAdapter(Statuses(getLogRecordings()));
            ImageButton homeButton = (ImageButton) findViewById(R.id.ButtonHome);
            homeButton.setOnClickListener(OnClickChangeToHome());

            ImageButton settingsButton = (ImageButton) findViewById(R.id.ButtonSettings);
            settingsButton.setOnClickListener(OnClickChangeToSettings());

            ImageButton filmToolButton = (ImageButton) findViewById(R.id.ButtonFilmTools);
            if (!preferences.isFilmToolEnabled()) filmToolButton.setVisibility(View.GONE);
            else filmToolButton.setOnClickListener(OnClickToFilmTool());
        } catch (Throwable t) {
            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
        }
    }

    private LogRecordingWithStatus[] getLogRecordings() throws FileNotFoundException {
        List<LogRecordingWithStatus> logListArrayAdapters = Ordering.natural().reverse().onResultOf(new Function<LogRecordingWithStatus, Long>() {
            @Override
            public Long apply(LogRecordingWithStatus logRecordingWithStatus) {
                return logRecordingWithStatus.getLogRecording().getId();
            }
        }).sortedCopy(dbHelper.getLogRecordingWithStatus());
        if (CollectionUtils.isNotEmpty(logListArrayAdapters)) {
            logListArrayAdapters = Lists.partition(logListArrayAdapters, 30).get(0);
            LogRecordingWithStatus[] logRecordingWithStatuses = new LogRecordingWithStatus[logListArrayAdapters.size()];

            for (int index = 0; index < logRecordingWithStatuses.length; index++) {
                logRecordingWithStatuses[index] = logListArrayAdapters.get(index);
            }
            return logRecordingWithStatuses;
        }
        return new LogRecordingWithStatus[0];
    }

    ArrayAdapter<LogRecordingWithStatus> Statuses(final LogRecordingWithStatus[] recordings) {

        return new ArrayAdapter<LogRecordingWithStatus>(getApplicationContext(), R.layout.loglistrow, recordings) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.loglistrow, parent, false);
                TextView textView = (TextView) rowView.findViewById(R.id.LogListLabel);
                ImageView uploadButton = (ImageView) rowView.findViewById(R.id.LogListIcon);

                String name = recordings[position].getLogRecording().getName();
                if (recordings[position].getLogRecording().getPointCount() != null)  name += " (" + recordings[position].getLogRecording().getPointCount() + ")";
                if (recordings[position].getLogRecording().getDistance() != null) {
                    double distance = recordings[position].getLogRecording().getDistance();
                    if (distance > 1000) name += " " + new Double(distance / 1000) + "km";
                    else name += " " + new Double(distance).intValue() + "m";
                }
                textView.setText(name);

                // Change the icon for Windows and iPhone
                if (recordings[position].getStatus() == DbHelper.LogStatus.to_be_sent_to_cloud) {
                    uploadButton.setImageResource(R.drawable.up_arrow);
                    uploadButton.setOnClickListener(OnClickOnLogToUpload(recordings[position].getLogRecording()));
                } else if (recordings[position].getStatus() == DbHelper.LogStatus.log_in_progress) uploadButton.setImageResource(R.drawable.padlock);
                else if (recordings[position].getStatus() == DbHelper.LogStatus.in_cloud) uploadButton.setImageResource(R.drawable.cloudy);
                else if (recordings[position].getStatus() == DbHelper.LogStatus.upload_in_progress) uploadButton.setImageResource(R.drawable.rss);

                ImageButton deleteButton = (ImageButton) rowView.findViewById(R.id.LogListDeleteButton);
                deleteButton.setImageResource(R.drawable.recycle_bin);
                deleteButton.setOnClickListener(OnClickOnDelete(recordings[position].getLogRecording(), rowView));
                if (recordings[position].getStatus() == DbHelper.LogStatus.log_in_progress) deleteButton.setVisibility(View.GONE);
                return rowView;
            }

        };

    }

    public boolean isConnected() {
        return NetUtils.isConnected(this);
    }

    AdapterView.OnClickListener OnClickOnLogToUpload(final LogRecording logRecording) {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUploadAlertDialog(logRecording).show();
            }
        };

    }

    private AlertDialog getUploadAlertDialog(final LogRecording logRecording) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(preferences.getTranslation(I18nConstants.loglist_uploadalerttitle));
        alertDialogBuilder.setPositiveButton(preferences.getTranslation(I18nConstants.loglist_uploadyes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    AsyncTask<Long, Integer, Integer> asyncTask = UploadLog();
                    asyncTask.execute(logRecording.getId());
//                    asyncTask.get();
                    initListView();
                } catch (Throwable t) {
                    Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
                    displayErrorDialog();
                }
            }
        });
        alertDialogBuilder.setNegativeButton(preferences.getTranslation(I18nConstants.loglist_uploadno), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initListView();
            }
        });
        return alertDialogBuilder.create();
    }

    private AlertDialog getUploadOkAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(preferences.getTranslation(I18nConstants.loglist_uploadoktitle));
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initListView();
            }
        });
        return alertDialogBuilder.create();
    }

    private AlertDialog getNotConnectedAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(preferences.getTranslation(I18nConstants.loglist_notconnected));
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initListView();
            }
        });
        return alertDialogBuilder.create();
    }

    AdapterView.OnClickListener OnClickOnDelete(final LogRecording logRecording, final View line) {

        return new AdapterView.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    logIdToBeDeleted = logRecording.getId();
                    getDeleteAlertDialog().show();
                } catch (Throwable t) {
                    Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t)) ;
                }
            }
        };

    }

    private AlertDialog getDeleteAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(preferences.getTranslation(I18nConstants.loglist_delete_question));
        alertDialogBuilder.setPositiveButton(preferences.getTranslation(I18nConstants.loglist_delete_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (logIdToBeDeleted >= 0) dbHelper.deleteRecording(logIdToBeDeleted);
                initListView();
            }
        });
        alertDialogBuilder.setNegativeButton(preferences.getTranslation(I18nConstants.loglist_delete_button_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initListView();
            }
        });
        return alertDialogBuilder.create();
    }

    AdapterView.OnClickListener OnClickNoOnDelete() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initListView();
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
        if (currentViewId == R.layout.loglist) initListView();
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

    AsyncTask<Long, Integer, Integer> UploadLog() {

        return new AsyncTask<Long, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Long... params) {
                try {
                    Looper.prepare();
                    if (!isConnected()) {
                        setProgress(10000);
                        getNotConnectedAlert().show();
                        return 0;
                    }
                    long logRecordingId = params[0];
                    dbHelper.markLogRecordingAsUploadInProgress(logRecordingId);
                    publishProgress(1000);
                    initListView();
                    if (!preferences.isConnected()) {
                        publishProgress(10000);
                        Intent intent = new Intent(getApplicationContext(), Connect.class);
                        intent.putExtra(selected_log_id, logRecordingId);
                        startActivity(intent);
                    } else {
                        publishProgress(3000);
                        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(preferences.getOauth2Key(), preferences.getOauthSecret());
                        publishProgress(4000);
                        try {
                            appEngineOAuthClient.pushLogRecording(dbHelper.getLogRecordingFromId(logRecordingId));
                            publishProgress(8000);
                            dbHelper.markLogRecordingAsSynchronizedWithCloud(logRecordingId);
                            publishProgress(10000);
                        } catch (Throwable throwable) {
                            publishProgress(10000);
                            dbHelper.markLogasToBeSent(logRecordingId);
                            Log.e(getLocalClassName(), Throwables.getStackTraceAsString(throwable));
                            return -1;
                        }
                    }
                    return 0;
                } catch (Throwable t) {
                    Log.e(getLocalClassName(), Throwables.getStackTraceAsString(t));
                    return -1;
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                setProgressFromAsynchTask(values[0]);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                if (integer < 0) {
                    displayErrorDialog();
                } else {
                    getUploadOkAlertDialog().show();
                }
            }
        };
    }

    private void displayErrorDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(preferences.getTranslation(I18nConstants.loglist_erroralerttitle));
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initListView();
            }
        });
        alertDialogBuilder.create().show();
    }

    private void setProgressFromAsynchTask(int progress) {
        setProgress(progress);
    }

}
