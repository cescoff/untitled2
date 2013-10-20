package fr.untitled2.raspi.batchlet;

import com.beust.jcommander.internal.Lists;
import fr.untitled2.common.entities.raspi.*;
import fr.untitled2.raspi.ImageConverter;
import fr.untitled2.raspi.api.SlaveBatchlet;
import org.javatuples.Pair;

import java.io.File;
import java.util.Collection;

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
        long timer = System.currentTimeMillis();
        getBatchContext().logInfo("Getting remote file '" + from.getOriginalFile().getName() + "'");
        File imageFile = getBatchContext().getRemoteFile(from.getOriginalFile());
        getBatchContext().logInfo("Remote file loaded '" + imageFile.getName() + "' in " + (System.currentTimeMillis() - timer) + "ms");

        File outpuDir = getBatchContext().createTempDir();

        getBatchContext().logInfo("OutputDir->'" + outpuDir + "'");
        ImageConverter imageConverter = new ImageConverter(imageFile, outpuDir);
        timer = System.currentTimeMillis();
        getBatchContext().logInfo("Starting full HD conversion");

        Collection<Pair<Integer, File>> resizedImages = imageConverter.convertIntoJpg(Lists.newArrayList(1920, 320));

        getBatchContext().logInfo("Conversion DONE : " + (System.currentTimeMillis() - timer) + "ms");

        getBatchContext().logInfo("Pushing thumbnail to remote server");

        FileRef thumbnailRef = null;
        FileRef miniRef = null;
        for (Pair<Integer, File> resizedImage : resizedImages) {
            if (resizedImage.getValue0() == 1920) {
                thumbnailRef = getBatchContext().pushRemoteFile(resizedImage.getValue1());
            } else if (resizedImage.getValue0() == 320) {
                miniRef = getBatchContext().pushRemoteFile(resizedImage.getValue1());
            }
        }
        getBatchContext().logInfo("Pushing mini to remote server");

        if (thumbnailRef == null) getBatchContext().logError("No optimized file generated");
        if (miniRef == null) getBatchContext().logError("No thumbnail file generated");

        getBatchContext().logInfo("Updating remote gallery");
        ProcessedImage processedImage = new ProcessedImage();
        processedImage.setOriginalFile(from.getOriginalFile());
        processedImage.setOptimizedFile(thumbnailRef);
        processedImage.setThumbnailFile(miniRef);

        getBatchContext().executeRemoteCommand("addPhotoGallery", processedImage, SimpleResponse.class);

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
