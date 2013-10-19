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
import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/16/13
 * Time: 10:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddPhotoGallery extends Command<PhotoGallery, PhotoGallery, PhotoGallery> {

    @Override
    protected PhotoGallery execute(PhotoGallery input, User user, String fromIpAddress) throws Exception {
        Gallery gallery = ObjectifyService.ofy().load().key(Key.create(Gallery.class, input.getId())).get();
        if (!gallery.getUser().equals(user)) throw new Exception("This gallery does not belong to you");
        int originalCount = 0;
        int thumbnailCount = 0;
        if (CollectionUtils.isNotEmpty(input.getFullResolutionFiles())) {
            FileRefs fileRefs = new FileRefs();
            if (StringUtils.isNotEmpty(gallery.getThumbnailFiles())) {
                fileRefs = JSonUtils.readJson(FileRefs.class, GzipUtils.unzipString(gallery.getThumbnailFiles()));
            }
            fileRefs.getFileRefs().addAll(input.getFullResolutionFiles());
            gallery.setThumbnailFiles(GzipUtils.zipString(JSonUtils.writeJson(fileRefs)));
            thumbnailCount = fileRefs.getFileRefs().size();
        }

        if (CollectionUtils.isNotEmpty(input.getMiniFiles())) {
            FileRefs fileRefs = new FileRefs();
            if (StringUtils.isNotEmpty(gallery.getMiniFiles())) {
                fileRefs = JSonUtils.readJson(FileRefs.class, GzipUtils.unzipString(gallery.getMiniFiles()));
            }
            fileRefs.getFileRefs().addAll(input.getMiniFiles());
            gallery.setMiniFiles(GzipUtils.zipString(JSonUtils.writeJson(fileRefs)));
        }

        if (CollectionUtils.isNotEmpty(input.getOriginalFiles())) {
            FileRefs fileRefs = new FileRefs();
            if (StringUtils.isNotEmpty(gallery.getOriginalFiles())) {
                fileRefs = JSonUtils.readJson(FileRefs.class, GzipUtils.unzipString(gallery.getOriginalFiles()));
            }
            fileRefs.getFileRefs().addAll(input.getOriginalFiles());
            gallery.setOriginalFiles(GzipUtils.zipString(JSonUtils.writeJson(fileRefs)));
            originalCount = fileRefs.getFileRefs().size();
        }

        if (originalCount == thumbnailCount) {
            gallery.setDone(true);
        }

        ObjectifyService.ofy().save().entity(gallery).now();

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
