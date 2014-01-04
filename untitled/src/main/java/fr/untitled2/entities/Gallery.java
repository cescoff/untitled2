package fr.untitled2.entities;

import com.beust.jcommander.internal.Lists;
import com.google.appengine.labs.repackaged.com.google.common.base.Function;
import com.google.appengine.labs.repackaged.com.google.common.collect.Iterables;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import fr.untitled2.common.entities.raspi.FileRef;
import fr.untitled2.utils.CollectionUtils;
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

    @Ignore
    private Iterable<OriginalToThumbnails> images = Lists.newArrayList();

    @Index
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

    public Iterable<OriginalToThumbnails> getImages() {
        initFiles();
        return images;
    }

    public Iterable<FileRef> getOriginalFiles() {
        initFiles();
        return Iterables.transform(images, new Function<OriginalToThumbnails, FileRef>() {
            @Override
            public FileRef apply(OriginalToThumbnails originalToThumbnails) {
                File originalFile = originalToThumbnails.getOriginalFile();
                FileRef fileRef = new FileRef();
                fileRef.setFilePartCount(originalFile.getFilePartCount());
                fileRef.setName(originalFile.getGsFilePath());
                fileRef.setId(originalFile.getId());
                return fileRef;
            }
        });
    }

    public Iterable<FileRef> getOptimzedFiles() {
        initFiles();
        return Iterables.transform(images, new Function<OriginalToThumbnails, FileRef>() {
            @Override
            public FileRef apply(OriginalToThumbnails originalToThumbnails) {
                File optimizedFile = originalToThumbnails.getOptimizedFile();
                FileRef fileRef = new FileRef();
                fileRef.setFilePartCount(optimizedFile.getFilePartCount());
                fileRef.setName(optimizedFile.getGsFilePath());
                fileRef.setId(optimizedFile.getId());
                return fileRef;
            }
        });
    }

    public Iterable<FileRef> getThumbnailFiles() {
        initFiles();
        return Iterables.transform(images, new Function<OriginalToThumbnails, FileRef>() {
            @Override
            public FileRef apply(OriginalToThumbnails originalToThumbnails) {
                File thumbnailFile = originalToThumbnails.getThumbnailFile();
                FileRef fileRef = new FileRef();
                fileRef.setFilePartCount(thumbnailFile.getFilePartCount());
                fileRef.setName(thumbnailFile.getGsFilePath());
                fileRef.setId(thumbnailFile.getId());
                return fileRef;
            }
        });
    }

    private void initFiles() {
        if (CollectionUtils.isEmpty(images)) {
            images = ObjectifyService.ofy().load().type(OriginalToThumbnails.class).filter("gallery", this);
        }
    }

    public boolean isValid() {
        if (done) return true;
        initFiles();
        for (OriginalToThumbnails image : images) {
            if (image == null) return false;
            if (image.getOptimizedFile() == null || image.getThumbnailFile() == null) {
                return false;
            }
        }
        return true;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gallery gallery = (Gallery) o;

        if (done != gallery.done) return false;
        if (creationDate != null ? !creationDate.equals(gallery.creationDate) : gallery.creationDate != null)
            return false;
        if (id != null ? !id.equals(gallery.id) : gallery.id != null) return false;
        if (images != null ? !images.equals(gallery.images) : gallery.images != null) return false;
        if (name != null ? !name.equals(gallery.name) : gallery.name != null) return false;
        if (user != null ? !user.equals(gallery.user) : gallery.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (images != null ? images.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (done ? 1 : 0);
        return result;
    }
}
