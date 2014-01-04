package fr.untitled2.common.json;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import fr.untitled2.common.entities.*;
import fr.untitled2.common.utils.introscpection.ClassDescriptor;
import fr.untitled2.common.utils.introscpection.ClassIntrospector;
import fr.untitled2.common.utils.introscpection.FieldDescriptor;
import fr.untitled2.utils.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private static final String log_recording_record_altitude = "altitude";
    private static final String log_recording_record_date_time = "dateTime";
    private static final String user_preferences_date_format = "dateFormat";
    private static final String user_preferences_locale = "preferedLocale";
    private static final String user_preferences_camera_time_zone = "cameraDateTimeZone";
    private static final String film_counter_film_id = "filmId";
    private static final String film_counter_pauses = "pauses";
    private static final String film_counter_pause_position = "position";
    private static final String film_counter_pause_date_time = "pauseDateTime";
    private static final String knownlocations = "knownLocations";
    private static final String knownlocation_name = "name";
    private static final String knownlocation_latitude = "latitude";
    private static final String knownlocation_longitude = "longitude";
    private static final String knownlocation_altitude = "altitude";
    private static final String knownlocation_wifissid = "wifiSSIDs";
    private static final String remoteerror_classname = "className";
    private static final String remoteerror_message = "message";
    private static final String remoteerror_stacktrace = "stackTrace";

    private static Map<Class<?>, ClassDescriptor> descriptors = Maps.newHashMap();

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
            recordObject.put(log_recording_record_altitude, logRecord.getAltitude());
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

        JSONArray jsonArray = (JSONArray) jsonObject.get(knownlocations);

        for (Object knownLocationObject : jsonArray) {
            JSONObject knownlocationJsonObject = (JSONObject) knownLocationObject;

            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName((String) knownlocationJsonObject.get(knownlocation_name));
            if (knownlocationJsonObject.get(knownlocation_latitude) != null) knownLocation.setLatitude(Double.parseDouble(knownlocationJsonObject.get(knownlocation_latitude).toString()));
            if (knownlocationJsonObject.get(knownlocation_longitude) != null) knownLocation.setLongitude(Double.parseDouble(knownlocationJsonObject.get(knownlocation_longitude).toString()));
            if (knownlocationJsonObject.get(knownlocation_altitude) != null) knownLocation.setAltitude(Double.parseDouble(knownlocationJsonObject.get(knownlocation_altitude).toString()));
            if (knownlocationJsonObject.containsKey(knownlocation_wifissid)) {
                JSONArray wifiSSIDArray = (JSONArray) knownlocationJsonObject.get(knownlocation_wifissid);
                List<String> ssids = Lists.newArrayList();

                for (int index = 0; index < wifiSSIDArray.size(); index++) {
                    ssids.add((String) wifiSSIDArray.get(index));
                }
                knownLocation.getWifiSSIDs().addAll(ssids);
            }
            result.getKnownLocations().add(knownLocation);
        }

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
            logRecord.setAltitude(Double.parseDouble(record.get(log_recording_record_altitude).toString()));
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
            logRecord.setAltitude(Double.parseDouble(record.get(log_recording_record_altitude).toString()));
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
            recordObject.put(log_recording_record_altitude, logRecord.getAltitude());
            jsonArray.add(recordObject);
        }
        jsonObject.put(log_recording_records, jsonArray);
        return jsonObject.toJSONString();
    }

    public static List<KnownLocation> getKnownLocations(String json) {
        if (StringUtils.isEmpty(json)) return Collections.EMPTY_LIST;
        List<KnownLocation> result = Lists.newArrayList();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);
        JSONArray jsonArray = (JSONArray) jsonObject.get(knownlocations);

        for (Object object : jsonArray) {
            JSONObject knownLocationObject = (JSONObject) object;
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName((String) knownLocationObject.get(knownlocation_name));
            if (knownLocationObject.get(knownlocation_latitude) != null) knownLocation.setLatitude(Double.parseDouble(knownLocationObject.get(knownlocation_latitude).toString()));
            if (knownLocationObject.get(knownlocation_longitude) != null) knownLocation.setLongitude(Double.parseDouble(knownLocationObject.get(knownlocation_longitude).toString()));
            if (knownLocationObject.get(knownlocation_altitude) != null) knownLocation.setAltitude(Double.parseDouble(knownLocationObject.get(knownlocation_altitude).toString()));
            if (knownLocationObject.containsKey(knownlocation_wifissid)) {
                JSONArray wifiSSIDArray = (JSONArray) knownLocationObject.get(knownlocation_wifissid);
                List<String> ssids = Lists.newArrayList();

                for (int index = 0; index < wifiSSIDArray.size(); index++) {
                    ssids.add((String) wifiSSIDArray.get(index));
                }
                knownLocation.getWifiSSIDs().addAll(ssids);
            }

            result.add(knownLocation);
        }
        return result;
    }

    public static String getJSONKnownLocation(Collection<KnownLocation> knownLocations) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (KnownLocation knownLocation : knownLocations) {
            JSONObject knownLocationObject = new JSONObject();
            knownLocationObject.put(knownlocation_name, knownLocation.getName());
            knownLocationObject.put(knownlocation_latitude, knownLocation.getLatitude());
            knownLocationObject.put(knownlocation_longitude, knownLocation.getLongitude());
            knownLocationObject.put(knownlocation_altitude, knownLocation.getAltitude());

            if (CollectionUtils.isNotEmpty(knownLocation.getWifiSSIDs())) {
                JSONArray wifiSSIDArray = new JSONArray();
                for (String ssid : knownLocation.getWifiSSIDs()) {
                    wifiSSIDArray.add(ssid);
                }
                knownLocationObject.put(knownlocation_wifissid, wifiSSIDArray);
            }

            jsonArray.add(knownLocationObject);
        }
        jsonObject.put(knownlocations, jsonArray);
        return jsonObject.toJSONString();
    }

    public static String getJSONRemoteError(RemoteError remoteError) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(remoteerror_classname, remoteError.getClassName());
        jsonObject.put(remoteerror_message, remoteError.getMessage());
        jsonObject.put(remoteerror_stacktrace, remoteError.getStackTrace());
        return jsonObject.toJSONString();
    }

    public static <T> String toJson(T object) throws Exception {
        return toJsonObject(object).toJSONString();
    }

    public static <T> T readJson(Class<T> aClass, String json) throws Exception {
        return readJsonObject(aClass, (JSONObject) JSONValue.parse(json));
    }

    private static <T> T readJsonObject(Class<T> objectClass, JSONObject jsonObject) throws Exception {
        ClassDescriptor classDescriptor = getClassDescriptor(objectClass);

        Object result = classDescriptor.getNoArgConstructor().newInstance(null);

        for (FieldDescriptor fieldDescriptor : classDescriptor.getFields().values()) {
            if (fieldDescriptor.getSetter() != null) {
                if (Collection.class.equals(fieldDescriptor.getType())) {
                    JSONArray jsonArray = (JSONArray) jsonObject.get(getFieldJsonArrayName(fieldDescriptor));
                    if (jsonArray != null) {
                        Collection collection = (Collection) fieldDescriptor.getGetter().invoke(result, null);
                        for (Object arrayValue : jsonArray) {
                            if (arrayValue instanceof String && fieldDescriptor.getSubType().get().getType().equals(String.class)) {
                                collection.add(arrayValue);
                            } else {
                                collection.add(readJsonObject(fieldDescriptor.getSubType().get().getType(), (JSONObject) arrayValue));
                            }
                        }
                    }
                } else if (!fieldDescriptor.isPrimitive() && !isJavaLangNumberObject(fieldDescriptor) && !isStringType(fieldDescriptor) && !fieldDescriptor.getContructorFromString().isPresent()) {
                    JSONObject son = (JSONObject) jsonObject.get(getFieldJsonArrayName(fieldDescriptor));
                    fieldDescriptor.getSetter().invoke(result, readJsonObject(fieldDescriptor.getType(), son));
                } else {
                    Object value = jsonObject.get(getFieldJsonName(fieldDescriptor));
                    if (value != null) {
                        if (fieldDescriptor.isPrimitive() || isStringType(fieldDescriptor) || isJavaLangNumberObject(fieldDescriptor)) {
                            fieldDescriptor.getSetter().invoke(result, value);
                        } else if (fieldDescriptor.getContructorFromString().isPresent()) {
                            Object valueFromString = fieldDescriptor.getContructorFromString().get().newInstance(value.toString());
                            fieldDescriptor.getSetter().invoke(result, valueFromString);
                        }
                    }
                }
            }
        }
        return (T) result;
    }

    private static <T> JSONObject toJsonObject(T object) throws Exception {
        ClassDescriptor classDescriptor = getClassDescriptor(object);
        JSONObject jsonObject = new JSONObject();

        for (FieldDescriptor fieldDescriptor : classDescriptor.getFields().values()) {
            if (fieldDescriptor.getGetter() != null) {
                if (!fieldDescriptor.getType().equals(Collection.class)) {
                    if (isJavaLangNumberObject(fieldDescriptor) || isStringType(fieldDescriptor) || fieldDescriptor.isPrimitive()) {
                        Object value = fieldDescriptor.getGetter().invoke(object, null);
                        if (value != null) {
                            jsonObject.put(getFieldJsonName(fieldDescriptor), fieldDescriptor.getGetter().invoke(object, null));
                        }
                    } else if (fieldDescriptor.getContructorFromString().isPresent()) {
                        Object value = fieldDescriptor.getGetter().invoke(object, null);
                        if (value != null) {
                            jsonObject.put(getFieldJsonName(fieldDescriptor), value.toString());
                        }
                    }
                } else {
                    JSONArray jsonArray = new JSONArray();
                    Collection values = (Collection) fieldDescriptor.getGetter().invoke(object, null);
                    for (Object value : values) {
                        jsonArray.add(toJson(value));
                    }
                    jsonObject.put(getFieldJsonArrayName(fieldDescriptor), jsonArray);
                }
            }
        }
        return jsonObject;
    }

    private static <T> ClassDescriptor getClassDescriptor(T object) throws Exception {
        return getClassDescriptor(object.getClass());
    }

    private static <T> ClassDescriptor getClassDescriptor(Class<T> objectClass) throws Exception {
        if (!descriptors.containsKey(objectClass)) descriptors.put(objectClass, ClassIntrospector.getClassDescriptor(objectClass));
        return descriptors.get(objectClass);
    }

    private static String getFieldJsonName(FieldDescriptor fieldDescriptor) {
        for (Annotation annotation : fieldDescriptor.getAnnotations()) {
            if (annotation.annotationType().equals(XmlAttribute.class)) {
                String name = ((XmlAttribute) annotation).name();
                if (StringUtils.isNotEmpty(name) && !"##default".equals(name)) return ((XmlAttribute) annotation).name();
            } else if (annotation.annotationType().equals(XmlElement.class)) {
                String name = ((XmlElement) annotation).name();
                if (StringUtils.isNotEmpty(name) && !"##default".equals(name)) return ((XmlElement) annotation).name();
            }
        }
        return fieldDescriptor.getName();
    }

    private static String getFieldJsonArrayName(FieldDescriptor fieldDescriptor) {
        for (Annotation annotation : fieldDescriptor.getAnnotations()) {
            if (annotation.annotationType().equals(XmlElementWrapper.class)) {
                String name = ((XmlElementWrapper) annotation).name();
                if (StringUtils.isNotEmpty(name) && !"##default".equals(name)) return name;
                else return fieldDescriptor.getName();
            } else if (annotation.annotationType().equals(XmlElement.class)) {
                String name = ((XmlElement) annotation).name();
                if (StringUtils.isNotEmpty(name) && !"##default".equals(name)) return name;
                else return fieldDescriptor.getName();
            }

        }
        return fieldDescriptor.getName();
    }

    private static boolean isJAXBAnnotatedField(FieldDescriptor fieldDescriptor) {
        for (Annotation annotation : fieldDescriptor.getAnnotations()) {
            if (isJAXBFieldAnnotation(annotation)) return true;
        }
        return false;
    }

    private static boolean isJAXBFieldAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(XmlElement.class) || annotation.annotationType().equals(XmlAttribute.class) || annotation.annotationType().equals(XmlElementWrapper.class);
    }

    private static boolean isStringType(FieldDescriptor fieldDescriptor) {
        return fieldDescriptor.getType().equals(String.class);
    }

    private static boolean isJavaLangNumberObject(FieldDescriptor fieldDescriptor) {
        return fieldDescriptor.getType().equals(Integer.class) ||
                fieldDescriptor.getType().equals(Long.class) ||
                fieldDescriptor.getType().equals(Double.class) ||
                fieldDescriptor.getType().equals(Float.class);
    }

}
