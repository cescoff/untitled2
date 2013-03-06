package fr.untitled2.security;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Throwables;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.User;
import fr.untitled2.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 6:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleAccountsAuthenticationEntryPoint  implements AuthenticationEntryPoint {

    private static Logger logger = LoggerFactory.getLogger(GoogleAccountsAuthenticationEntryPoint.class);

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        logger.info("Commence:start");
        response.sendRedirect(UserUtils.createLoginUrl(request));
    }

    private void logError(HttpServletRequest request, Throwable t) throws IOException {
        logger.error("Erreur du service : '" + request.getRequestURI() + "'", t);
        MailService.Message message = new MailService.Message();
        message.setTo("corentin.escoffier@gmail.com");
        message.setSubject("MyPictureLog Error");
        message.setTextBody("Une erreur s'est produite, " + t.getMessage() + "\n\n" + Throwables.getStackTraceAsString(t));
        MailServiceFactory.getMailService().send(message);
    }
}
