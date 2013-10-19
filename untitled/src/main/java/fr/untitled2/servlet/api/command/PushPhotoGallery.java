package fr.untitled2.servlet.api.command;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.FileRefs;
import fr.untitled2.common.entities.raspi.PhotoGallery;
import fr.untitled2.entities.Gallery;
import fr.untitled2.entities.User;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.GzipUtils;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

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
        } else gallery = ObjectifyService.ofy().load().key(Key.create(Gallery.class, input.getId())).get();

        if (CollectionUtils.isNotEmpty(input.getOriginalFiles())) {
            FileRefs fileRefs = new FileRefs();
            fileRefs.getFileRefs().addAll(input.getOriginalFiles());
            gallery.setOriginalFiles(GzipUtils.zipString(JSonUtils.writeJson(fileRefs)));
        }

        if (CollectionUtils.isNotEmpty(input.getMiniFiles())) {
            FileRefs fileRefs = new FileRefs();
            fileRefs.getFileRefs().addAll(input.getMiniFiles());
            gallery.setMiniFiles(GzipUtils.zipString(JSonUtils.writeJson(fileRefs)));
        }

        if (CollectionUtils.isNotEmpty(input.getFullResolutionFiles())) {
            FileRefs fileRefs = new FileRefs();
            fileRefs.getFileRefs().addAll(input.getFullResolutionFiles());
            gallery.setThumbnailFiles(GzipUtils.zipString(JSonUtils.writeJson(fileRefs)));
        }

        gallery.setUser(user);

        ObjectifyService.ofy().save().entity(gallery);
        return input;
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
