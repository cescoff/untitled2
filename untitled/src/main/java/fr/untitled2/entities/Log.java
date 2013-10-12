package fr.untitled2.entities;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import fr.untitled2.common.entities.jaxb.LocalDateTimeAdapter;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/28/13
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD) @Cache
public class Log {

    private static Logger logger = LoggerFactory.getLogger(Log.class);

    @Id
    private String internalId;

    @Index @XmlElement
    private String name;

    @Index
    @XmlTransient
    private Key<User> user;

    @XmlTransient
    private boolean validated = false;

    @Ignore @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime startTime;

    @Ignore @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime endTime;

    @Ignore
    private double distance;

    private int pointCount;

    private String timeZoneId;

    @Ignore @XmlElement(name = "point") @XmlElementWrapper(name = "points")
    private Collection<TrackPoint> trackPoints = Lists.newArrayList();

    @Ignore
    private User realUser;

    public String getInternalId() {
        return internalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Collection<TrackPoint> getTrackPoints() {
        if (CollectionUtils.isNotEmpty(trackPoints)) {
            return trackPoints;
        }
        if (StringUtils.isEmpty(internalId)) internalId = calculateInternalId();
        if (StringUtils.isNotEmpty(internalId)) {
            LogTrackPoints logTrackPoints = ObjectifyService.ofy().load().key(Key.create(LogTrackPoints.class, internalId)).get();
            if (logTrackPoints != null) {
                this.trackPoints = logTrackPoints.getTrackPoints();
                return this.trackPoints;
            }
        }
        this.trackPoints = Lists.newArrayList();
        return this.trackPoints;
    }

    public void setTrackPoints(Collection<TrackPoint> newTrackPoints) {
        LogTrackPoints logTrackPoints = null;

        if (newTrackPoints != null) logger.info("setTrackPoints[trackPoints]:" + newTrackPoints.size());

        if (StringUtils.isNotEmpty(internalId)) logTrackPoints = ObjectifyService.ofy().load().key(Key.create(LogTrackPoints.class, internalId)).get();
        if (logTrackPoints == null) {
            logTrackPoints = new LogTrackPoints();
            if (StringUtils.isNotEmpty(internalId)) logTrackPoints.setLogId(internalId);
            else logTrackPoints.setLogId(calculateInternalId());
        } else if (logTrackPoints.getTrackPoints() != null) {
            logger.info("setTrackPoints[logTrackPoints.getTrackPoints().size()]:" + logTrackPoints.getTrackPoints().size());
        }
        logTrackPoints.setTrackPoints(newTrackPoints);
        logger.info("setTrackPoints[logTrackPoints.getTrackPoints().size()2]:" + logTrackPoints.getTrackPoints().size());
        ObjectifyService.ofy().save().entity(logTrackPoints).now();
        this.trackPoints = newTrackPoints;
    }

    public User getUser() {
        return ObjectifyService.ofy().load().key(user).get();
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
        this.realUser = user;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public boolean isValidated() {
        return validated;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public int getPointCount() {
        return pointCount;
    }

    public Log clone() {
        Log result = new Log();
        result.startTime = this.startTime;
        result.endTime = this.endTime;
        result.name = this.name;
        result.timeZoneId = this.timeZoneId;
        result.internalId = this.internalId;
        result.user = this.user;
        result.validated = this.validated;
        result.trackPoints = this.trackPoints;
        result.distance = this.distance;
        result.pointCount = this.pointCount;
        return result;
    }

    @OnSave
    public void prepersist() {
        if (StringUtils.isEmpty(internalId)) this.internalId = calculateInternalId();
        setTrackPoints(this.trackPoints);
    }

    @OnLoad
    public void postload() {

        try {
            LogStatistics logStatistics = ObjectifyService.ofy().load().key(Key.create(LogStatistics.class, internalId)).get();
            if (logStatistics != null) {
                this.distance = logStatistics.getDistance();
                this.startTime = logStatistics.getStart();
                this.endTime = logStatistics.getEnd();
                this.pointCount = logStatistics.getPointCount();
            } else {
                startTime = LocalDateTime.now().withYear(1980);
                endTime = LocalDateTime.now().withYear(1980);
            }

        } catch (Throwable t) {
            throw new IllegalStateException("Enable to read json trackpoints", t);
        }
        this.realUser = getUser();
    }

    private String calculateInternalId() {
        if (realUser != null) return SignUtils.calculateSha1Digest(realUser.getUserId() + name + startTime);
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Log log = (Log) o;

        if (pointCount != log.pointCount) return false;
        if (validated != log.validated) return false;
        if (internalId != null ? !internalId.equals(log.internalId) : log.internalId != null) return false;
        if (name != null ? !name.equals(log.name) : log.name != null) return false;
        if (timeZoneId != null ? !timeZoneId.equals(log.timeZoneId) : log.timeZoneId != null) return false;
        if (user != null ? !user.equals(log.user) : log.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = internalId != null ? internalId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (validated ? 1 : 0);
        result = 31 * result + pointCount;
        result = 31 * result + (timeZoneId != null ? timeZoneId.hashCode() : 0);
        return result;
    }
}
