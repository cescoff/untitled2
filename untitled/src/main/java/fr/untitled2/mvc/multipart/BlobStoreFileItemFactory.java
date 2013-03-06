package fr.untitled2.mvc.multipart;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 6:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class BlobStoreFileItemFactory implements FileItemFactory {

    private static Logger logger = LoggerFactory.getLogger(BlobStoreFileItemFactory.class);

    @Override
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        BlobStoreFileItem blobStoreFileItem = new BlobStoreFileItem(contentType, fileName, fieldName, isFormField);
        try {
            blobStoreFileItem.init();
        } catch (IOException e) {
            logger.error("Impossible de creer le blob file '" + fileName + "'", e);
        }
        return blobStoreFileItem;
    }
}
