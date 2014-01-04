package fr.untitled2.servlet.api.command;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.FileRef;
import fr.untitled2.common.entities.raspi.PhotoGallery;
import fr.untitled2.entities.File;
import fr.untitled2.entities.Gallery;
import fr.untitled2.entities.OriginalToThumbnails;
import fr.untitled2.entities.User;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/16/13
 * Time: 10:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class PushPhotoGallery extends Command<PhotoGallery, PhotoGallery, PhotoGallery> {

    @Override
    protected PhotoGallery execute(PhotoGallery input, User user, String fromIpAddress) throws Exception {
        Gallery gallery = null;
        if (StringUtils.isEmpty(input.getId())) {
            input.setId(SignUtils.calculateSha1Digest(LocalDateTime.now() + user.getEmail()));
            gallery = new Gallery();
            gallery.setId(input.getId());
            gallery.setName(DateTimeFormat.forPattern(user.getDateFormat() + " HH:mm:ss").print(LocalDateTime.now()));
            gallery.setCreationDate(LocalDateTime.now());
            gallery.setUser(user);
            ObjectifyService.ofy().save().entity(gallery).now();
        } else {
            gallery = ObjectifyService.ofy().load().key(Key.create(Gallery.class, input.getId())).get();
        }

        if (CollectionUtils.isNotEmpty(input.getOriginalFiles())) {
            for (FileRef fileRef : input.getOriginalFiles()) {
                OriginalToThumbnails originalToThumbnails = getImageFile(fileRef, user, gallery);
                ObjectifyService.ofy().save().entity(originalToThumbnails);
            }
        }

        if (gallery.isValid() && !gallery.isDone()) {
            gallery.setDone(true);
            ObjectifyService.ofy().save().entity(gallery);
        }

        return input;
    }

    private OriginalToThumbnails getImageFile(FileRef fileRef, User user, Gallery gallery) throws IOException {
        String id = SignUtils.calculateSha1Digest(fileRef.getId() + user.getUserId());
        OriginalToThumbnails originalToThumbnails = ObjectifyService.ofy().load().key(Key.create(OriginalToThumbnails.class, id)).get();

        if (originalToThumbnails == null) {
            originalToThumbnails = new OriginalToThumbnails();
            originalToThumbnails.setId(id);
            originalToThumbnails.setGallery(gallery);
            originalToThumbnails.setOriginalFile(ObjectifyService.ofy().load().key(Key.create(File.class, fileRef.getId())).get());
            originalToThumbnails.setUser(user);
        }
        return originalToThumbnails;
    }

    @Override
    protected Class<PhotoGallery> getInputObjectType() {
        return PhotoGallery.class;
    }

    @Override
    protected Class<PhotoGallery> getOutputObjectType() {
        return PhotoGallery.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
