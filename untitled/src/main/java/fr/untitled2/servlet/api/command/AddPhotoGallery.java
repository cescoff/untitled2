package fr.untitled2.servlet.api.command;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.*;
import fr.untitled2.entities.*;
import fr.untitled2.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/16/13
 * Time: 10:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddPhotoGallery extends Command<ProcessedImage, SimpleResponse, SimpleResponse> {

    private static final Logger logger = LoggerFactory.getLogger(AddPhotoGallery.class);

    @Override
    protected SimpleResponse execute(ProcessedImage input, User user, String fromIpAddress) throws Exception {
        logger.info("User->" + user);


        Iterable<OriginalToThumbnails> imageFiles = getFiles(user);

        if (CollectionUtils.isEmpty(imageFiles)) logger.error("No image files");

        for (OriginalToThumbnails imageFile : imageFiles) {
            if (imageFile != null) {
                File originalFile = imageFile.getOriginalFile();
                if (originalFile.getId().equals(input.getOriginalFile().getId())) {
                    logger.info("Found originalFile '" + originalFile.getId() + "'");
                    File optimizedFile = ObjectifyService.ofy().load().key(Key.create(File.class, input.getOptimizedFile().getId())).get();
                    if (optimizedFile == null) logger.info("File '" + input.getOptimizedFile().getId() + "' is not found");
                    imageFile.setOptimizedFile(optimizedFile);

                    File thumbnailFile = ObjectifyService.ofy().load().key(Key.create(File.class, input.getThumbnailFile().getId())).get();
                    imageFile.setThumbnailFile(thumbnailFile);
                    ObjectifyService.ofy().save().entity(imageFile);
                    return new SimpleResponse(true);
                }
            } else logger.error("Image file is null '" + imageFile + "'");
        }
        logger.error("No file found with id '" + input.getOriginalFile().getId() + "'");
        return new SimpleResponse(false);
    }

    private Iterable<OriginalToThumbnails> getFiles(final User user) {
        return Iterables.filter(ObjectifyService.ofy().load().type(OriginalToThumbnails.class), new Predicate<OriginalToThumbnails>() {
            @Override
            public boolean apply(OriginalToThumbnails originalToThumbnails) {
                return originalToThumbnails.getUser().equals(user);
            }
        });
    }

    @Override
    protected Class<ProcessedImage> getInputObjectType() {
        return ProcessedImage.class;
    }

    @Override
    protected Class<SimpleResponse> getOutputObjectType() {
        return SimpleResponse.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
