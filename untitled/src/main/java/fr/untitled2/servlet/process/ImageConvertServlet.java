package fr.untitled2.servlet.process;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Throwables;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.CacheHelper;
import fr.untitled2.business.ImageBusiness;
import fr.untitled2.business.MapBusiness;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.ImageConversionJob;
import fr.untitled2.entities.PictureMap;
import fr.untitled2.entities.User;
import fr.untitled2.imageconversion.AnswerXml;
import fr.untitled2.imageconversion.Requester;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.ImageUtils;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/7/13
 * Time: 12:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImageConvertServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(ImageConvertServlet.class);

    private ImageBusiness imageBusiness = new ImageBusiness();

    private MapBusiness mapBusiness = new MapBusiness();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String conversionJobId = req.getParameter(ServletConstants.conversion_job_id_parameter);
        ImageConversionJob conversionJob = ObjectifyService.ofy().load().key(Key.create(ImageConversionJob.class, conversionJobId)).get();

        if (conversionJob == null) resp.sendError(404, "Job not found");

        logger.info("Recherche du statut de conversion pour le hash '" + conversionJob.getHashCode() + "'");

        if (conversionJob.isOnError()) {
            try {
                AnswerXml answerXml = Requester.postImageToConvert(conversionJob.getImage().getImageKey());
                if (answerXml.getParams() == null) {
                    logger.error("Le statut retourné est :" + answerXml.getStatus().getCode() + " (" + answerXml.getStatus().getMessage() + ")");
                    Queue queue = QueueFactory.getQueue(ServletConstants.image_conversion_queue_name);
                    TaskOptions taskOptions = TaskOptions.Builder.withUrl("/imageConvert").param(ServletConstants.conversion_job_id_parameter, conversionJob.getHashCode());
                    queue.add(taskOptions);
                    return;
                } else {
                    ObjectifyService.ofy().delete().entity(conversionJob);
                    conversionJob.setHashCode(answerXml.getParams().getHash());
                }
            } catch (Throwable t) {
                logger.error("Impossible d'obtenir le statut de la conversion dans l'API de conversion", t);
            }
        }

        try {
            AnswerXml answerXml = Requester.getQueueStatus(conversionJob);
            if (Requester.isReadyToDownload(answerXml)) {
                logger.info("Le hash '" + conversionJob.getHashCode() + "' est pret pour le telechargement");
                Image image = conversionJob.getImage();
                byte[] imageData = Requester.getConvertedFile(answerXml);
                ImageUtils.handleMetaData(image, imageData);
                ImageUtils.buildReducedImages(image, imageData);
                image.setReady(true);

                ObjectifyService.ofy().save().entity(image).now();
                ObjectifyService.ofy().delete().entity(conversionJob);

                User user = image.getUser();
                CacheHelper.removeImageList(user, 0);
                imageBusiness.getImageList(0, user);
                logger.info("La liste des images a été recachee");

                for (PictureMap pictureMap : ObjectifyService.ofy().load().type(PictureMap.class).filter("user", user).list()) {
                    CacheHelper.removePictureMapImageList(pictureMap);
                    mapBusiness.getMapMarkers(DateTimeFormat.forPattern(user.getDateFormat()), pictureMap.getSharingKey(), user);
                }
                logger.info("La liste des image des cartes a ete mise a jour");
            } else {
                ObjectifyService.ofy().save().entity(conversionJob);
                logger.info("Le hash '" + conversionJob.getHashCode() + "' n'est pas encore pret");
                Queue queue = QueueFactory.getQueue(ServletConstants.image_conversion_queue_name);
                TaskOptions taskOptions = TaskOptions.Builder.withUrl("/imageConvert").param(ServletConstants.conversion_job_id_parameter, conversionJob.getHashCode());
                queue.add(taskOptions);
            }
        } catch (Throwable t) {
            logger.error("Impossible de charger l'image convertie", t);
            Image image = conversionJob.getImage();
            image.setError(true);

            ObjectifyService.ofy().save().entity(image);
            ObjectifyService.ofy().delete().entity(conversionJob);

            MailService mailService = MailServiceFactory.getMailService();
            MailService.Message message = new MailService.Message("corentin.escoffier@gmail.com", "corentin.escoffier@gmail.com", "Erreur de conversion d'image : " + t.getMessage(), Throwables.getStackTraceAsString(t));
            mailService.send(message);

        }

    }
}
