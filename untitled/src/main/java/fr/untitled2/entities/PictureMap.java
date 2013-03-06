package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/8/13
 * Time: 4:12 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class PictureMap {

    @Id
    private String sharingKey;

    @Translate(LocalDateTranslatorFactory.class)
    private LocalDate periodStart;

    @Translate(LocalDateTranslatorFactory.class)
    private LocalDate periodEnd;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime creationDate = LocalDateTime.now();

    @Index
    private Key<User> user;

    private String name;

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public User getUser() {
        return ObjectifyService.ofy().load().key(user).get();
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
    }

    public String getSharingKey() {
        return sharingKey;
    }

    public void setSharingKey(String sharingKey) {
        this.sharingKey = sharingKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
