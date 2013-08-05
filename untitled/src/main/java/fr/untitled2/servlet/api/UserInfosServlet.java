package fr.untitled2.servlet.api;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.gwt.core.client.JsonUtils;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.UserInfos;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.entities.User;
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
 * Date: 3/16/13
 * Time: 12:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserInfosServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(UserInfosServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        com.google.appengine.api.users.User user = null;
        String json = IOUtils.toString(req.getInputStream());
        UserPreferences userPreferences = JSonUtils.readJson(UserPreferences.class, json);
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
            applicationUser = new User();
            applicationUser.setUserId("gmail:" + user.getEmail());
            applicationUser.setEmail(user.getEmail());
            applicationUser.setAuthMode(User.AuthMode.GOOGLE);
            applicationUser.setDateFormat(userPreferences.getDateFormat());
            applicationUser.setLocale(userPreferences.getPreferedLocale());
            applicationUser.setEnabled(true);
            applicationUser.setTimeZoneId(userPreferences.getCameraDateTimeZone());
            ObjectifyService.ofy().save().entity(applicationUser);
        }
        UserInfos userInfos = new UserInfos();
        userInfos.setUserId(applicationUser.getUserId());
        resp.getOutputStream().write(JSonUtils.writeJson(userInfos).getBytes());
    }
}
