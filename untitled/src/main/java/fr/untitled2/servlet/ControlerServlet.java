package fr.untitled2.servlet;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Throwables;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/8/13
 * Time: 12:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ControlerServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(ControlerServlet.class);

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = handleUser(req, resp);
        try {
            get(req, resp, user);
        } catch (Throwable t) {
            logError(req, t);
            resp.sendError(500, "An error has occured an email has been sent to administrator");
        }
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = handleUser(req, resp);
        try {
            post(req, resp, user);
        } catch (Throwable t) {
            logError(req, t);
            resp.sendError(500, "An error has occured an email has been sent to administrator");
        }
    }

    private User handleUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        com.google.appengine.api.users.User user = userService.getCurrentUser();

        if (user == null) {
            response.sendRedirect(userService.createLoginURL(getRequestUrl(request)));
            return null;
        }

        User applicationUser = ObjectifyService.ofy().load().key(Key.create(User.class, user.getEmail())).get();
        if (applicationUser == null) {
            applicationUser = new User(user.getEmail());
            ObjectifyService.ofy().save().entity(applicationUser);
        }
        return applicationUser;
    }

    private String getRequestUrl(HttpServletRequest request) {
        StringBuilder result = new StringBuilder(request.getRequestURI());
        Map<String, String> parameters = request.getParameterMap();
        int position = 0;
        for (String parameterName : parameters.keySet()) {
            if (position == 0) result.append("?");
            else result.append("&");
            result.append(parameterName).append("=").append(parameters.get(parameterName));
            position++;
        }
        return result.toString();
    }

    private void logError(HttpServletRequest request, Throwable t) throws IOException {
        logger.error("Erreur du service : '" + request.getRequestURI() + "'", t);
        MailService.Message message = new MailService.Message();
        message.setTo("corentin.escoffier@gmail.com");
        message.setSubject("MyPictureLog Error");
        message.setTextBody("Une erreur s'est produite, " + t.getMessage() + "\n\n" + Throwables.getStackTraceAsString(t));
        MailServiceFactory.getMailService().send(message);
    }

    protected void get(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        response.sendError(405, "Not supported");
    }

    protected void post(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        response.sendError(405, "Not supported");
    }

}
