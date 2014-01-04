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
public class OriginalToThumbnails {

    @Id
    private String id;

    private Key<File> originalFile;

    private Key<File> optimizedFile;

    private Key<File> thumbnailFile;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime creationDate = LocalDateTime.now();

    private Key<Gallery> gallery;

    private Key<User> user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public File getOriginalFile() {
        if (originalFile != null) return ObjectifyService.ofy().load().key(originalFile).get();
        else return null;
    }

    public void setOriginalFile(File originalFile) {
        this.originalFile = Key.create(File.class, originalFile.getId());
    }

    public File getOptimizedFile() {
        if (optimizedFile != null) return ObjectifyService.ofy().load().key(optimizedFile).get();
        return null;
    }

    public void setOptimizedFile(File optimizedFile) {
        this.optimizedFile = Key.create(File.class, optimizedFile.getId());
    }

    public File getThumbnailFile() {
        if (thumbnailFile != null) return ObjectifyService.ofy().load().key(thumbnailFile).get();
        else return null;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OriginalToThumbnails that = (OriginalToThumbnails) o;

        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (gallery != null ? !gallery.equals(that.gallery) : that.gallery != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (optimizedFile != null ? !optimizedFile.equals(that.optimizedFile) : that.optimizedFile != null)
            return false;
        if (originalFile != null ? !originalFile.equals(that.originalFile) : that.originalFile != null) return false;
        if (thumbnailFile != null ? !thumbnailFile.equals(that.thumbnailFile) : that.thumbnailFile != null)
            return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (originalFile != null ? originalFile.hashCode() : 0);
        result = 31 * result + (optimizedFile != null ? optimizedFile.hashCode() : 0);
        result = 31 * result + (thumbnailFile != null ? thumbnailFile.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (gallery != null ? gallery.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
