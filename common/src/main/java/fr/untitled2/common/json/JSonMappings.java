package fr.untitled2.common.json;

import com.google.common.collect.Lists;
import fr.untitled2.common.entities.FilmCounter;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserInfos;
import fr.untitled2.common.entities.UserPreferences;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/16/13
 * Time: 2:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class JSonMappings {

    private static final String user_infos_user_id = "userId";

    private static final String log_recordings = "logRecordings";
    private static final String log_recording_name = "name";
    private static final String log_recording_time_zone = "dateTimeZone";
    private static final String log_recording_records = "records";
    private static final String log_recording_record_latitude = "latitude";
    private static final String log_recording_record_longitude = "longitude";
    private static final String log_recording_record_date_time = "dateTime";
    private static final String user_preferences_date_format = "dateFormat";
    private static final String user_preferences_locale = "preferedLocale";
    private static final String user_preferences_camera_time_zone = "cameraDateTimeZone";
    private static final String film_counter_film_id = "filmId";
    private static final String film_counter_pauses = "pauses";
    private static final String film_counter_pause_position = "position";
    private static final String film_counter_pause_date_time = "pauseDateTime";

    public static UserInfos getUserInfos(String json) {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);
        String userId = (String) jsonObject.get(user_infos_user_id);
        UserInfos result = new UserInfos();
        result.setUserId(userId);
        return result;
    }

    public static String getJSON(UserInfos userInfos) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(user_infos_user_id, userInfos.getUserId());
        return jsonObject.toJSONString();
    }


    public static String getJSON(LogRecording logRecording) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(log_recording_name, logRecording.getName());
        jsonObject.put(log_recording_time_zone, logRecording.getDateTimeZone());
        JSONArray jsonArray = new JSONArray();
        for (LogRecording.LogRecord logRecord : logRecording.getRecords()) {
            JSONObject recordObject = new JSONObject();
            recordObject.put(log_recording_record_date_time, logRecord.getDateTime().toString());
            recordObject.put(log_recording_record_latitude, logRecord.getLatitude());
            recordObject.put(log_recording_record_longitude, logRecord.getLongitude());
            jsonArray.add(recordObject);
        }
        jsonObject.put(log_recording_records, jsonArray);
        return jsonObject.toJSONString();
    }

    public static Collection<LogRecording> getLogRecordings(String json) {
        Collection<LogRecording> result = Lists.newArrayList();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);
        JSONArray jsonArray = (JSONArray) jsonObject.get(log_recordings);

        for (Object object : jsonArray) {
            JSONObject logRecordingObject = (JSONObject) object;
            result.add(getLogRecording(logRecordingObject));
        }
        return result;
    }

    public static LogRecording getLogRecording(String json) {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);

        return getLogRecording(jsonObject);
    }

    public static UserPreferences getUserPreferences(String json) {
        UserPreferences result = new UserPreferences();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);
        result.setPreferedLocale((String) jsonObject.get(user_preferences_locale));
        result.setDateFormat((String) jsonObject.get(user_preferences_date_format));
        result.setCameraDateTimeZone((String) jsonObject.get(user_preferences_camera_time_zone));

        return result;
    }

    private static LogRecording getLogRecording(JSONObject jsonObject) {
        LogRecording result = new LogRecording();
        result.setName((String) jsonObject.get(log_recording_name));
        result.setDateTimeZone((String) jsonObject.get(log_recording_time_zone));

        JSONArray jsonArray = (JSONArray) jsonObject.get(log_recording_records);

        for (Object recordObject : jsonArray) {
            JSONObject record = (JSONObject) recordObject;
            LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
            if (record.get(log_recording_record_date_time) instanceof JSONArray) {
                JSONArray dateTimeArray = (JSONArray) record.get(log_recording_record_date_time);
                StringBuilder dateBuilder = new StringBuilder();
                for (int index = 0; index < dateTimeArray.size();index++) {
                    dateBuilder.append(dateTimeArray.get(index));
                    if (index < dateTimeArray.size() - 1 && index < 2) dateBuilder.append("-");
                    else if (index < dateTimeArray.size() - 1 && index == 2) dateBuilder.append("T");
                    else if (index < dateTimeArray.size() - 1 && index > 2 && index < 5) dateBuilder.append(":");
                    else if (index < dateTimeArray.size() - 1 && index > 2 && index == 5) dateBuilder.append(".");
                }
                logRecord.setDateTime(new LocalDateTime(dateBuilder.toString()));
            } else {
                logRecord.setDateTime(new LocalDateTime(record.get(log_recording_record_date_time)));
            }

            logRecord.setLatitude(Double.parseDouble(record.get(log_recording_record_latitude).toString()));
            logRecord.setLongitude(Double.parseDouble(record.get(log_recording_record_longitude).toString()));
            result.getRecords().add(logRecord);
        }
        return result;
    }

    public static String getJSON(UserPreferences userPreferences) {
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isNotEmpty(userPreferences.getDateFormat())) jsonObject.put(user_preferences_date_format, userPreferences.getDateFormat());
        if (StringUtils.isNotEmpty(userPreferences.getCameraDateTimeZone())) jsonObject.put(user_preferences_camera_time_zone, userPreferences.getCameraDateTimeZone());
        if (StringUtils.isNotEmpty(userPreferences.getPreferedLocale())) jsonObject.put(user_preferences_locale, userPreferences.getPreferedLocale());
        return jsonObject.toJSONString();
    }

    public static FilmCounter getFilmCounter(String json) {
        FilmCounter result = new FilmCounter();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);
        result.setFilmId((String) jsonObject.get(film_counter_film_id));

        JSONArray jsonArray = (JSONArray) jsonObject.get(film_counter_pauses);

        for (Object recordObject : jsonArray) {
            JSONObject record = (JSONObject) recordObject;
            FilmCounter.Pause pause = new FilmCounter.Pause();
            if (record.get(film_counter_pause_date_time) instanceof JSONArray) {
                JSONArray dateTimeArray = (JSONArray) record.get(film_counter_pause_date_time);
                StringBuilder dateBuilder = new StringBuilder();
                for (int index = 0; index < dateTimeArray.size();index++) {
                    dateBuilder.append(dateTimeArray.get(index));
                    if (index < dateTimeArray.size() - 1 && index < 2) dateBuilder.append("-");
                    else if (index < dateTimeArray.size() - 1 && index == 2) dateBuilder.append("T");
                    else if (index < dateTimeArray.size() - 1 && index > 2 && index < 5) dateBuilder.append(":");
                    else if (index < dateTimeArray.size() - 1 && index > 2 && index == 5) dateBuilder.append(".");
                }
                pause.setPauseDateTime(new LocalDateTime(dateBuilder.toString()));
            } else {
                pause.setPauseDateTime(new LocalDateTime(record.get(film_counter_pause_date_time)));
            }

            pause.setPosition(Integer.parseInt(record.get(film_counter_pause_position).toString()));
            result.getPauses().add(pause);
        }
        return result;
    }

    public static String getJSON(FilmCounter filmCounter) {
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isNotEmpty(filmCounter.getFilmId())) jsonObject.put(film_counter_film_id, filmCounter.getFilmId());
        JSONArray jsonArray = new JSONArray();
        for (FilmCounter.Pause pause : filmCounter.getPauses()) {
            JSONObject pauseObject = new JSONObject();
            pauseObject.put(film_counter_pause_position, pause.getPosition());
            pauseObject.put(film_counter_pause_date_time, pause.getPauseDateTime().toString());
            jsonArray.add(pauseObject);
        }
        jsonObject.put(film_counter_pauses, jsonArray);
        return jsonObject.toJSONString();
    }

    public static Collection<LogRecording.LogRecord> getLogRecords(String json) {
        Collection<LogRecording.LogRecord> result = Lists.newArrayList();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);
        JSONArray jsonArray = (JSONArray) jsonObject.get(log_recording_records);

        for (Object recordObject : jsonArray) {
            JSONObject record = (JSONObject) recordObject;
            LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
            if (record.get(log_recording_record_date_time) instanceof JSONArray) {
                JSONArray dateTimeArray = (JSONArray) record.get(log_recording_record_date_time);
                StringBuilder dateBuilder = new StringBuilder();
                for (int index = 0; index < dateTimeArray.size();index++) {
                    dateBuilder.append(dateTimeArray.get(index));
                    if (index < dateTimeArray.size() - 1 && index < 2) dateBuilder.append("-");
                    else if (index < dateTimeArray.size() - 1 && index == 2) dateBuilder.append("T");
                    else if (index < dateTimeArray.size() - 1 && index > 2 && index < 5) dateBuilder.append(":");
                    else if (index < dateTimeArray.size() - 1 && index > 2 && index == 5) dateBuilder.append(".");
                }
                logRecord.setDateTime(new LocalDateTime(dateBuilder.toString()));
            } else {
                logRecord.setDateTime(new LocalDateTime(record.get(log_recording_record_date_time)));
            }

            logRecord.setLatitude(Double.parseDouble(record.get(log_recording_record_latitude).toString()));
            logRecord.setLongitude(Double.parseDouble(record.get(log_recording_record_longitude).toString()));
            result.add(logRecord);
        }
        return result;
    }

    public static String getJSON(Collection<LogRecording.LogRecord> logRecords) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (LogRecording.LogRecord logRecord : logRecords) {
            JSONObject recordObject = new JSONObject();
            recordObject.put(log_recording_record_date_time, logRecord.getDateTime().toString());
            recordObject.put(log_recording_record_latitude, logRecord.getLatitude());
            recordObject.put(log_recording_record_longitude, logRecord.getLongitude());
            jsonArray.add(recordObject);
        }
        jsonObject.put(log_recording_records, jsonArray);
        return jsonObject.toJSONString();
    }



}
