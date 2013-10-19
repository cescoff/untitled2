package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import fr.untitled2.common.entities.raspi.LogLevel;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 1:57 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class Batchlet {

    @Id
    private String id;

    private String className;

    private String frequenceTimeUnit;

    private int frequency;

    private LogLevel logLevel;

    @Index
    private Key<User> user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFrequenceTimeUnit() {
        return frequenceTimeUnit;
    }

    public void setFrequenceTimeUnit(String frequenceTimeUnit) {
        this.frequenceTimeUnit = frequenceTimeUnit;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public User getUser() {
        return ObjectifyService.ofy().load().key(user).get();
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Batchlet batchlet = (Batchlet) o;

        if (frequency != batchlet.frequency) return false;
        if (className != null ? !className.equals(batchlet.className) : batchlet.className != null) return false;
        if (frequenceTimeUnit != null ? !frequenceTimeUnit.equals(batchlet.frequenceTimeUnit) : batchlet.frequenceTimeUnit != null)
            return false;
        if (id != null ? !id.equals(batchlet.id) : batchlet.id != null) return false;
        if (logLevel != batchlet.logLevel) return false;
        if (user != null ? !user.equals(batchlet.user) : batchlet.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (frequenceTimeUnit != null ? frequenceTimeUnit.hashCode() : 0);
        result = 31 * result + frequency;
        result = 31 * result + (logLevel != null ? logLevel.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
