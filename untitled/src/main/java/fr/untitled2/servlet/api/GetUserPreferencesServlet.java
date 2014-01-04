package fr.untitled2.servlet.api;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.labs.repackaged.com.google.common.base.Function;
import com.google.appengine.labs.repackaged.com.google.common.collect.Collections2;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.LogBusiness;
import fr.untitled2.business.beans.LogList;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.TrackPoint;
import fr.untitled2.entities.User;
import fr.untitled2.utils.JSonUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/16/13
 * Time: 12:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class GetUserPreferencesServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(GetUserPreferencesServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        if (applicationUser != null) {
            UserPreferences userPreferences = new UserPreferences();
            userPreferences.setCameraDateTimeZone(applicationUser.getTimeZoneId());
            userPreferences.setDateFormat(applicationUser.getDateFormat());
            userPreferences.setPreferedLocale(applicationUser.getLocale());
            userPreferences.setKnownLocations(applicationUser.getKnownLocations());
            JSonUtils.writeJson(userPreferences, resp.getOutputStream());
            return;
        }

        resp.sendError(404, "No matching user");
    }

    private Log getCandidate(Collection<Log> logs, LocalDateTime start, LocalDateTime end) {
        for (Log log : logs) {
            if (log.getStartTime().isBefore(start) && log.getEndTime().isAfter(end)) {
                return log;
            }
        }
        return null;
    }

    private LogRecording getLogRecording(Log log) {
        LogRecording result = new LogRecording();
        result.setName(log.getName());
        result.setDateTimeZone(log.getTimeZoneId());
        result.getRecords().addAll(Collections2.transform(log.getTrackPoints(), new Function<TrackPoint, LogRecording.LogRecord>() {
            @Override
            public LogRecording.LogRecord apply(TrackPoint trackPoint) {
                LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                logRecord.setDateTime(trackPoint.getPointDate());
                logRecord.setLatitude(trackPoint.getLatitude());
                logRecord.setLongitude(trackPoint.getLongitude());
                return logRecord;
            }
        }));

        return result;
    }

}
