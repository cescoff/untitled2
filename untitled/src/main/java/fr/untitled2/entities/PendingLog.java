package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Translate;
import fr.untitled2.utils.SignUtils;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/5/13
 * Time: 6:33 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class PendingLog {

    @Id
    private String identificationKey;

    @Index @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime creationDate = LocalDateTime.now();

    @Index
    private Key<Log> trip;

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public String getIdentificationKey() {
        return identificationKey;
    }

    public Key<Log> getTripKey() {
        return this.trip;
    }

    public Log getTrip() {
        return ObjectifyService.ofy().load().key(trip).get();
    }

    public void setTrip(Log aLog, Key<Log> key) {
        this.trip = key;
        this.identificationKey = SignUtils.calculateSha1Digest(creationDate + aLog.getName() + aLog.getUser().getUserId() + aLog.getStartTime() + aLog.getEndTime());
    }



}
