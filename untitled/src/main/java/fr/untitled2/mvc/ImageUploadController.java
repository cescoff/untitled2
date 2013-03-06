package fr.untitled2.mvc;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.ImageStorageType;
import fr.untitled2.entities.User;
import fr.untitled2.mvc.multipart.BlobStoreFileItem;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.JSonUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 6:13 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class ImageUploadController extends AuthenticatedController {

    private static Logger logger = LoggerFactory.getLogger(ImageUploadController.class);

    @ModelAttribute
    public void ajaxAttribute(WebRequest request, Model model) {
        model.addAttribute("ajaxRequest", AjaxUtils.isAjaxRequest(request));
    }

    @RequestMapping(value = "/images/add", method=RequestMethod.GET)
    public String fileUploadForm() {
        return MVCConstants.image_add_view;
    }

    @RequestMapping(value = "/images/add", method= RequestMethod.POST)
    public void processUpload(@RequestParam MultipartFile files, HttpServletResponse response) throws IOException {
        try {
            User user = getUser();
            logger.info("Upload pour l'utilisateur '" + user.getUserId() + "'");
            logger.info("Image uploadee pour l'utilisateur '" + user.getUserId() + "'s");

            Image image = new Image();
            String fileKey = ((BlobStoreFileItem) ((CommonsMultipartFile) files).getFileItem()).getBlobKey().getKeyString();
            logger.info("FileKey:" + fileKey);
            image.setImageKey(fileKey);
            image.setReady(false);
            image.setUser(user);
            image.setName(files.getOriginalFilename());
            image.setImageStorageType(ImageStorageType.mypicturelog);
            ObjectifyService.ofy().save().entity(image).now();

            Queue queue = QueueFactory.getQueue(ServletConstants.image_queue_name);

            TaskOptions taskOptions = TaskOptions.Builder.withUrl("/imageProcess").param(ServletConstants.image_key_processor_parameter, image.getImageKey() + "").param(ServletConstants.user_id_parameter, user.getUserId());
            queue.add(taskOptions);
            UploadedFiles uploadedFiles = new UploadedFiles();
            UploadedFiles.UploadedFile uploadedFile = new UploadedFiles.UploadedFile();
            uploadedFile.setUrl("/ihm/images/files/view?imageKey=" + image.getImageKey());
            uploadedFile.setThumbnailUrl("/ihm/images/files/thumbnail?imageKey=" + image.getImageKey());
            uploadedFile.setSize(files.getSize());
            uploadedFile.setDeleteUrl("/ihm/images/files/delete?imageKey=" + image.getImageKey());
            uploadedFile.setName(image.getImageKey());

            uploadedFiles.getFiles().add(uploadedFile);

            response.getOutputStream().write(JSonUtils.writeJson(uploadedFiles).getBytes());
        } catch (Throwable t) {
            logger.error("Impossible d'uploader une image", t);
            MailService.Message message = new MailService.Message();
            message.setSender("corentin.escoffier@gmail.com");
            message.setTo("corentin.escoffier@gmail.com");
            message.setSubject("Upload error : " + t.getMessage());
            message.setTextBody(Throwables.getStackTraceAsString(t));
        }
    }

    @RequestMapping(value = "/images/files/view", method = RequestMethod.GET)
    public void viewImage(@RequestParam("imageKey") String imageKey, HttpServletResponse response) throws IOException {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        blobstoreService.serve(new BlobKey(imageKey), response);
    }

    @RequestMapping(value = "/images/files/thumbnail", method = RequestMethod.GET)
    public void viewImageThumbnail(@RequestParam("imageKey") String imageKey, HttpServletResponse response) throws IOException {
        AppEngineFile appEngineFile = FileServiceFactory.getFileService().getBlobFile(new BlobKey(imageKey));
        FileReadChannel fileReadChannel = FileServiceFactory.getFileService().openReadChannel(appEngineFile, false);
        InputStream inputStream = Channels.newInputStream(fileReadChannel);
        byte[] imageData = IOUtils.toByteArray(inputStream);
        fileReadChannel.close();

        ImagesService imagesService = ImagesServiceFactory.getImagesService();

        com.google.appengine.api.images.Image serviceImage = ImagesServiceFactory.makeImage(imageData);

        int newWidth = 100;
        int newHeight = new Double((newWidth * serviceImage.getHeight()) / serviceImage.getWidth()).intValue();

        Transform resizeHighResolution = ImagesServiceFactory.makeResize(newWidth, newHeight);
        com.google.appengine.api.images.Image thumbNail = imagesService.applyTransform(resizeHighResolution, serviceImage);
        response.getOutputStream().write(thumbNail.getImageData());
    }

    @RequestMapping(value = "/images/files/delete", method = RequestMethod.DELETE)
    public void deleteImage(@RequestParam("imageKey") String imageKey, HttpServletResponse response) throws IOException {
        Image image = ObjectifyService.ofy().load().key(Key.create(Image.class, imageKey)).get();
        ObjectifyService.ofy().delete().entity(image).now();
        AppEngineFile appEngineFile = FileServiceFactory.getFileService().getBlobFile(new BlobKey(imageKey));
        FileServiceFactory.getFileService().delete(appEngineFile);
        response.getOutputStream().write("OK".getBytes());
    }



    @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
    public static class UploadedFiles {

        @XmlElement
        private List<UploadedFile> files = Lists.newArrayList();

        public List<UploadedFile> getFiles() {
            return files;
        }

        public void setFiles(List<UploadedFile> files) {
            this.files = files;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class UploadedFile {

            @XmlElement
            private String name;

            @XmlElement
            private Long size;

            @XmlElement
            private String url;

            @XmlElement(name = "thumbnail_url")
            private String thumbnailUrl;

            @XmlElement(name = "delete_url")
            private String deleteUrl;

            @XmlElement(name = "delete_type")
            private String deleteType = "DELETE";

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Long getSize() {
                return size;
            }

            public void setSize(Long size) {
                this.size = size;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getThumbnailUrl() {
                return thumbnailUrl;
            }

            public void setThumbnailUrl(String thumbnailUrl) {
                this.thumbnailUrl = thumbnailUrl;
            }

            public String getDeleteUrl() {
                return deleteUrl;
            }

            public void setDeleteUrl(String deleteUrl) {
                this.deleteUrl = deleteUrl;
            }

            public String getDeleteType() {
                return deleteType;
            }

            public void setDeleteType(String deleteType) {
                this.deleteType = deleteType;
            }
        }
    }

}
