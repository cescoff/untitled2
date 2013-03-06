package fr.untitled2.servlet.process;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.CacheHelper;
import fr.untitled2.business.ImageBusiness;
import fr.untitled2.business.MapBusiness;
import fr.untitled2.entities.*;
import fr.untitled2.imageconversion.AnswerXml;
import fr.untitled2.imageconversion.Requester;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.GeoLocalisationUtils;
import fr.untitled2.utils.ImageUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageProcessServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(ImageProcessServlet.class);

    private MapBusiness mapBusiness = new MapBusiness();

    private ImageBusiness imageBusiness = new ImageBusiness();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String imageKey = req.getParameter(ServletConstants.image_key_processor_parameter);
            String userId = req.getParameter(ServletConstants.user_id_parameter);

            FileService fileService = FileServiceFactory.getFileService();

            Image image = ObjectifyService.ofy().load().key(Key.create(Image.class, imageKey)).get();

            if (image == null) {
                logger.warn("Image inexistante");
                image = new Image();
                image.setImageKey(imageKey);
                image.setReady(false);
                User user = ObjectifyService.ofy().load().key(Key.create(User.class, userId)).get();

                image.setUser(user);
                ObjectifyService.ofy().save().entity(image);
            }

            if (image.getUser() == null) {
                logger.info("Ajout forcé de l'utilisteur");
                User user = ObjectifyService.ofy().load().key(Key.create(User.class, userId)).get();
                if (user == null) logger.error("Impossible de charger l'utilisateur '" + userId + "'");
                image.setUser(user);
                if (image.getUser() == null) logger.error("Impossible de setter un user a l'image");
            }
            User user = image.getUser();
            logger.info("Reduction en cours de l'image (" + image.getUser() + ")");

            AppEngineFile appEngineFile = fileService.getBlobFile(new BlobKey(imageKey));
            FileReadChannel fileReadChannel = FileServiceFactory.getFileService().openReadChannel(appEngineFile, false);
            InputStream imageInputStream = Channels.newInputStream(fileReadChannel);
            byte[] imageData = IOUtils.toByteArray(imageInputStream);
            try {
                imageInputStream.close();
                fileReadChannel.close();
            } catch (Throwable t) {
                logger.error("Impossible de cloturer l'inputstream", t);
                MailService.Message message = new MailService.Message();
                message.setSender("corentin.escoffier@gmail.com");
                message.setTo("corentin.escoffier@gmail.com");
                message.setSubject("Impossible de cloturer l'inputstream : " + t.getMessage());
                message.setTextBody(Throwables.getStackTraceAsString(t));
                MailServiceFactory.getMailService().send(message);
            }

            ImageUtils.handleMetaData(image, imageData);
            logger.info("Les metas ont ete extraites");

            ImageUtils.buildReducedImages(image, imageData);

            logger.info("L'image a ete reduite");
            image.setName(StringUtils.substring(imageKey, StringUtils.lastIndexOf(imageKey, "/") + 1));
            image.setTimeZoneId(user.getTimeZoneId());

            GeoLocalisationUtils.updateImagesLocalisation(Lists.newArrayList(image), Lists.newArrayList(ObjectifyService.ofy().load().type(Log.class).filter("user", image.getUser())));

            if (image.getWidth() < 200 || image.getHeight() < 200) {
                logger.info("Le format de fichier n'est pas supporté, utilisation du service de conversion en ligne");
                AnswerXml answerXml = Requester.postImageToConvert(imageKey);

                ObjectifyService.ofy().save().entity(image);

                ImageConversionJob imageConversionJob = new ImageConversionJob();

                if (answerXml.getParams() != null) imageConversionJob.setHashCode(answerXml.getParams().getHash());
                else {
                    logger.error("Aucun parametres retournes : statut : " + answerXml.getStatus().getCode() + "->" + answerXml.getStatus().getMessage());
                    imageConversionJob.setOnError(true);
                    imageConversionJob.setHashCode(SignUtils.calculateSha1Digest(imageKey));
                }
                imageConversionJob.setImage(image);

                ObjectifyService.ofy().save().entity(imageConversionJob);
                Queue queue = QueueFactory.getQueue(ServletConstants.image_conversion_queue_name);
                TaskOptions taskOptions = TaskOptions.Builder.withUrl("/imageConvert").param(ServletConstants.conversion_job_id_parameter, imageConversionJob.getHashCode());
                queue.add(taskOptions);
            } else {
                image.setReady(true);

                ObjectifyService.ofy().save().entity(image).now();
            }
            logger.info("La nouvelle image a ete persistee avec la clef : " + imageKey);
            CacheHelper.removeImageList(user, 0);
            imageBusiness.getImageList(0, user);
            logger.info("La liste des images a été recachee");

            for (PictureMap pictureMap : ObjectifyService.ofy().load().type(PictureMap.class).filter("user", user).list()) {
                CacheHelper.removePictureMapImageList(pictureMap);
                mapBusiness.getMapMarkers(DateTimeFormat.forPattern(user.getDateFormat()), pictureMap.getSharingKey(), user);
            }
            logger.info("La liste des image des cartes a ete mise a jour");
        } catch (Throwable t) {
            logger.error("Une erreur s'est produite lors du process d'une image", t);
            MailService mailService = MailServiceFactory.getMailService();
            MailService.Message message = new MailService.Message("corentin.escoffier@gmail.com", "corentin.escoffier@gmail.com", "Erreur de conversion d'image : " + t.getMessage(), Throwables.getStackTraceAsString(t));
            mailService.send(message);
        }
    }

}
