package fr.untitled2.servlet.api;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserInfos;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.TrackPoint;
import fr.untitled2.entities.User;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.JSonUtils;
import org.apache.commons.io.IOUtils;
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
        logger.info("JSON:" + json);
        LogRecording logRecording = JSonUtils.readJson(LogRecording.class, json);

        Log log = new Log();
        log.setTimeZoneId(logRecording.getDateTimeZone());
        log.setName(logRecording.getName());
        if (CollectionUtils.isNotEmpty(logRecording.getRecords())) {
            log.setStartTime(LogRecording.DATE_ORDERING.sortedCopy(logRecording.getRecords()).get(0).getDateTime());
            log.setEndTime(LogRecording.DATE_ORDERING.reverse().sortedCopy(logRecording.getRecords()).get(0).getDateTime());
        }
        log.setUser(applicationUser);
        log.setValidated(true);

        for (LogRecording.LogRecord logRecord : logRecording.getRecords()) {
            TrackPoint trackPoint = new TrackPoint();
            trackPoint.setPointDate(logRecord.getDateTime());
            trackPoint.setLatitude(logRecord.getLatitude());
            trackPoint.setLongitude(logRecord.getLongitude());
            log.getTrackPoints().add(trackPoint);
        }
        ObjectifyService.ofy().save().entity(log).now();
        resp.getOutputStream().write("OK".getBytes());
    }
}
