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
    private Iterable<ImageFiles> images = Lists.newArrayList();

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

    public Iterable<ImageFiles> getImages() {
        initFiles();
        return images;
    }

    public Iterable<FileRef> getOriginalFiles() {
        initFiles();
        return Iterables.transform(images, new Function<ImageFiles, FileRef>() {
            @Override
            public FileRef apply(ImageFiles imageFiles) {
                File originalFile = imageFiles.getOriginalFile();
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
        return Iterables.transform(images, new Function<ImageFiles, FileRef>() {
            @Override
            public FileRef apply(ImageFiles imageFiles) {
                File optimizedFile = imageFiles.getOptimizedFile();
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
        return Iterables.transform(images, new Function<ImageFiles, FileRef>() {
            @Override
            public FileRef apply(ImageFiles imageFiles) {
                File thumbnailFile = imageFiles.getThumbnailFile();
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
            images = ObjectifyService.ofy().load().type(ImageFiles.class).filter("gallery", this);
        }
    }

    public boolean isValid() {
        if (done) return true;
        initFiles();
        for (ImageFiles image : images) {
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
}
