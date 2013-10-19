package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Translate;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/16/13
 * Time: 10:14 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class Gallery {

    @Id
    private String id;

    private String name;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime creationDate;

    private String originalFiles;

    private String thumbnailFiles;

    private String miniFiles;

    private Key<User> user;

    private boolean done;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getOriginalFiles() {
        return originalFiles;
    }

    public void setOriginalFiles(String originalFiles) {
        this.originalFiles = originalFiles;
    }

    public String getThumbnailFiles() {
        return thumbnailFiles;
    }

    public void setThumbnailFiles(String thumbnailFiles) {
        this.thumbnailFiles = thumbnailFiles;
    }

    public String getMiniFiles() {
        return miniFiles;
    }

    public void setMiniFiles(String miniFiles) {
        this.miniFiles = miniFiles;
    }

    public User getUser() {
        return ObjectifyService.ofy().load().key(user).get();
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
