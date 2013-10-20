package fr.untitled2.servlet.api.command;

import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.*;
import fr.untitled2.entities.File;
import fr.untitled2.entities.Gallery;
import fr.untitled2.entities.ImageFiles;
import fr.untitled2.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/20/13
 * Time: 10:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetGalleries extends Command<SimpleStringMessage, Galleries, Galleries> {

    private static final Logger logger = LoggerFactory.getLogger(GetGalleries.class);

    @Override
    protected Galleries status(User user) throws Exception {
        return getPhotoGallery(user);
    }

    @Override
    protected Galleries execute(SimpleStringMessage input, User user, String fromIpAddress) throws Exception {
        return getPhotoGallery(user);
    }

    private Galleries getPhotoGallery(User user) {
        Galleries result = new Galleries();
        Iterable<Gallery> galleries = ObjectifyService.ofy().load().type(Gallery.class).filter("user", user);
        for (Gallery gallery : galleries) {
            if (gallery != null) {
                FullPhotoGallery fullPhotoGallery = new FullPhotoGallery();
                fullPhotoGallery.setGalleryId(gallery.getId());
                fullPhotoGallery.setGalleryName(gallery.getName());
                Iterable<ImageFiles> images = ObjectifyService.ofy().load().type(ImageFiles.class).filter("gallery", gallery);
                for (ImageFiles image : images) {
                    if (image != null) {
                        PhotoGalleryItem photoGalleryItem = new PhotoGalleryItem();

                        photoGalleryItem.setOriginalFile(toFileRef(image.getOriginalFile()));
                        photoGalleryItem.setOptimizedFile(toFileRef(image.getOptimizedFile()));
                        photoGalleryItem.setThumnailFile(toFileRef(image.getThumbnailFile()));

                        fullPhotoGallery.getItems().add(photoGalleryItem);
                    } else logger.error("Image is null for gallery '" + gallery.getId() + "'");
                }
                result.getGalleries().add(fullPhotoGallery);
            } else logger.error("Gallery is null");
        }
        return result;
    }

    private FileRef toFileRef(File file) {
        if (file == null) return null;
        FileRef result = new FileRef();
        result.setId(file.getId());
        result.setName(file.getGsFilePath());
        result.setFilePartCount(file.getFilePartCount());
        return result;
    }

    @Override
    protected Class<SimpleStringMessage> getInputObjectType() {
        return SimpleStringMessage.class;
    }

    @Override
    protected Class<Galleries> getOutputObjectType() {
        return Galleries.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
