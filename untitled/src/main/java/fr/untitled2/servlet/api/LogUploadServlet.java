package fr.untitled2.servlet.api;

import com.google.appengine.api.files.*;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserInfos;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.LogPersistenceJob;
import fr.untitled2.entities.TrackPoint;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/13/13
 * Time: 5:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class LogUploadServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(UserInfosServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        String json = IOUtils.toString(req.getInputStream());
        LogRecording logRecording = JSonUtils.readJson(LogRecording.class, json);

        try {
            LogPersistenceJob logPersistenceJob = new LogPersistenceJob();
            logPersistenceJob.setKey(SignUtils.calculateSha1Digest(user.getEmail() + logRecording.getName()));
            logPersistenceJob.setLogRecording(logRecording);
            logPersistenceJob.setUserKey(Key.create(User.class, user.getUserId()));
            Key<LogPersistenceJob> logPersistenceJobKey = ObjectifyService.ofy().save().entity(logPersistenceJob).now();

            Queue queue = QueueFactory.getQueue(ServletConstants.log_persistence_queue_name);

            TaskOptions taskOptions = TaskOptions.Builder.withUrl("/logPersistence").param(ServletConstants.log_peristence_job_key, logPersistenceJobKey.getString());
            queue.add(taskOptions);

        } catch (Throwable t) {
            logger.error("Impossible de persister un LogRecording", t);
            return;
        }


        resp.getOutputStream().write("OK".getBytes());
    }
}
