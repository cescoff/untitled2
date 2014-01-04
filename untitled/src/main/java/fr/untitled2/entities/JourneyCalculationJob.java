package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Translate;
import fr.untitled2.entities.LocalDateTimeTranslatorFactory;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 23/11/13
 * Time: 15:48
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class JourneyCalculationJob {

    @Id
    private String id;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime startDateTime;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime endDateTime;

    private int computedJourneyCount;

    private Key<User> user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getComputedJourneyCount() {
        return computedJourneyCount;
    }

    public void setComputedJourneyCount(int computedJourneyCount) {
        this.computedJourneyCount = computedJourneyCount;
    }

    public User getUser() {
        return ObjectifyService.ofy().load().key(user).get();
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
    }
}
