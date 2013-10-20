package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/19/13
 * Time: 6:55 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class ImageFiles {

    @Id
    private String id;

    private Key<File> originalFile;

    private Key<File> optimizedFile;

    private Key<File> thumbnailFile;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime creationDate = LocalDateTime.now();

    @Index
    private Key<Gallery> gallery;

    @Index
    private Key<User> user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public File getOriginalFile() {
        return ObjectifyService.ofy().load().key(originalFile).get();
    }

    public void setOriginalFile(File originalFile) {
        this.originalFile = Key.create(File.class, originalFile.getId());
    }

    public File getOptimizedFile() {
        return ObjectifyService.ofy().load().key(optimizedFile).get();
    }

    public void setOptimizedFile(File optimizedFile) {
        this.optimizedFile = Key.create(File.class, optimizedFile.getId());
    }

    public File getThumbnailFile() {
        return ObjectifyService.ofy().load().key(thumbnailFile).get();
    }

    public void setThumbnailFile(File thumbnailFile) {
        this.thumbnailFile = Key.create(File.class, thumbnailFile.getId());
    }

    public Gallery getGallery() {
        return ObjectifyService.ofy().load().key(gallery).get();
    }

    public void setGallery(Gallery gallery) {
        this.gallery = Key.create(Gallery.class, gallery.getId());
    }

    public User getUser() {
        return ObjectifyService.ofy().load().key(user).get();
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public enum ImageFileType {
        original,
        optimized,
        thumbnail
    }

}
