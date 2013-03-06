package fr.untitled2.security;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.User;
import fr.untitled2.mvc.AppRole;
import fr.untitled2.mvc.MVCConstants;
import fr.untitled2.utils.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/5/13
 * Time: 2:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleSignInAdapter implements SignInAdapter, ConnectionSignUp {

    private static Logger logger = LoggerFactory.getLogger(SimpleSignInAdapter.class);

    @Override
    public String signIn(String userId, Connection<?> connection, NativeWebRequest request) {
        logger.info("Signin a user : " + userId);
        User user = ObjectifyService.ofy().load().key(Key.create(User.class, userId)).get();

        String userEmail = getUserEmail(connection);
        if (user == null && StringUtils.isNotEmpty(userEmail)) {
            List<User> users = ObjectifyService.ofy().load().type(User.class).filter("email", userEmail).list();
            if (CollectionUtils.isNotEmpty(users)) {
                user = users.get(0);
                user.getKnwonUserIds().add(userId);
            }
        }

        if (user == null) {
            logger.info("User '" + userId + " does not exist");
            user = new User();
            user.setEnabled(true);
            user.getRoles().add(AppRole.NEW_USER);
        } else {
            logger.info("User '" + userId + "' already exist");
        }
        user.setUserId(userId);
        user.setEmail(getUserEmail(connection));
        user.setAuthMode(User.AuthMode.SOCIAL);
        user.setNickName(connection.createData().getDisplayName());
        GaeUserAuthentication gaeUserAuthentication = new GaeUserAuthentication(user, null);
        gaeUserAuthentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(gaeUserAuthentication);

        logger.info("Checking if user is new");
        if (user.getRoles().contains(AppRole.NEW_USER)) {
            logger.info("User is new");
            String returnPath = "";
            String previousUrl = request.getParameter(MVCConstants.return_path_parameter);
            if (StringUtils.isNotEmpty(previousUrl)) {
                returnPath = "?" + MVCConstants.return_path_parameter + "=" + previousUrl;
            }
            logger.info("Redirecting '/ihm/register.htm" + returnPath + "'");
            return "/ihm/register.htm" + returnPath;
        } else {
            logger.info("User is not new");
            String previousUrl = request.getParameter(MVCConstants.return_path_parameter);
            if (StringUtils.isNotEmpty(previousUrl)) {
                logger.info("Older url detected : " + previousUrl);
                try {
                    return URLDecoder.decode(previousUrl, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error("Impossible de décoder l'url", e);
                    return "/ihm/logs";
                }
            } else {
                logger.info("No redirect url");
                return "/ihm/logs";
            }
        }
    }

    @Override
    public String execute(Connection<?> connection) {
        String email = getUserEmail(connection);
        User user = new User();
        user.setEmail(email);
        user.setNickName(connection.createData().getDisplayName());
        user.setAuthMode(User.AuthMode.SOCIAL);
        user.getRoles().add(AppRole.NEW_USER);
        GaeUserAuthentication gaeUserAuthentication = new GaeUserAuthentication(user, null);
        gaeUserAuthentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(gaeUserAuthentication);

        ConnectionData connectionData = connection.createData();
        return connectionData.getProviderId() + ":" + connectionData.getProviderUserId();
    }

    private String getUserEmail(Connection connection) {
        if (connection.getApi() instanceof Facebook) {
            logger.info("Connection is made with facebook");
            Facebook facebook = (Facebook) connection.getApi();
            String email = facebook.userOperations().getUserProfile().getEmail();
            logger.info("Found user email '" + email + "'");
            if (StringUtils.isNotEmpty(email)) return email;
        }
        return null;
    }

}
