package fr.untitled2.utils;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import fr.untitled2.entities.User;
import fr.untitled2.mvc.MVCConstants;
import fr.untitled2.servlet.ServletConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/16/13
 * Time: 2:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserUtils {

    private static Logger logger = LoggerFactory.getLogger(UserUtils.class);

    public static boolean isReturningUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) logger.info(authentication.getPrincipal() + "->" + authentication.getPrincipal().getClass().getName());
        else logger.info("Authentication ou Principal null");

        return authentication != null && authentication.getPrincipal() != null && !isAnonymousUser();
    }

    public static boolean isAnonymousUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) logger.info(authentication.getPrincipal() + "->" + authentication.getPrincipal().getClass().getName());
        else logger.info("Authentication ou Principal null");

        return authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal());
    }

    public static boolean isConnected() {
        return isReturningUser();
    }

    public static boolean isGoogleLoggedUser() {
        UserService userService = UserServiceFactory.getUserService();
        return userService.getCurrentUser() != null;
    }

    public static com.google.appengine.api.users.User getGoogleUser() {
        return UserServiceFactory.getUserService().getCurrentUser();
    }

    public static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return  (User)authentication.getPrincipal();
    }

    public static String getUserNickName() {
        User user = getUser();
        if (user != null) {
            String nick = user.getNickName();
            if (StringUtils.isNotEmpty(nick)) {
                return nick;
            } else if (UserServiceFactory.getUserService().isUserLoggedIn()) {
                com.google.appengine.api.users.User googleUser = UserServiceFactory.getUserService().getCurrentUser();
                return googleUser.getNickname();
            }
        }
        return "unknown";
    }

    public static String getProfileUrl(HttpServletRequest request) {
        String returnPath = getRequestUrl(request);
        if (StringUtils.isNotEmpty(returnPath)) {
            try {
                returnPath = URLEncoder.encode(returnPath, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("Impossible d'encode l'url", e);
                return "/ihm/profile.htm";
            }

            return "/ihm/profile.htm?" + MVCConstants.return_path_parameter + "=" + returnPath;
        }
        return "/ihm/profile.htm";
    }

    public static String createLoginUrl(HttpServletRequest request) {
        try {
            return "/ihm/start.htm?returnPath=" + URLEncoder.encode(getRequestUrl(request), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Impossible d'encoder l'url '" + getRequestUrl(request) + "'", e);
            return "/ihm/start.htm";
        }
    }

    public static String getRequestUrl(HttpServletRequest request) {
        if (request.getAttribute(ServletConstants.currentPageUrl) != null) return (String) request.getAttribute(ServletConstants.currentPageUrl);

        if (request.getRequestURI().indexOf("/maps/view") >= 0) {
            return "/ihm/logs";
        }
        StringBuilder result = new StringBuilder(request.getRequestURI());
        Enumeration parameters = request.getParameterNames();
        int position = 0;
        while (parameters.hasMoreElements()) {
            String parameterName = (String) parameters.nextElement();
            if (position == 0) result.append("?");
            else result.append("&");
            result.append(parameterName).append("=").append(request.getParameter(parameterName));
            position++;
        }
        return result.toString();
    }


}
