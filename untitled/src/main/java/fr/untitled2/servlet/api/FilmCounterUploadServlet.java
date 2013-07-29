package fr.untitled2.servlet.api;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.FilmCounter;
import fr.untitled2.common.entities.LogRecording;
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
 * Date: 3/29/13
 * Time: 5:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilmCounterUploadServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(FilmCounterUploadServlet.class);

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
        FilmCounter filmCounter = JSonUtils.readJson(FilmCounter.class, json);

        fr.untitled2.entities.FilmCounter entity = new fr.untitled2.entities.FilmCounter();
        entity.setName(filmCounter.getFilmId());
        entity.setUser(applicationUser);

        for (FilmCounter.Pause pause : filmCounter.getPauses()) {
            fr.untitled2.entities.FilmCounter.Pause entityPause = new fr.untitled2.entities.FilmCounter.Pause();
            entityPause.setLocalDateTime(pause.getPauseDateTime());
            entityPause.setPosition(pause.getPosition());
            entityPause.setLatitude(pause.getLatitude());
            entityPause.setLongitude(pause.getLongitude());
            entity.getPauses().add(entityPause);
        }

        ObjectifyService.ofy().save().entity(entity);
        resp.getOutputStream().write("OK".getBytes());
    }

}
