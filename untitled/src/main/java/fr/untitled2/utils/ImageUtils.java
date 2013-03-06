package fr.untitled2.utils;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.common.collect.Sets;
import fr.untitled2.entities.Image;
import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 8:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageUtils {

    private static Logger logger = LoggerFactory.getLogger(ImageUtils.class);

    private static DateTimeFormatter  dateTimeFormat = DateTimeFormat.forPattern("yyyy:MM:dd HH:mm:ss");

    private static Collection<String> datePrefixes = Sets.newHashSet("Create Date: '"); /* , "Date Time Original: '" */

    private static int targetHeightHighResolution = 1080;

    private static int targetHeightMediumResolution = 490;

    private static int targetHeightLowResolution = 200;

    public static void buildReducedImages(Image image, byte[] sourceImageData) throws Exception{
        logger.info("Taille de l'image : " + sourceImageData.length + " (avant reduction)");
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        com.google.appengine.api.images.Image serviceImage = ImagesServiceFactory.makeImage(sourceImageData);

        image.setWidth(serviceImage.getWidth());
        image.setHeight(serviceImage.getHeight());

        int width = new Double((image.getWidth() * targetHeightHighResolution) / image.getHeight()).intValue();
        int height = targetHeightHighResolution;

        if (image.getWidth() < width) width = image.getWidth();
        if (image.getHeight() < height) height = image.getHeight();
// High resolution
        Transform resizeHighResolution = ImagesServiceFactory.makeResize(width, height);
        com.google.appengine.api.images.Image highResolution = imagesService.applyTransform(resizeHighResolution, serviceImage);

        image.setHighResolutionPreview(highResolution.getImageData());

// Medium resolution
        if (image.getWidth() > image.getHeight()) {
            width = targetHeightMediumResolution;
            height = new Double((image.getHeight() * targetHeightMediumResolution) / image.getWidth()).intValue();
        } else {
            width = new Double((image.getWidth() * targetHeightMediumResolution) / image.getHeight()).intValue();
            height = targetHeightMediumResolution;
        }

        Transform resizeMediumResolution = ImagesServiceFactory.makeResize(width, height);
        com.google.appengine.api.images.Image mediumResolution = imagesService.applyTransform(resizeMediumResolution, serviceImage);

        image.setMediumResolutionPreview(mediumResolution.getImageData());

// Low resolution
        width = new Double((image.getWidth() * targetHeightLowResolution) / image.getHeight()).intValue();
        height = targetHeightLowResolution;

        Transform resizeLowResolution = ImagesServiceFactory.makeResize(width, height);
        com.google.appengine.api.images.Image lowResolution = imagesService.applyTransform(resizeLowResolution, serviceImage);

        image.setLowResolutionPreview(lowResolution.getImageData());

// Square low resolution
        generateSquareImage(image, serviceImage, imagesService);
    }

    public static void handleMetaData(Image image, byte[] imageData) throws Exception {
        logger.info("Taille de l'image : " + imageData.length);
        IImageMetadata metdata = Sanselan.getMetadata(imageData);
        if (metdata != null) {
            Collection<IImageMetadata.IImageMetadataItem> items = metdata.getItems();
            for (IImageMetadata.IImageMetadataItem item : items) {


                String itemString = item.toString();
                for (String datePrefix : datePrefixes) {
                    if (StringUtils.startsWith(itemString, datePrefix)) {
                        LocalDateTime localDateTime = dateTimeFormat.parseLocalDateTime(StringUtils.remove(StringUtils.remove(itemString, datePrefix), "'"));
                        logger.info("(" + image + ")");
                        logger.info("(" + image.getUser() + ")");
                        logger.info("(" + image.getUser().getTimeZoneId() + ")");
                        logger.info("(" + localDateTime + ")");
                        logger.info("(" + localDateTime.toDateTime(DateTimeZone.forID(image.getUser().getTimeZoneId())).toDateTime(DateTimeZone.UTC).toLocalDateTime() + ")");

                        image.setDateTaken(localDateTime.toDateTime(DateTimeZone.forID(image.getUser().getTimeZoneId())).toDateTime(DateTimeZone.UTC).toLocalDateTime());
                    }
                }
            }
        } else logger.error("Les meta data sont nulles pour l'image '" + image.getImageKey() + "'");
    }

    private static void generateSquareImage(Image image, com.google.appengine.api.images.Image serviceImage, ImagesService imagesService) throws Exception {
        int width = serviceImage.getWidth();
        int height = serviceImage.getHeight();
        int targetSize = Math.min(width, height);
        if (width > height) {
            double leftX = (width - targetSize) * Math.pow(width, -1.0) * 0.5;
            double rightX = 1.0 - ((width - targetSize) * Math.pow(width, -1.0) * 0.5);
            logger.info("width:" + width + ", targetSize:" + targetSize + ", leftX:" + leftX + ", rightX" + rightX);
            Transform crop = ImagesServiceFactory.makeCrop(leftX, 0.0, rightX, 1.0);
            com.google.appengine.api.images.Image croppedImage = imagesService.applyTransform(crop, serviceImage);
            Transform resize = ImagesServiceFactory.makeResize(targetHeightLowResolution, targetHeightLowResolution);
            croppedImage = imagesService.applyTransform(resize, croppedImage);
            image.setSquareLowResolutionPreview(croppedImage.getImageData());

        } else {
            double topY = ((height - targetSize) * Math.pow(height, -1.0) * 0.5);
            double bottomY = 1.0 - ((height - targetSize) * Math.pow(height, -1.0) * 0.5);
            logger.info("width:" + height + ", targetSize:" + targetSize + ", topY:" + topY + ", bottomY:" + bottomY);
            Transform crop = ImagesServiceFactory.makeCrop(0.0, topY, 1.0, bottomY);
            com.google.appengine.api.images.Image croppedImage = imagesService.applyTransform(crop, serviceImage);
            Transform resize = ImagesServiceFactory.makeResize(targetHeightLowResolution, targetHeightLowResolution);
            croppedImage = imagesService.applyTransform(resize, croppedImage);
            image.setSquareLowResolutionPreview(croppedImage.getImageData());
        }
    }

}
