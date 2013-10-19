package fr.untitled2.raspi.batchlet;

import fr.untitled2.common.entities.raspi.FileRef;
import fr.untitled2.common.entities.raspi.ImageTransformationTask;
import fr.untitled2.common.entities.raspi.PhotoGallery;
import fr.untitled2.raspi.ImageConverter;
import fr.untitled2.raspi.api.SlaveBatchlet;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/14/13
 * Time: 12:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImageTransformBatchlet extends SlaveBatchlet<ImageTransformationTask, ImageTransformationTask> {

    @Override
    public ImageTransformationTask execute(ImageTransformationTask from) throws Exception {
        getBatchContext().logInfo("Getting remote file '" + from.getOriginalFile().getName() + "'");
        File imageFile = getBatchContext().getRemoteFile(from.getOriginalFile());
        getBatchContext().logInfo("Remote file loaded '" + imageFile.getName() + "'");

        File outpuDir = getBatchContext().createTempDir();

        getBatchContext().logInfo("OutputDir->'" + outpuDir + "'");
        ImageConverter imageConverter = new ImageConverter(imageFile, outpuDir);
        long timer = System.currentTimeMillis();
        getBatchContext().logInfo("Starting full HD conversion");
        File thumbNail = imageConverter.convertIntoJpg(1920);
        getBatchContext().logInfo("Full HD conversion DONE : " + (System.currentTimeMillis() - timer) + "ms");

        timer = System.currentTimeMillis();
        getBatchContext().logInfo("Starting mini conversion");
        imageConverter = new ImageConverter(thumbNail, outpuDir);
        File mini = imageConverter.convertIntoJpg(320);
        getBatchContext().logInfo("Mini conversion DONE : " + (System.currentTimeMillis() - timer) + "ms");

        getBatchContext().logInfo("Pushing thumbnail to remote server");
        FileRef thumbnailRef = getBatchContext().pushRemoteFile(thumbNail);
        getBatchContext().logInfo("Pushing mini to remote server");
        FileRef miniRef = getBatchContext().pushRemoteFile(mini);

        getBatchContext().logInfo("Updating remote gallery");
        PhotoGallery photoGallery = new PhotoGallery();
        photoGallery.setId(from.getGalleryId());
        photoGallery.getMiniFiles().add(miniRef);
        photoGallery.getFullResolutionFiles().add(thumbnailRef);

        getBatchContext().executeRemoteCommand("addPhotoGallery", photoGallery, PhotoGallery.class);

        return from;
    }

    @Override
    public Class getInputType() {
        return ImageTransformationTask.class;
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public boolean isThreadSafe() {
        return true;
    }


}
