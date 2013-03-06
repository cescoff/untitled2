package fr.untitled2.mvc;

import fr.untitled2.utils.JSonUtils;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/19/13
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageUploadControllerTest {
    @Test
    public void testJson() throws Exception {
        ImageUploadController.UploadedFiles uploadedFiles = new ImageUploadController.UploadedFiles();

        ImageUploadController.UploadedFiles.UploadedFile file1 = new ImageUploadController.UploadedFiles.UploadedFile();
        file1.setDeleteUrl("deleteUrl1");
        file1.setName("name1");
        file1.setSize(1L);
        file1.setThumbnailUrl("thumbNailUrl1");
        file1.setUrl("url1");

        ImageUploadController.UploadedFiles.UploadedFile file2 = new ImageUploadController.UploadedFiles.UploadedFile();
        file2.setDeleteUrl("deleteUrl2");
        file2.setName("name2");
        file2.setSize(2L);
        file2.setThumbnailUrl("thumbNailUrl2");
        file2.setUrl("url2");

        uploadedFiles.getFiles().add(file1);
        uploadedFiles.getFiles().add(file2);

        System.out.println(JSonUtils.writeJson(uploadedFiles));

    }
}
