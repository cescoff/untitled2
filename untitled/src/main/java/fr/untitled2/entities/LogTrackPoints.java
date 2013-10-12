package fr.untitled2.entities;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 20/08/13
 * Time: 11:02
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class LogTrackPoints {

    private static Logger logger = LoggerFactory.getLogger(LogTrackPoints.class);

    @Id
    private String logId;

    private boolean gzip;

    private String json;

    @Ignore
    private Collection<TrackPoint> trackPoints = Lists.newArrayList();

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public void setTrackPoints(Collection<TrackPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }

    public Collection<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    @OnSave
    public void prepersist() {
        if (trackPoints != null) logger.info("prepersist:[trackPoints.size()]" + trackPoints.size());
        TrackPointsHolder trackPointsHolder = new TrackPointsHolder();
        trackPointsHolder.setTrackPoints(this.trackPoints);

        if (this.trackPoints.size() > 8500) {
            gzip = true;
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream);
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(base64OutputStream);
                JSonUtils.writeJson(trackPointsHolder, gzipOutputStream);
                gzipOutputStream.close();
                base64OutputStream.close();
                byteArrayOutputStream.close();
                this.json = new String(byteArrayOutputStream.toByteArray());
            } catch (Throwable t) {
                throw new IllegalStateException("Enable to save json", t);
            }
        } else {
            try {
                this.json = JSonUtils.writeJson(trackPointsHolder);
            } catch (Throwable t) {
                throw new IllegalStateException("Enable to save json", t);
            }
        }
    }

    @OnLoad
    public void postload() {
        try {
            if (StringUtils.isNotEmpty(json)) {
                if (gzip) {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(json.getBytes());
                    Base64InputStream base64InputStream = new Base64InputStream(byteArrayInputStream);
                    GZIPInputStream gzipInputStream = new GZIPInputStream(base64InputStream);
                    TrackPointsHolder trackPointsHolder = JSonUtils.readJson(TrackPointsHolder.class, gzipInputStream);
                    this.trackPoints = trackPointsHolder.getTrackPoints();
                } else {
                    TrackPointsHolder trackPointsHolder = JSonUtils.readJson(TrackPointsHolder.class, json);
                    this.trackPoints = trackPointsHolder.getTrackPoints();
                }
            }
        } catch (Throwable t) {
            throw new IllegalStateException("Enable to read json trackpoints", t);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogTrackPoints that = (LogTrackPoints) o;

        if (gzip != that.gzip) return false;
        if (json != null ? !json.equals(that.json) : that.json != null) return false;
        if (logId != null ? !logId.equals(that.logId) : that.logId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = logId != null ? logId.hashCode() : 0;
        result = 31 * result + (gzip ? 1 : 0);
        result = 31 * result + (json != null ? json.hashCode() : 0);
        return result;
    }
}
