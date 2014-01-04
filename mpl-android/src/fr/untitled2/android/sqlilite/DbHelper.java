package fr.untitled2.android.sqlilite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import fr.untitled2.android.entities.UploadProcessStatus;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.common.entities.FilmCounter;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.WifiDetection;
import fr.untitled2.common.json.JSonMappings;
import fr.untitled2.common.utils.DateTimeUtils;
import fr.untitled2.common.utils.DistanceUtils;
import fr.untitled2.utils.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    private static final String ERROR_REPORT_TABLE_NAME = "errorreports";

    private static final String LOG_TABLE_NAME = "logs";

    private static final String FILM_COUNTER_TABLE_NAME = "filmcounter";

    private static final String UPLOAD_PROCESS_TABLE_NAME = "uploadprocess";

    private static final String KNOWNLOCATION_TABLE_NAME = "knownlocations";

    private static final String WIFI_TABLE_NAME = "wifis";

    private static final String SENSOR_TABLE_NAME = "sensors";

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

    private static final String COLUMN_LAST_POINT_ALTITUDE = "lastpointalt";

    private static final String COLUMN_POINT_COUNT = "pointcnt";

    private static final String COLUMN_DISTANCE = "distance";

    private static final String COLUMN_LAST_UPLOAD_DATE = "lastuploaddate";

    private static final String COLUMN_UPLOAD_LAST_POINT_LATITUDE = "uploadlastpointlat";

    private static final String COLUMN_UPLOAD_LAST_POINT_LONGITUDE = "uploadlastpointlon";

    private static final String COLUMN_WIFI_STABLE = "stable";

    private static final String COLUMN_WIFI_NAME = "name";

    private static final String COLUMN_WIFI_DETECTION_DATETIME = "detectiondatetime";

    private static final String COLUMN_KNOWNLOCATION_NAME = "name";

    private static final String COLUMN_KNOWNLOCATION_LATITUDE = "latitude";

    private static final String COLUMN_KNOWNLOCATION_LONGITUDE = "longitude";

    private static final String COLUMN_KNOWNLOCATION_DISTANCE = "distance";

    private static final String COLUMN_KNOWNLOCATION_DATETIME = "datetime";

    private static final String COLUMN_SENSOR_DATETIME = "datetime";

    private static final String COLUMN_SENSOR_TEMPERATURE = "temperature";

    private static final String COLUMN_SENSOR_PRESSURE = "pressure";

    private static final String COLUMN_ERRORREPORT_MESSAGE = "message";

    private static final String COLUMN_ERRORREPORT_CLASS = "className";

    private static final String COLUMN_ERRORREPORT_STACKTRACE = "stackTrace";

    private static final String SENSOR_TABLE_CREATE = "create table "
            + SENSOR_TABLE_NAME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_SENSOR_DATETIME + " text not null, "
            + COLUMN_SENSOR_PRESSURE + " text not null, "
            + COLUMN_SENSOR_TEMPERATURE + " text not null)";

    private static final String ERRORREPORT_TABLE_CREATE = "create table "
            + ERROR_REPORT_TABLE_NAME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_ERRORREPORT_CLASS + " text not null, "
            + COLUMN_ERRORREPORT_MESSAGE + " text not null, "
            + COLUMN_ERRORREPORT_STACKTRACE + " text not null)";

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
                + COLUMN_LAST_POINT_ALTITUDE + " text, "
                + COLUMN_TIME_ZONE + " text not null);";

    private static final String LOG_STATUS_INDEX_CREATE = "create index statusindex on " + LOG_TABLE_NAME + "(" + COLUMN_STATUS + ");";

    private static final String FILM_COUNTER_TABLE_CREATE = "create table "
            + FILM_COUNTER_TABLE_NAME + "("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_STATUS + " text not null, "
                + COLUMN_PAUSES + " text not null);";

    private static final String FILM_COUNTER_INDEX_CREATE = "create index filmstatusindex on " + FILM_COUNTER_TABLE_NAME + "(" + COLUMN_STATUS + ");";

    private static final String KNOWNLOCATION_TABLE_CREATE = "create table "
            + KNOWNLOCATION_TABLE_NAME + "("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_KNOWNLOCATION_DATETIME + " text not null, "
                + COLUMN_KNOWNLOCATION_NAME + " text not null, "
                + COLUMN_KNOWNLOCATION_LATITUDE + " text not null, "
                + COLUMN_KNOWNLOCATION_LONGITUDE + " text not null);";

    private static final String WIFIS_TABLE_CREATE = "create table "
            + WIFI_TABLE_NAME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_WIFI_NAME + " text not null, "
            + COLUMN_WIFI_DETECTION_DATETIME + " text not null, "
            + COLUMN_WIFI_STABLE + " text not null);";

    private Preferences preferences;

    private Context context;

    public DbHelper(Context context, Preferences preferences) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.preferences = preferences;
        checkIfTablesExist();
        this.context = context;
    }

    private void checkIfTablesExist() {
        //TODO delete it
/*        try {
            getWritableDatabase().execSQL("ALTER TABLE " + LOG_TABLE_NAME + " ADD COLUMN " + COLUMN_LAST_POINT_ALTITUDE + " text");
        } catch (Throwable t) {
        }
        try {
            getWritableDatabase().execSQL("ALTER TABLE " + KNOWNLOCATION_TABLE_NAME + " ADD COLUMN " + COLUMN_KNOWNLOCATION_DISTANCE + " text");
        } catch (Throwable t) {
            Log.e("Oups", Throwables.getStackTraceAsString(t));
        }*/

/*
        try {
            getWritableDatabase().execSQL("DROP TABLE " + WIFI_TABLE_NAME);
        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            Log.e(getClass().getName(), stackTrace);
        }
*/


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

        try {
            getReadableDatabase().query(WIFI_TABLE_NAME, new String[]{COLUMN_ID}, null, null, null, null, null);
        } catch (Throwable t) {
            getWritableDatabase().execSQL(WIFIS_TABLE_CREATE);
        }

        try {
            getReadableDatabase().query(KNOWNLOCATION_TABLE_NAME, new String[]{COLUMN_ID}, null, null, null, null, null);
        } catch (Throwable t) {
            getWritableDatabase().execSQL(KNOWNLOCATION_TABLE_CREATE);
        }

        try {
            getReadableDatabase().query(SENSOR_TABLE_NAME, new String[]{COLUMN_ID}, null, null, null, null, null);
        } catch (Throwable t) {
            getWritableDatabase().execSQL(SENSOR_TABLE_CREATE);
        }

        try {
            getReadableDatabase().query(ERROR_REPORT_TABLE_NAME, new String[]{COLUMN_ID}, null, null, null, null, null);
        } catch (Throwable t) {
            getWritableDatabase().execSQL(ERRORREPORT_TABLE_CREATE);
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
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TIME_ZONE, COLUMN_NAME, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_LAST_POINT_ALTITUDE, COLUMN_POINTS_FILE}, COLUMN_STATUS + "=?", new String[]{LogStatus.log_in_progress.toString()}, null, null, null);
        cursor.moveToFirst();
        return !cursor.isAfterLast();
    }

    public Optional<LogRecording> getCurrentLog() {
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TIME_ZONE, COLUMN_NAME, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_LAST_POINT_ALTITUDE, COLUMN_POINTS_FILE}, COLUMN_STATUS + "=?", new String[]{LogStatus.log_in_progress.toString()}, null, null, null);
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
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TIME_ZONE, COLUMN_NAME, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_LAST_POINT_ALTITUDE, COLUMN_POINTS_FILE}, COLUMN_STATUS + "=?", new String[]{LogStatus.to_be_sent_to_cloud.toString()}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result.add(getLogRecordingFromCursor(cursor));
            cursor.moveToNext();
        }
        return result;
    }

    public void addRecordToCurrentLog(LogRecording.LogRecord logRecord, Optional<KnownLocation> knownLocationOptional) throws Exception {
        Optional<LogRecording> currentLogRecordingOption = getCurrentLog();

        if (!currentLogRecordingOption.isPresent()) throw new Exception("Aucun log est en cours d'enregistrement");

        LogRecording currentLogRecording = currentLogRecordingOption.get();
        FileOutputStream fileOutputStream = context.openFileOutput(getLogRecordingFileName(currentLogRecording) + "-" + currentLogRecording.getId(), Context.MODE_APPEND);
        fileOutputStream.write(logRecord.toLineString().getBytes());
        fileOutputStream.close();
        if (currentLogRecording.getPointCount() == null) currentLogRecording.setPointCount(0);
        currentLogRecording.setPointCount(currentLogRecording.getPointCount() + 1);

        if (currentLogRecording.getDistance() == null) currentLogRecording.setDistance(0.0);

        double distanceToLastLogRecord = 0.0;



        if (currentLogRecording.getLastLogRecord() != null) {
            distanceToLastLogRecord = new Double(DistanceUtils.getDistance(currentLogRecording.getLastLogRecord().getLatitudeAndLongitude(), logRecord.getLatitudeAndLongitude()));
            currentLogRecording.setDistance(currentLogRecording.getDistance() + distanceToLastLogRecord);
        } else currentLogRecording.setDistance(0.0);

        if (!knownLocationOptional.isPresent()) {
            Optional<KnownLocationWithDatetime> lastKnownLocationOptional = getLastKnownLocation();
            if (lastKnownLocationOptional.isPresent()) {
                KnownLocationWithDatetime lastKnownLocation = lastKnownLocationOptional.get();
                lastKnownLocation.setDistance(lastKnownLocation.getDistance() + distanceToLastLogRecord);
                updateKnownLocation(lastKnownLocation);
            }
        } else {
            addKnownLocation(DateTimeUtils.getCurrentDateTimeInUTC(), knownLocationOptional.get());
        }

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
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_STATUS, COLUMN_TIME_ZONE, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_LAST_POINT_ALTITUDE, COLUMN_POINTS_FILE}, null, null, null, null, null);
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
        Cursor cursor = getReadableDatabase().query(LOG_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_TIME_ZONE, COLUMN_POINT_COUNT, COLUMN_DISTANCE, COLUMN_LAST_POINT_DATE, COLUMN_LAST_POINT_LATITUDE, COLUMN_LAST_POINT_LONGITUDE, COLUMN_LAST_POINT_ALTITUDE, COLUMN_POINTS_FILE}, COLUMN_ID + "=?", new String[]{id + ""}, null, null, null);
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

    public void updateKnownLocation(KnownLocationWithDatetime knownLocationWithDatetime) {
        getWritableDatabase().update(KNOWNLOCATION_TABLE_NAME, getContentValuesFromKnownLocation(knownLocationWithDatetime.getPointDate(), knownLocationWithDatetime.getDistance(), knownLocationWithDatetime.getKnownLocation()), COLUMN_ID + "=?", new String[]{"" + knownLocationWithDatetime.getId()});
    }

    public void addKnownLocation(LocalDateTime dateTime, KnownLocation knownLocation) {
        getWritableDatabase().insertOrThrow(KNOWNLOCATION_TABLE_NAME, null, getContentValuesFromKnownLocation(dateTime, 0.0, knownLocation));

        Cursor cursor = getReadableDatabase().query(KNOWNLOCATION_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_KNOWNLOCATION_NAME, COLUMN_KNOWNLOCATION_LATITUDE, COLUMN_KNOWNLOCATION_LONGITUDE, COLUMN_KNOWNLOCATION_DATETIME}, null, null, null, null, null);
        cursor.moveToFirst();
        List<KnownLocationWithDatetime> result = Lists.newArrayList();
        List<Integer> idsToDelete = Lists.newArrayList();
        while (!cursor.isAfterLast()) {
            KnownLocationWithDatetime knownLocationWithDatetime = getKnownLocationFromCursor(cursor);
            if (knownLocationWithDatetime.getPointDate().isBefore(LocalDateTime.now().minusHours(24))) {
                idsToDelete.add(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            }
            cursor.moveToNext();
        }
        for (Integer id : idsToDelete) {
            getWritableDatabase().delete(KNOWNLOCATION_TABLE_NAME, COLUMN_ID + "=?", new String[]{"" + id});
        }
    }

    public List<KnownLocationWithDatetime> getKnownLocations() {
        Cursor cursor = getReadableDatabase().query(KNOWNLOCATION_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_KNOWNLOCATION_NAME, COLUMN_KNOWNLOCATION_LATITUDE, COLUMN_KNOWNLOCATION_LONGITUDE, COLUMN_KNOWNLOCATION_DATETIME, COLUMN_KNOWNLOCATION_DISTANCE}, null, null, null, null, null);
        cursor.moveToFirst();
        List<KnownLocationWithDatetime> result = Lists.newArrayList();
        while (!cursor.isAfterLast()) {
            KnownLocationWithDatetime knownLocationWithDatetime = getKnownLocationFromCursor(cursor);
            result.add(knownLocationWithDatetime);
            cursor.moveToNext();
        }
        return result;
    }

    public Optional<KnownLocationWithDatetime> getLastKnownLocation() {
        List<KnownLocationWithDatetime> knownLocationWithDatetimes = getKnownLocations();
        knownLocationWithDatetimes = Ordering.natural().reverse().onResultOf(new Function<KnownLocationWithDatetime, LocalDateTime>() {
            @Override
            public LocalDateTime apply(KnownLocationWithDatetime knownLocationWithDatetime) {
                return knownLocationWithDatetime.getPointDate();
            }
        }).sortedCopy(knownLocationWithDatetimes);
        if (CollectionUtils.isNotEmpty(knownLocationWithDatetimes)) return Optional.of(knownLocationWithDatetimes.get(0));
        return Optional.absent();
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
            if (lastLogRecord.getKnownLocation() == null) {
                contentValues.put(COLUMN_LAST_POINT_LATITUDE, lastLogRecord.getLatitude());
                contentValues.put(COLUMN_LAST_POINT_LONGITUDE, lastLogRecord.getLongitude());
                contentValues.put(COLUMN_LAST_POINT_ALTITUDE, lastLogRecord.getAltitude());
            } else {
                contentValues.put(COLUMN_LAST_POINT_LATITUDE, lastLogRecord.getKnownLocation().getLatitude());
                contentValues.put(COLUMN_LAST_POINT_LONGITUDE, lastLogRecord.getKnownLocation().getLongitude());
                contentValues.put(COLUMN_LAST_POINT_ALTITUDE, lastLogRecord.getKnownLocation().getAltitude());
            }
        }
        if (logRecording.getDistance() != null) contentValues.put(COLUMN_DISTANCE, logRecording.getDistance());
        else contentValues.put(COLUMN_DISTANCE, 0.0);

        if (logRecording.getPointCount() != null) contentValues.put(COLUMN_POINT_COUNT, logRecording.getPointCount());
        else contentValues.put(COLUMN_POINT_COUNT, 0);

        return contentValues;
    }

    private ContentValues getContentValuesFromKnownLocation(LocalDateTime dateTime, double distance, KnownLocation knownLocation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_KNOWNLOCATION_NAME, knownLocation.getName());
        contentValues.put(COLUMN_KNOWNLOCATION_LATITUDE, knownLocation.getLatitude() + "");
        contentValues.put(COLUMN_KNOWNLOCATION_LONGITUDE, knownLocation.getLongitude() + "");
        contentValues.put(COLUMN_KNOWNLOCATION_DATETIME, dateTime.toString());
        contentValues.put(COLUMN_KNOWNLOCATION_DISTANCE, distance + "");
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
        String lastPointAltitude = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_POINT_ALTITUDE));

        LogRecording.LogRecord lastLogRecord = null;
        if (StringUtils.isNotEmpty(lastPointDate) && StringUtils.isNotEmpty(lastPointLatitude) && StringUtils.isNotEmpty(lastPointLongitude)) {
            lastLogRecord = new LogRecording.LogRecord();
            if (StringUtils.isNotEmpty(lastPointLatitude)) lastLogRecord.setLatitude(Double.parseDouble(lastPointLatitude));
            if (StringUtils.isNotEmpty(lastPointLongitude)) lastLogRecord.setLongitude(Double.parseDouble(lastPointLongitude));
            if (StringUtils.isNotEmpty(lastPointAltitude)) lastLogRecord.setAltitude(Double.parseDouble(lastPointAltitude));
            if (StringUtils.isNotEmpty(lastPointDate)) lastLogRecord.setDateTime(new LocalDateTime(lastPointDate));
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

    private KnownLocationWithDatetime getKnownLocationFromCursor(Cursor cursor) {
        KnownLocationWithDatetime knownLocationWithDatetime = new KnownLocationWithDatetime();
        knownLocationWithDatetime.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
        knownLocationWithDatetime.setPointDate(new LocalDateTime(cursor.getString(cursor.getColumnIndex(COLUMN_KNOWNLOCATION_DATETIME))));

        if (cursor.getColumnIndex(COLUMN_KNOWNLOCATION_DISTANCE) >= 0) {
            String distanceString = cursor.getString(cursor.getColumnIndex(COLUMN_KNOWNLOCATION_DISTANCE));
            if (StringUtils.isNotEmpty(distanceString)) knownLocationWithDatetime.setDistance(Double.parseDouble(distanceString));
            else knownLocationWithDatetime.setDistance(-1.0);
        }

        KnownLocation knownLocation = new KnownLocation();
        knownLocation.setName(cursor.getString(cursor.getColumnIndex(COLUMN_KNOWNLOCATION_NAME)));
        knownLocation.setLatitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_KNOWNLOCATION_LATITUDE))));
        knownLocation.setLongitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_KNOWNLOCATION_LONGITUDE))));

        knownLocationWithDatetime.setKnownLocation(knownLocation);

        return knownLocationWithDatetime;
    }

    private String getLogRecordingFileName(LogRecording logRecording) {
        return StringUtils.replace(logRecording.getName(), "/", "-");
    }

    public void addSensorValues(SensorReport sensorReport) {
        getWritableDatabase().insert(SENSOR_TABLE_NAME, null, getContentValueSensor(sensorReport));
    }

    public List<SensorReport> getSensorReports() {
        Cursor cursor = getReadableDatabase().query(SENSOR_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_SENSOR_DATETIME, COLUMN_SENSOR_TEMPERATURE, COLUMN_SENSOR_PRESSURE}, null, null, null, null, null);
        cursor.moveToFirst();
        List<SensorReport> result = Lists.newArrayList();
        while (!cursor.isAfterLast()) {
            SensorReport sensorReport = getSensorReportFromCursor(cursor);
            result.add(sensorReport);
            cursor.moveToNext();
        }
        result = Ordering.natural().onResultOf(new Function<SensorReport, LocalDateTime>() {
            @Override
            public LocalDateTime apply(SensorReport sensorReport) {
                return sensorReport.getDateTime();
            }
        }).sortedCopy(result);
        return result;
    }

    public Pair<SensorReport, SensorReport> getLastPressureAndTemperatureMesure() {
        List<SensorReport> reports = Ordering.natural().reverse().onResultOf(new Function<SensorReport, LocalDateTime>() {
            @Override
            public LocalDateTime apply(SensorReport sensorReport) {
                return sensorReport.getDateTime();
            }
        }).sortedCopy(getSensorReports());

        SensorReport temperature = null;
        SensorReport pressure = null;

        for (SensorReport report : reports) {
            if (temperature == null && report.getTemperature() != null) {
                temperature = report;
            }

            if (pressure == null && report.getPressure() != null) {
                pressure = report;
            }

            if (temperature != null && pressure != null) {
                return Pair.with(pressure, temperature);

            }
        }

        if (temperature == null && pressure != null) return Pair.with(pressure, pressure);
        else if (temperature != null && pressure == null) return Pair.with(temperature, temperature);

        return null;
    }

    private SensorReport getSensorReportFromCursor(Cursor cursor) {
        SensorReport sensorReport = new SensorReport();
        sensorReport.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        sensorReport.setDateTime(new LocalDateTime(cursor.getString(cursor.getColumnIndex(COLUMN_SENSOR_DATETIME))));

        String pressureString = cursor.getString(cursor.getColumnIndex(COLUMN_SENSOR_PRESSURE));
        if (StringUtils.isNotEmpty(pressureString) && !"empty".equals(pressureString)) sensorReport.setPressure(Float.parseFloat(pressureString));

        String temperatureString = cursor.getString(cursor.getColumnIndex(COLUMN_SENSOR_TEMPERATURE));
        if (StringUtils.isNotEmpty(temperatureString) && !"empty".equals(temperatureString)) sensorReport.setTemperature(Float.parseFloat(temperatureString));
        return sensorReport;
    }

    private ContentValues getContentValueSensor(SensorReport sensorReport) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_SENSOR_DATETIME, sensorReport.getDateTime().toString());
        if (sensorReport.getPressure() != null) contentValues.put(COLUMN_SENSOR_PRESSURE, sensorReport.getPressure() + "");
        else contentValues.put(COLUMN_SENSOR_PRESSURE, "empty");

        if (sensorReport.getTemperature() != null) contentValues.put(COLUMN_SENSOR_TEMPERATURE, sensorReport.getTemperature() + "");
        else contentValues.put(COLUMN_SENSOR_TEMPERATURE, "empty");
        return contentValues;
    }

    public void deleteErrorReport(ErrorReport errorReport) {
        getWritableDatabase().delete(ERROR_REPORT_TABLE_NAME, COLUMN_ID + "=?", new String[]{"" + errorReport.getId()});
    }

    public Collection<ErrorReport> getErrorReports() {
        Collection<ErrorReport> result = Lists.newArrayList();
        Cursor cursor = getReadableDatabase().query(ERROR_REPORT_TABLE_NAME, new String[] {COLUMN_ID, COLUMN_ERRORREPORT_CLASS, COLUMN_ERRORREPORT_MESSAGE, COLUMN_ERRORREPORT_STACKTRACE}, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            ErrorReport errorReport = getErrorReportFromCursor(cursor);
            result.add(errorReport);
            cursor.moveToNext();
        }
        return result;
    }

    public void addErrorReport(ErrorReport errorReport) {
        getWritableDatabase().insert(ERROR_REPORT_TABLE_NAME, null, getContentValuesFromErrorReport(errorReport));
    }

    private ContentValues getContentValuesFromErrorReport(ErrorReport errorReport) {
        ContentValues result = new ContentValues();
        result.put(COLUMN_ERRORREPORT_CLASS, errorReport.getClassName());
        result.put(COLUMN_ERRORREPORT_MESSAGE, errorReport.getMessage());
        result.put(COLUMN_ERRORREPORT_STACKTRACE, errorReport.getStackTrace());
        return result;
    }

    private ErrorReport getErrorReportFromCursor(Cursor cursor) {
        ErrorReport result = new ErrorReport();
        result.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        result.setClassName(cursor.getString(cursor.getColumnIndex(COLUMN_ERRORREPORT_CLASS)));
        result.setMessage(cursor.getString(cursor.getColumnIndex(COLUMN_ERRORREPORT_MESSAGE)));
        result.setStackTrace(cursor.getString(cursor.getColumnIndex(COLUMN_ERRORREPORT_STACKTRACE)));
        return result;
    }

    public Collection<WifiDetectionHolder> getWifiDetected() {
        Collection<WifiDetectionHolder> result = Lists.newArrayList();
        Cursor cursor = getReadableDatabase().query(WIFI_TABLE_NAME, new String[] {COLUMN_ID, COLUMN_WIFI_STABLE, COLUMN_WIFI_NAME, COLUMN_WIFI_DETECTION_DATETIME}, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            WifiDetectionHolder wifiDetectionHolder = getWifiDetecionHolderFromCursor(cursor);
            result.add(wifiDetectionHolder);
            cursor.moveToNext();
        }
        return result;
    }

    public void cleanupDetectedWifis() {
        LocalDateTime minDate = DateTimeUtils.getCurrentDateTimeInUTC().minusHours(24);
        for (WifiDetectionHolder wifiDetectionHolder : getWifiDetected()) {
            if (wifiDetectionHolder.getWifiDetection().getDetectionDate().isBefore(minDate)) deleteDetectedWifi(wifiDetectionHolder);
        }
    }

    public void deleteDetectedWifi(WifiDetectionHolder wifiDetectionHolder) {
        getWritableDatabase().delete(WIFI_TABLE_NAME, COLUMN_ID + "=?", new String[]{"" + wifiDetectionHolder.getId()});
    }

    public Optional<WifiDetectionHolder> getDetectedWifiBySSID(String ssid) {
        Collection<WifiDetectionHolder> knownWifi = getWifiDetected();
        for (WifiDetectionHolder wifiDetectionHolder : knownWifi) {
            if (ssid.equals(wifiDetectionHolder.getWifiDetection().getSsid())) {
                return Optional.of(wifiDetectionHolder);
            }
        }
        return Optional.absent();
    }

    public Optional<WifiDetectionHolder> getLastDetectedWifi() {
        Collection<WifiDetectionHolder> wifiDetectionHolders = getWifiDetected();
        if (CollectionUtils.isEmpty(wifiDetectionHolders)) return Optional.absent();
        List<WifiDetectionHolder> wifiDetectionHolderList = Ordering.natural().reverse().onResultOf(new Function<WifiDetectionHolder, LocalDateTime>() {
            @Override
            public LocalDateTime apply(WifiDetectionHolder wifiDetectionHolder) {
                return wifiDetectionHolder.getWifiDetection().getDetectionDate();
            }
        }).sortedCopy(wifiDetectionHolders);

        return Optional.of(wifiDetectionHolderList.get(0));
    }

    public void markWifiAsDetected(String ssid, LocalDateTime dateTime) {
        WifiDetectionHolder wifiDetectionHolder = new WifiDetectionHolder();
        wifiDetectionHolder.setStable(false);
        WifiDetection wifiDetection = new WifiDetection();
        wifiDetection.setSsid(ssid);
        wifiDetection.setDetectionDate(dateTime);
        wifiDetectionHolder.setWifiDetection(wifiDetection);
        ContentValues contentValues = getContentValuesFromWifiDetectionHolder(wifiDetectionHolder);
        try {
            getWritableDatabase().insertOrThrow(WIFI_TABLE_NAME, null, contentValues);
        } catch (Throwable t) {
            String stackTrace = Throwables.getStackTraceAsString(t);
            Log.e(getClass().getName(), stackTrace);
        }
    }

    public void updateDetectedWifi(WifiDetectionHolder wifiDetectionHolder) {
        int test = getWritableDatabase().update(WIFI_TABLE_NAME, getContentValuesFromWifiDetectionHolder(wifiDetectionHolder), COLUMN_ID + "=?", new String[]{wifiDetectionHolder.getId() + ""});
        Log.d(getClass().getName(), test + "");
    }

    private ContentValues getContentValuesFromWifiDetectionHolder(WifiDetectionHolder wifiDetectionHolder) {
        ContentValues result = new ContentValues();
        result.put(COLUMN_ID, wifiDetectionHolder.getId());
        result.put(COLUMN_WIFI_STABLE, wifiDetectionHolder.isStable() + "");
        result.put(COLUMN_WIFI_NAME, wifiDetectionHolder.getWifiDetection().getSsid());
        result.put(COLUMN_WIFI_DETECTION_DATETIME, wifiDetectionHolder.getWifiDetection().getDetectionDate().toString());
        return result;
    }

    private WifiDetectionHolder getWifiDetecionHolderFromCursor(Cursor cursor) {
        WifiDetectionHolder wifiDetectionHolder = new WifiDetectionHolder();
        wifiDetectionHolder.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));

        wifiDetectionHolder.setStable("true".equals(cursor.getString(cursor.getColumnIndex(COLUMN_WIFI_STABLE))));

        WifiDetection wifiDetection = new WifiDetection();
        wifiDetection.setDetectionDate(new LocalDateTime(cursor.getString(cursor.getColumnIndex(COLUMN_WIFI_DETECTION_DATETIME))));
        wifiDetection.setSsid(cursor.getString(cursor.getColumnIndex(COLUMN_WIFI_NAME)));

        wifiDetectionHolder.setWifiDetection(wifiDetection);
        return wifiDetectionHolder;
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
