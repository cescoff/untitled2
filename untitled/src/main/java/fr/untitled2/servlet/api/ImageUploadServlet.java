package fr.untitled2.servlet.api;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
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

            String fileName = req.getParameter("fileName");

            if (StringUtils.isEmpty(fileName)) fileName = "fileName";
            String fileType = "application/octet-stream";
            if (fileName != null && (fileName.endsWith("jpeg") || fileName.endsWith("jpg"))) fileType = "image/jpg";

            com.google.appengine.api.users.User user = null;
            try {
                user = OAuthServiceFactory.getOAuthService().getCurrentUser();
            } catch (OAuthRequestException e) {
                logger.error("Impossible d'authentifier l'utilisateur", e);
            }
            if (user == null) {
                logger.error("Utilisateur non connecté");
                resp.sendError(403, "Forbidden");
            }
            User applicationUser = null;
            for (User userCandidate : ObjectifyService.ofy().load().type(User.class).list()) {
                if (userCandidate.getEmail().equals(user.getEmail())) {
                    applicationUser = userCandidate;
                    break;
                }
            }
            if (applicationUser == null) {
                logger.error("Impossible de trouver l'utilisateur '" + user.getEmail() + "'");
                resp.sendError(403, "Not registred");
            }

            AppEngineFile appEngineFile = FileServiceFactory.getFileService().createNewBlobFile(fileType, fileName);
            OutputStream outputStream = Channels.newOutputStream(FileServiceFactory.getFileService().openWriteChannel(appEngineFile, true));
            IOUtils.copy(req.getInputStream(), outputStream);
            outputStream.close();

            logger.info("File saved sleeping");
            Image image = new Image();
            logger.info("Saving image for user '" + applicationUser.getEmail() + "'");
            image.setImageKey(FileServiceFactory.getFileService().getBlobKey(appEngineFile).getKeyString());

            image.setUser(applicationUser);
            ObjectifyService.ofy().save().entity(image);
            Queue queue = QueueFactory.getQueue(ServletConstants.image_queue_name);

            TaskOptions taskOptions = TaskOptions.Builder.withUrl("/imageProcess").param(ServletConstants.image_key_processor_parameter, image.getImageKey()).param(ServletConstants.user_id_parameter, applicationUser.getUserId());
            queue.add(taskOptions);
        } catch (Throwable t) {
            logger.error("An error has occured while uploading image", t);
            resp.sendError(500, t.getMessage() + "\n\n" + Throwables.getStackTraceAsString(t));
        }
    }
}
