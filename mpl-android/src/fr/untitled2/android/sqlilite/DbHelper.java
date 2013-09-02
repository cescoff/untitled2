package fr.untitled2.android.sqlilite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import fr.untitled2.android.entities.UploadProcessStatus;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.utils.JSonParser;
import fr.untitled2.common.entities.FilmCounter;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.LogRecordingConstants;
import fr.untitled2.common.json.JSonMappings;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.DistanceUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/11/13
 * Time: 3:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyPictureLog.db";

    private static final String LOG_TABLE_NAME = "logs";

    private static final String FILM_COUNTER_TABLE_NAME = "filmcounter";

    private static final String UPLOAD_PROCESS_TABLE_NAME = "uploadprocess";

    private static final int DATABASE_VERSION = 1;

    private static final String COLUMN_ID = "_id";

    private static final String COLUMN_NAME = "name";

    private static final String COLUMN_POINTS_FILE = "pointsfile";

    private static final String COLUMN_STATUS = "status";

    private static final String COLUMN_TIME_ZONE = "timezone";

    private static final String COLUMN_PAUSES = "pauses";

    private static final String COLUMN_LAST_POINT_DATE = "lastpointdate";

    private static final String COLUMN_LAST_POINT_LATITUDE = "lastpointlat";

    private static final String COLUMN_LAST_POINT_LONGITUDE = "lastpointlon";

    private static final String COLUMN_POINT_COUNT = "pointcnt";

    private static final String COLUMN_DISTANCE = "distance";

    private static final String COLUMN_LAST_UPLOAD_DATE = "lastuploaddate";

    private static final String COLUMN_UPLOAD_LAST_POINT_LATITUDE = "uploadlastpointlat";

    private static final String COLUMN_UPLOAD_LAST_POINT_LONGITUDE = "uploadlastpointlon";

    private static final String UPLOAD_PROCESS_TABLE_CREATE = "create table "
            + UPLOAD_PROCESS_TABLE_NAME + "("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_LAST_UPLOAD_DATE + " text not null, "
                + COLUMN_UPLOAD_LAST_POINT_LATITUDE + " text not null, "
                + COLUMN_UPLOAD_LAST_POINT_LONGITUDE + " text not null)";

    private static final String LOG_TABLE_CREATE = "create table "
            + LOG_TABLE_NAME + "("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_STATUS + " text not null, "
                + COLUMN_NAME + " text not null, "
                + COLUMN_POINTS_FILE + " text, "
                + COLUMN_POINT_COUNT + " integer, "
                + COLUMN_DISTANCE + " integer, "
                + COLUMN_LAST_POINT_DATE + " text, "
                + COLUMN_LAST_POINT_LATITUDE + " text, "
                + COLUMN_LAST_POINT_LONGITUDE + " text, "
                + COLUMN_TIME_ZONE + " text not null);";

    private static final String LOG_STATUS_INDEX_CREATE = "create index statusindex on " + LOG_TABLE_NAME + "(" + COLUMN_STATUS + ");";

    private static final String FILM_COUNTER_TABLE_CREATE = "create table "
            + FILM_COUNTER_TABLE_NAME + "("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_STATUS + " text not null, "
                + COLUMN_PAUSES + " text not null);";

    private static final String FILM_COUNTER_INDEX_CREATE = "create index filmstatusindex on " + FILM_COUNTER_TABLE_NAME + "(" + COLUMN_STATUS + ");";

    private Preferences preferences;

    private Context context;

    public DbHelper(Context context, Preferences preferences) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.preferences = preferences;
        checkIfTablesExist();
        this.context = context;
    }

    private void checkIfTablesExist() {
        try {
            getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID}, null, null, null, null, null);
        } catch (Throwable t) {
            getWritableDatabase().execSQL(LOG_TABLE_CREATE);
            getWritableDatabase().execSQL(LOG_STATUS_INDEX_CREATE);
        }

        try {
            getReadableDatabase().query(UPLOAD_PROCESS_TABLE_NAME, new String[]{COLUMN_ID}, null, null, null, null, null);
        } catch (Throwable t) {
            getWritableDatabase().execSQL(UPLOAD_PROCESS_TABLE_CREATE);
        }

        try {
            getReadableDatabase().query(FILM_COUNTER_TABLE_NAME, new String[]{COLUMN_ID}, null, null, null, null, null);
        } catch (Throwable t) {
            getWritableDatabase().execSQL(FILM_COUNTER_TABLE_CREATE);
            getWritableDatabase().execSQL(FILM_COUNTER_INDEX_CREATE);
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOG_TABLE_CREATE);
        db.execSQL(LOG_STATUS_INDEX_CREATE);
        db.execSQL(FILM_COUNTER_TABLE_CREATE);
        db.execSQL(FILM_COUNTER_INDEX_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DbHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + LOG_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FILM_COUNTER_TABLE_NAME);
        onCreate(db);
    }

    public boolean hasCurrentLog() {
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TIME_ZONE, COLUMN_NAME, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_POINTS_FILE}, COLUMN_STATUS + "=?", new String[]{LogStatus.log_in_progress.toString()}, null, null, null);
        cursor.moveToFirst();
        return !cursor.isAfterLast();
    }

    public Optional<LogRecording> getCurrentLog() {
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TIME_ZONE, COLUMN_NAME, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_POINTS_FILE}, COLUMN_STATUS + "=?", new String[]{LogStatus.log_in_progress.toString()}, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            LogRecording result = getLogRecordingFromCursor(cursor);
            return Optional.of(result);
        } else {
            return Optional.absent();
        }
    }

    public FilmCounter getCurrentFilmCounter() {
        Cursor cursor = getReadableDatabase().query(FILM_COUNTER_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_STATUS, COLUMN_PAUSES}, COLUMN_STATUS + "=?", new String[]{FilmCounterStatus.in_progress.toString()}, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            FilmCounter result = getFilmCounterFromCursor(cursor);
            return result;
        } else {
            return createNewFilmCounterInProgress();
        }
    }

    public Collection<LogRecording> getLogRecordingToBeSentToCloud() {
        Collection<LogRecording> result = Lists.newArrayList();
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TIME_ZONE, COLUMN_NAME, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_POINTS_FILE}, COLUMN_STATUS + "=?", new String[]{LogStatus.to_be_sent_to_cloud.toString()}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result.add(getLogRecordingFromCursor(cursor));
            cursor.moveToNext();
        }
        return result;
    }

    public void addRecordToCurrentLog(LogRecording.LogRecord logRecord) throws Exception {
        Optional<LogRecording> currentLogRecordingOption = getCurrentLog();

        if (!currentLogRecordingOption.isPresent()) throw new Exception("Aucun log est en cours d'enregistrement");

        LogRecording currentLogRecording = currentLogRecordingOption.get();
        FileOutputStream fileOutputStream = context.openFileOutput(getLogRecordingFileName(currentLogRecording) + "-" + currentLogRecording.getId(), Context.MODE_APPEND);
        fileOutputStream.write(logRecord.toLineString().getBytes());
        fileOutputStream.close();
        if (currentLogRecording.getPointCount() == null) currentLogRecording.setPointCount(0);
        currentLogRecording.setPointCount(currentLogRecording.getPointCount() + 1);

        if (currentLogRecording.getDistance() == null) currentLogRecording.setDistance(0.0);

        if (currentLogRecording.getLastLogRecord() != null) currentLogRecording.setDistance(currentLogRecording.getDistance() + new Double(DistanceUtils.getDistance(new Pair<Double, Double>(currentLogRecording.getLastLogRecord().getLatitude(), currentLogRecording.getLastLogRecord().getLongitude()), new Pair<Double, Double>(logRecord.getLatitude(), logRecord.getLongitude()))));
        else currentLogRecording.setDistance(0.0);

        currentLogRecording.setLastLogRecord(logRecord);
        getWritableDatabase().update(LOG_TABLE_NAME, getContentValuesFromLogRecording(currentLogRecording, logRecord, LogStatus.log_in_progress, true), COLUMN_ID + "=?", new String[]{currentLogRecording.getId() + ""});
    }

    public void markCurrentLogAsToBeSent() throws Exception {
        Optional<LogRecording> logRecordingOption = getCurrentLog();
        if (!logRecordingOption.isPresent()) throw new Exception("Aucune log est en cours d'enregistrement");
        markLogasToBeSent(logRecordingOption.get());
    }

    public void markLogasToBeSent(LogRecording logRecording) {
        markLogasToBeSent(logRecording.getId());
    }

    public void markLogasToBeSent(long logRecordingId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, LogStatus.to_be_sent_to_cloud.toString());
        getWritableDatabase().update(LOG_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{logRecordingId + ""});
    }

    public void markFilmCountAsToBeSent(long filmCounterId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, FilmCounterStatus.to_be_sent_to_cloud.toString());
        getWritableDatabase().update(FILM_COUNTER_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{filmCounterId + ""});
    }

    public void markFilmCountAsInCloud(long filmCounterId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, FilmCounterStatus.in_cloud.toString());
        getWritableDatabase().update(FILM_COUNTER_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{filmCounterId + ""});
    }

    public List<LogRecordingWithStatus> getLogRecordingWithStatus() {
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_STATUS, COLUMN_TIME_ZONE, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_POINTS_FILE}, null, null, null, null, null);
        cursor.moveToFirst();
        List<LogRecordingWithStatus> result = Lists.newArrayList();
        while (!cursor.isAfterLast()) {
            LogRecording logRecording = getLogRecordingFromCursor(cursor);
            LogRecordingWithStatus logRecordingWithStatus = new LogRecordingWithStatus(logRecording, LogStatus.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS))));
            result.add(logRecordingWithStatus);
            cursor.moveToNext();
        }
        return result;
    }

    public void deleteRecording(long id) {
        getWritableDatabase().delete(LOG_TABLE_NAME, COLUMN_ID + "=?", new String[]{"" + id});
    }

    public void deleteRecording(LogRecording logRecording) {
        deleteRecording(logRecording.getId());
    }

    public void markLogRecordingAsSynchronizedWithCloud(Long logRecordingId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, LogStatus.in_cloud.toString());
        getWritableDatabase().update(LOG_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{"" + logRecordingId});
    }

    public void markLogRecordingAsUploadInProgress(Long logRecordingId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, LogStatus.upload_in_progress.toString());
        getWritableDatabase().update(LOG_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{"" + logRecordingId});
    }

    public void updateFilmCounter(FilmCounter filmCounter) {
        ContentValues contentValues = getContentValuesFromFilmCounter(filmCounter, FilmCounterStatus.in_progress);
        getWritableDatabase().update(FILM_COUNTER_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{filmCounter.getId() + ""});
    }

    public LogRecording getLogRecordingFromId(Long id) {
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_TIME_ZONE, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_POINTS_FILE}, COLUMN_ID + "=?", new String[]{id + ""}, null, null, null);
        cursor.moveToFirst();
        if (cursor.isAfterLast()) return null;
        LogRecording logRecording = getLogRecordingFromCursor(cursor);
        return logRecording;
    }

    public LogRecording createNewLogInProgress() {
        LogRecording result = new LogRecording();
        result.setName(preferences.getDateTimeFormatter().print(DateTime.now()));
        result.setDateTimeZone(DateTimeZone.forTimeZone(TimeZone.getDefault()).getID());

        try {
            Long id = getWritableDatabase().insertOrThrow(LOG_TABLE_NAME, null, getContentValuesFromLogRecording(result, null, LogStatus.log_in_progress, false));
            result.setId(id);
        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            Log.e(getClass().getName(), stackTrace);
        }

        return result;
    }

    public Optional<UploadProcessStatus> getLastUploadStatus() {
        Cursor cursor = getReadableDatabase().query(UPLOAD_PROCESS_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_LAST_UPLOAD_DATE, COLUMN_UPLOAD_LAST_POINT_LATITUDE, COLUMN_UPLOAD_LAST_POINT_LONGITUDE}, null, null, null, null, null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            Long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            DateTime uploadDate = new DateTime(cursor.getString(cursor.getColumnIndex(COLUMN_LAST_UPLOAD_DATE)));
            double latitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_UPLOAD_LAST_POINT_LATITUDE)));
            double longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_UPLOAD_LAST_POINT_LONGITUDE)));

            return Optional.of(new UploadProcessStatus(id, uploadDate, latitude, longitude));
        } else {
            return Optional.absent();
        }
    }

    public void notifyUploadDone(DateTime uploadDate, double latitude, double longitude) {
        Cursor cursor = getReadableDatabase().query(UPLOAD_PROCESS_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_LAST_UPLOAD_DATE, COLUMN_UPLOAD_LAST_POINT_LATITUDE, COLUMN_UPLOAD_LAST_POINT_LONGITUDE}, null, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.isAfterLast()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_LAST_UPLOAD_DATE, uploadDate.toString());
            contentValues.put(COLUMN_UPLOAD_LAST_POINT_LATITUDE, latitude);
            contentValues.put(COLUMN_UPLOAD_LAST_POINT_LONGITUDE, longitude);
            getWritableDatabase().insert(UPLOAD_PROCESS_TABLE_NAME, null, contentValues);
        } else {
            ContentValues contentValues = new ContentValues();
            Long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            contentValues.put(COLUMN_LAST_UPLOAD_DATE, uploadDate.toString());
            contentValues.put(COLUMN_UPLOAD_LAST_POINT_LATITUDE, latitude);
            contentValues.put(COLUMN_UPLOAD_LAST_POINT_LONGITUDE, longitude);
            getWritableDatabase().update(UPLOAD_PROCESS_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{"" + id});
        }
    }

    public void markUploadProcessAsStopped() {
        Optional<UploadProcessStatus> uploadProcessStatusOptional = getLastUploadStatus();
        if (uploadProcessStatusOptional.isPresent()) {
            long id = uploadProcessStatusOptional.get().getId();
            getWritableDatabase().delete(UPLOAD_PROCESS_TABLE_NAME, COLUMN_ID + "=?", new String[]{"" + id});
        }
    }

    private FilmCounter createNewFilmCounterInProgress() {
        FilmCounter result = new FilmCounter();
        result.setFilmId(preferences.getDateTimeFormatter().print(DateTime.now()));


        Long id = getWritableDatabase().insert(FILM_COUNTER_TABLE_NAME, null, getContentValuesFromFilmCounter(result, FilmCounterStatus.in_progress));
        result.setId(id);

        return result;
    }

    private ContentValues getContentValuesFromLogRecording(LogRecording logRecording, LogRecording.LogRecord lastLogRecord, LogStatus logStatus, boolean hasRecordings) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, logRecording.getName());
        contentValues.put(COLUMN_TIME_ZONE, logRecording.getDateTimeZone());
        contentValues.put(COLUMN_STATUS, logStatus.toString());
        if (hasRecordings) contentValues.put(COLUMN_POINTS_FILE, getLogRecordingFileName(logRecording) + "-" + logRecording.getId());
        if (lastLogRecord != null) {
            contentValues.put(COLUMN_LAST_POINT_DATE, lastLogRecord.getDateTime().toString());
            contentValues.put(COLUMN_LAST_POINT_LATITUDE, lastLogRecord.getLatitude());
            contentValues.put(COLUMN_LAST_POINT_LONGITUDE, lastLogRecord.getLongitude());
        }
        if (logRecording.getDistance() != null) contentValues.put(COLUMN_DISTANCE, logRecording.getDistance());
        else contentValues.put(COLUMN_DISTANCE, 0.0);

        if (logRecording.getPointCount() != null) contentValues.put(COLUMN_POINT_COUNT, logRecording.getPointCount());
        else contentValues.put(COLUMN_POINT_COUNT, 0);

        return contentValues;
    }

    private ContentValues getContentValuesFromFilmCounter(FilmCounter filmCounter, FilmCounterStatus filmCounterStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, filmCounterStatus.toString());
        contentValues.put(COLUMN_PAUSES, JSonMappings.getJSON(filmCounter));
        return contentValues;
    }

    private LogRecording getLogRecordingFromCursor(Cursor cursor) {
        LogRecording logRecording = new LogRecording();
        logRecording.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        logRecording.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
        logRecording.setDateTimeZone(cursor.getString(cursor.getColumnIndex(COLUMN_TIME_ZONE)));
        String fileName = cursor.getString(cursor.getColumnIndex(COLUMN_POINTS_FILE));

        String lastPointDate = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_POINT_DATE));
        String lastPointLatitude = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_POINT_LATITUDE));
        String lastPointLongitude = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_POINT_LONGITUDE));

        LogRecording.LogRecord lastLogRecord = null;
        if (StringUtils.isNotEmpty(lastPointDate) && StringUtils.isNotEmpty(lastPointLatitude) && StringUtils.isNotEmpty(lastPointLongitude)) {
            lastLogRecord = new LogRecording.LogRecord();
            lastLogRecord.setLatitude(Double.parseDouble(lastPointLatitude));
            lastLogRecord.setLongitude(Double.parseDouble(lastPointLongitude));
            lastLogRecord.setDateTime(new LocalDateTime(lastPointDate));
            lastLogRecord.setLogRecordingId(logRecording.getId());
            logRecording.setLastLogRecord(lastLogRecord);
            logRecording.setPointCount(cursor.getInt(cursor.getColumnIndex(COLUMN_POINT_COUNT)));
            logRecording.setDistance(cursor.getDouble(cursor.getColumnIndex(COLUMN_DISTANCE)));
        }

        if (StringUtils.isNotEmpty(fileName)) {
            try {
                logRecording.setRecordingsFileInputStream(context.openFileInput(fileName));
            } catch (FileNotFoundException e) {
                logRecording.setRecordingsFileInputStream(null);
            }
        }
        return logRecording;
    }

    private FilmCounter getFilmCounterFromCursor(Cursor cursor) {
        String json = cursor.getString(cursor.getColumnIndex(COLUMN_PAUSES));
        FilmCounter filmCounter = JSonMappings.getFilmCounter(json);
        filmCounter.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));

        return filmCounter;
    }

    private String getLogRecordingFileName(LogRecording logRecording) {
        return StringUtils.replace(logRecording.getName(), "/", "-");
    }

    public enum LogStatus {
        upload_in_progress,
        in_cloud,
        log_in_progress,
        to_be_sent_to_cloud
    }

    public enum FilmCounterStatus {
        in_progress,
        to_be_sent_to_cloud,
        in_cloud
    }

}
