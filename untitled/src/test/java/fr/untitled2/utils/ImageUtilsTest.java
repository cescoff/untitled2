package fr.untitled2.utils;

import fr.untitled2.entities.Image;
import fr.untitled2.entities.User;
import org.apache.commons.io.IOUtils;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffElement;
import org.apache.sanselan.formats.tiff.TiffImageData;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageUtilsTest {
    @Test
    public void testBuildReducedImages() throws Exception {
        File imageFile = new File("/Users/corentinescoffier/Desktop/TestImages/D22_3099.NEF");
        Image image = new Image();
        ImageUtils.buildReducedImages(image, IOUtils.toByteArray(new FileInputStream(imageFile)));
        System.out.println("Width = " + image.getWidth());
        System.out.println("Height = " + image.getHeight());
        File thumbImageFile = new File("/Users/corentinescoffier/Desktop/TestImages/D22_3099_thumb.jpg");
        //IOUtils.write(image.getHighResolutionPreview(), new FileOutputStream(thumbImageFile));
    }

    @Test
    public void getMetaData() throws Exception {
//        File imageFile = new File("/Users/corentinescoffier/Desktop/TestImages/D22_3099.NEF");
        File imageFile = new File("/Users/corentinescoffier/Desktop/TestImages/D22_3099_converted.jpg");
        Image image = new Image();
        ImageUtils.handleMetaData(image, IOUtils.toByteArray(new FileInputStream(imageFile)));
        System.out.println(image.getDateTaken());
    }

    @Test
    public void getMetaDataFromFile() throws Exception {
        File sourceFile = new File("/Users/corentinescoffier/Desktop/GeoLocImages/Trajet Psy/D22_3495.jpg");
//        File sourceFile = new File("/Users/corentinescoffier/Desktop/test-date-anglais.NEF");
        FileInputStream fileInputStream = new FileInputStream(sourceFile);
        System.out.println(Sanselan.getMetadata(IOUtils.toByteArray(fileInputStream)));
    }

    @Test
    public void testCrop() {
        int width = 490;
        int height = 328;
        int targetSize = Math.min(width, height);
        double leftX = 1.0 - ((width - targetSize) * Math.pow(width, -1.0) * 0.5);
        double rightX = (width - targetSize) * Math.pow(width, -1.0) * 0.5;
        System.out.println("leftX:" + leftX);
        System.out.println("rightX:" + rightX);
    }

    @Test
    public void handleDateMetaData() throws Exception {
        IImageMetadata metdata = Sanselan.getMetadata(IOUtils.toByteArray(new FileInputStream(new File(("/Users/corentinescoffier/Desktop/IMAGE_TEST_DATES2.JPG")))));
        Collection<IImageMetadata.IImageMetadataItem> items = metdata.getItems();
        for (IImageMetadata.IImageMetadataItem item : items) {
            String itemString = item.toString();
            System.out.println(itemString);
        }
        Image image = new Image();
        ImageUtils.handleMetaData(image, IOUtils.toByteArray(new FileInputStream(new File(("/Users/corentinescoffier/Desktop/IMAGE_TEST_DATES2.JPG")))));
        System.out.println(image.getDateTaken());
    }

}
