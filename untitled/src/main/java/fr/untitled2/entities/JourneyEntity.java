package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.utils.JSonUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 23/11/13
 * Time: 15:43
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class JourneyEntity {

    @Id
    private String id;

    private String dateTimeZone;

    @Index
    private Key<User> user;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime startDatetime;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime endDateTime;

    private int pointCount;

    private double distance;

    private String startString;

    private String endString;

    private double maxSpeed;

    @Ignore
    private KnownLocation start;

    @Ignore
    private KnownLocation end;

    private Key<Log> log;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateTimeZone() {
        return dateTimeZone;
    }

    public void setDateTimeZone(String dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }

    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(LocalDateTime startDatetime) {
        this.startDatetime = startDatetime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getStartString() {
        return startString;
    }

    public void setStartString(String startString) {
        this.startString = startString;
    }

    public String getEndString() {
        return endString;
    }

    public void setEndString(String endString) {
        this.endString = endString;
    }

    public Log getLog() {
        return ObjectifyService.ofy().load().key(log).get();
    }

    public void setLog(Log log) {
        this.log = Key.create(Log.class, log.getInternalId());
    }

    public KnownLocation getStart() {
        if (start != null) return start;
        if (StringUtils.isEmpty(startString)) return null;
        try {
            start = JSonUtils.readJson(KnownLocation.class, startString);
        } catch (IOException e) {
            return null;
        }
        return start;
    }

    public void setStart(KnownLocation start) {
        this.start = start;
        try {
            startString = JSonUtils.writeJson(start);
        } catch (IOException e) {
        }
    }

    public KnownLocation getEnd() {
        if (end != null) return end;
        if (StringUtils.isEmpty(endString)) return null;
        try {
            end = JSonUtils.readJson(KnownLocation.class, endString);
        } catch (IOException e) {
            return null;
        }
        return end;
    }

    public void setEnd(KnownLocation end) {
        this.end = end;
        try {
            endString = JSonUtils.writeJson(end);
        } catch (IOException e) {
        }
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public User getUser() {
        return ObjectifyService.ofy().load().key(user).get();
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
    }
}
