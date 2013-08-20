package fr.untitled2.entities;

import com.beust.jcommander.internal.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.utils.JSonUtils;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 19/08/13
 * Time: 12:27
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class LogPersistenceJob {

    private String key;

    private Key<User> userKey;

    private String logRecordingJson;

    private boolean gzip;

    @Ignore
    private LogRecording logRecording;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Key<User> getUserKey() {
        return userKey;
    }

    public void setUserKey(Key<User> userKey) {
        this.userKey = userKey;
    }

    public String getLogRecordingJson() {
        return logRecordingJson;
    }

    public void setLogRecordingJson(String logRecordingJson) {
        this.logRecordingJson = logRecordingJson;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public LogRecording getLogRecording() {
        return logRecording;
    }

    public void setLogRecording(LogRecording logRecording) {
        this.logRecording = logRecording;
    }

    @OnSave
    public void prepersist() {
        if (logRecording != null) {
            String json = null;
            if (logRecording.getRecords().size() > 8500) {
                gzip = true;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream);
                try {
                    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(base64OutputStream);
                    JSonUtils.writeJson(logRecording, gzipOutputStream);
                    gzipOutputStream.close();
                    base64OutputStream.close();

                    json = new String(byteArrayOutputStream.toByteArray());
                } catch (Throwable t) {
                    throw new IllegalStateException("Enable to write json", t);
                }
            } else {
                try {
                    json = JSonUtils.writeJson(logRecording);
                } catch (Throwable t) {
                    throw new IllegalStateException("Enable to write json", t);
                }
            }
            this.logRecordingJson = json;
        }
    }

    @OnLoad
    public void postload() {
        if (StringUtils.isNotEmpty(logRecordingJson)) {
            if (gzip) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(logRecordingJson.getBytes());
                Base64InputStream base64InputStream = new Base64InputStream(byteArrayInputStream);
                try {
                    GZIPInputStream gzipInputStream = new GZIPInputStream(base64InputStream);
                    this.logRecording = JSonUtils.readJson(LogRecording.class, gzipInputStream);
                } catch (Throwable t) {
                    throw new IllegalStateException("Enable to read json", t);
                }
            } else {
                try {
                    this.logRecording = JSonUtils.readJson(LogRecording.class, logRecordingJson);
                } catch (Throwable t) {
                    throw new IllegalStateException("Enable to read json", t);
                }
            }
        }
    }

}
