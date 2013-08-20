package fr.untitled2.desktop;

import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.utils.JAXBUtils;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 05/08/13
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
public class ImageUploadTask extends ImageLocationTask {

    private double maxProgress;

    public ImageUploadTask(double maxProgress) {
        super(maxProgress * 2);
        maxProgress = maxProgress;
    }

    @Override
    protected Double call() throws Exception {
        super.call();

        Configuration configuration = getConfiguration();

        Collection<File> imageFiles = FileUtils.listFiles(getImageDir(), image_file_extensions, true);

        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(configuration.getoAuthAccessKey(), configuration.getoAuthAccessSecret());

        double remainingPercentage = maxProgress / imageFiles.size();
        double progress = maxProgress;
        for (File imageFile : imageFiles) {
            if (isNefFile(imageFile)) imageFile = convertIntoJPEG(imageFile);
            try {
                appEngineOAuthClient.uploadImage(imageFile);
                progress += remainingPercentage;
                updateProgress(progress / (2.0 * maxProgress), 2.0 * maxProgress);
                updateMessage("Uploaded : " + imageFile.getName());
            } catch (Throwable t) {
                updateMessage("An error has occured on file " + imageFile.getName());
                t.printStackTrace();
            }
        }

        return 1.0;
    }

    private File convertIntoJPEG(File nefFile) throws IOException {
        File newFile = getJpgFile(nefFile);
        BufferedImage image = ImageIO.read(nefFile);
        ImageIO.write(image, "jpg", newFile);
        return newFile;
    }

    private boolean isNefFile(File aFile) {
        return aFile != null && StringUtils.endsWith(aFile.getName().toLowerCase(), "nef");
    }

    private File getJpgFile(File nefFile) {
        String fileName = nefFile.getName();
        fileName = StringUtils.replace(nefFile.getName(), ".nef", ".jpg");
        fileName = StringUtils.replace(nefFile.getName(), ".Nef", ".jpg");
        fileName = StringUtils.replace(nefFile.getName(), ".NEF", ".jpg");
        return new File(nefFile.getParentFile(), fileName);
    }

}
