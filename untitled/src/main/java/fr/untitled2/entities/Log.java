package fr.untitled2.entities;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import fr.untitled2.common.entities.jaxb.DateTimeAdapter;
import fr.untitled2.common.entities.jaxb.LocalDateTimeAdapter;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.SignUtils;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/28/13
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class Log {

    @Id
    private String internalId;

    @Index @XmlElement
    private String name;

    @Index
    @XmlTransient
    private Key<User> user;

    @XmlTransient
    private boolean validated = false;

    @Translate(LocalDateTimeTranslatorFactory.class) @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime startTime;

    @Translate(LocalDateTimeTranslatorFactory.class) @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime endTime;

    private String timeZoneId;

    @Ignore @XmlElement(name = "point") @XmlElementWrapper(name = "points")
    private Collection<TrackPoint> trackPoints = Lists.newArrayList();

    @Ignore
    private User realUser;

    @XmlTransient
    private String jsonPoints;

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
        return trackPoints;
    }

    public void setTrackPoints(Collection<TrackPoint> trackPoints) {
        this.trackPoints = trackPoints;
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

    public Log clone() {
        Log result = new Log();
        result.startTime = this.startTime;
        result.endTime = this.endTime;
        result.name = this.name;
        result.timeZoneId = this.timeZoneId;
        result.internalId = this.internalId;
        result.user = this.user;
        result.validated = this.validated;
        result.trackPoints.addAll(this.trackPoints);
        return result;
    }

    @OnSave
    public void prepersist() {
        this.startTime = null;
        this.endTime = null;
        for (TrackPoint trackPoint : trackPoints) {
            if (this.startTime == null) this.startTime = trackPoint.getPointDate();
            else if (this.startTime.isAfter(trackPoint.getPointDate())) this.startTime = trackPoint.getPointDate();

            if (this.endTime == null) this.endTime = trackPoint.getPointDate();
            else if (this.endTime.isBefore(trackPoint.getPointDate())) this.endTime = trackPoint.getPointDate();
        }
        TrackPointsHolder trackPointsHolder = new TrackPointsHolder();
        trackPointsHolder.setTrackPoints(this.trackPoints);
        try {
            this.jsonPoints = JSonUtils.writeJson(trackPointsHolder);
        } catch (Throwable t) {
            throw new IllegalStateException("Enable to generate json for trackpoints", t);
        }
        this.internalId = SignUtils.calculateSha1Digest(realUser.getUserId() + name + startTime + endTime);
    }

    @OnLoad
    public void postload() {
        try {
            TrackPointsHolder trackPointsHolder = JSonUtils.readJson(TrackPointsHolder.class, jsonPoints);
            this.trackPoints = trackPointsHolder.trackPoints;
        } catch (Throwable t) {
            throw new IllegalStateException("Enable to read json trackpoints", t);
        }
        this.realUser = getUser();
    }

    @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
    public static class TrackPointsHolder {

        @XmlElement(name = "point") @XmlElementWrapper(name = "points")
        private Collection<TrackPoint> trackPoints = Lists.newArrayList();

        public Collection<TrackPoint> getTrackPoints() {
            return trackPoints;
        }

        public void setTrackPoints(Collection<TrackPoint> trackPoints) {
            this.trackPoints = trackPoints;
        }
    }

}
