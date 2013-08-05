package fr.untitled2.desktop;

import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 05/08/13
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
public class ImageUploadTask extends ImageLocationTask {

    public ImageUploadTask(double maxProgress) {
        super(maxProgress * 2);
    }

    @Override
    protected Double call() throws Exception {
        super.call();

        Collection<File> imageFiles = FileUtils.listFiles(getImageDir(), image_file_extensions, true);



        return 1.0;
    }

    private File convertIntoJPEG(File nefFile) {
        return nefFile;
    }



}
