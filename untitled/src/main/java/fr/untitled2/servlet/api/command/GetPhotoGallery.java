package fr.untitled2.servlet.api.command;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.FileRefs;
import fr.untitled2.common.entities.raspi.PhotoGallery;
import fr.untitled2.entities.Gallery;
import fr.untitled2.entities.User;
import fr.untitled2.utils.GzipUtils;
import fr.untitled2.utils.JSonUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/16/13
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetPhotoGallery extends Command<PhotoGallery, PhotoGallery, PhotoGallery> {

    @Override
    protected PhotoGallery execute(PhotoGallery input, User user, String fromIpAddress) throws Exception {
        if (StringUtils.isEmpty(input.getId())) throw new Exception("Missing required parameter id");
        Gallery gallery = ObjectifyService.ofy().load().key(Key.create(Gallery.class, input.getId())).get();

        if (!gallery.getUser().equals(user)) throw new Exception("This gallery does not belong to you");

        PhotoGallery photoGallery = new PhotoGallery();
        photoGallery.setId(input.getId());
        if (StringUtils.isNotEmpty(gallery.getMiniFiles())) {
            FileRefs fileRefs = JSonUtils.readJson(FileRefs.class, GzipUtils.unzipString(gallery.getMiniFiles()));
            photoGallery.getMiniFiles().addAll(fileRefs.getFileRefs());
        }

        if (StringUtils.isNotEmpty(gallery.getOriginalFiles())) {
            FileRefs fileRefs = JSonUtils.readJson(FileRefs.class, GzipUtils.unzipString(gallery.getOriginalFiles()));
            photoGallery.getOriginalFiles().addAll(fileRefs.getFileRefs());
        }

        if (StringUtils.isNotEmpty(gallery.getThumbnailFiles())) {
            FileRefs fileRefs = JSonUtils.readJson(FileRefs.class, GzipUtils.unzipString(gallery.getThumbnailFiles()));
            photoGallery.getOriginalFiles().addAll(fileRefs.getFileRefs());
        }
        return photoGallery;
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
