package fr.untitled2.servlet.api;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Throwables;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.User;
import fr.untitled2.mvc.multipart.BlobStoreFileItem;
import fr.untitled2.mvc.multipart.BlobStoreFileItemFactory;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/12/13
 * Time: 8:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImageUploadServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(ImageUploadServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            BlobStoreFileItemFactory blobStoreFileItemFactory = new BlobStoreFileItemFactory();
            ServletFileUpload fileUpload = new ServletFileUpload(blobStoreFileItemFactory);
            List<FileItem> fileItems = fileUpload.parseRequest(req);

            String userEmail = null;
            FileItem userFile = null;
            for (FileItem fileItem : fileItems) {
                logger.info("FieldName" + fileItem.getFieldName());
                if (fileItem.getFieldName().equals("userEmail")) {
                    userEmail = fileItem.getString();
                    logger.info("Using email '" + userEmail + "'");
                } else if (fileItem.getFieldName().equals("userFile")) {
                    userFile = fileItem;
                }
            }


            logger.info("File saved sleeping");
            Image image = new Image();
            Thread.sleep(30000);
            logger.info("Saving image for user '" + userEmail + "'");
            image.setImageKey(((BlobStoreFileItem) userFile).getName());
            List<User>  users = ObjectifyService.ofy().load().type(User.class).filter("email", userEmail).list();
            User user = null;
            if (CollectionUtils.isNotEmpty(users)) user = users.get(0);
            if (user != null) {
                image.setUser(user);
                ObjectifyService.ofy().save().entity(image);
                Queue queue = QueueFactory.getQueue(ServletConstants.image_queue_name);

                TaskOptions taskOptions = TaskOptions.Builder.withUrl("/imageProcess").param(ServletConstants.image_key_processor_parameter, image.getImageKey()).param(ServletConstants.user_id_parameter, user.getUserId());
                queue.add(taskOptions);
            } else logger.error("No user matches '" + userEmail + "'");
        } catch (Throwable t) {
            logger.error("An error has occured while uploading image", t);
            resp.sendError(500, t.getMessage() + "\n\n" + Throwables.getStackTraceAsString(t));
        }
    }
}
