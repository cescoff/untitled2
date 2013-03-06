package fr.untitled2.security;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import fr.untitled2.mvc.AppRole;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 7:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class GaeAuthenticationFilter extends GenericFilterBean {

    private static final String REGISTRATION_URL = "/ihm/register.htm";

    private AuthenticationDetailsSource ads = new WebAuthenticationDetailsSource();

    private AuthenticationManager authenticationManager;

    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.info("Filter start");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        request.setAttribute(ServletConstants.currentPageUrl, UserUtils.getRequestUrl((HttpServletRequest) request));

        if (authentication == null) {
            // User isn't authenticated. Check if there is a Google Accounts user
            User googleUser = UserServiceFactory.getUserService().getCurrentUser();
            if (googleUser != null) {
                logger.info("Found user:" + googleUser.getUserId());
                // User has returned after authenticating through GAE. Need to authenticate to Spring Security.
                PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(googleUser, null);
                token.setDetails(ads.buildDetails(request));

                try {
                    logger.info("AuthenticationManager:" + authenticationManager + "(" + this.toString() + ")");
                    authentication = authenticationManager.authenticate(token);
                    // Setup the security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    // Send new users to the registration page.
                    if (authentication.getAuthorities().contains(AppRole.NEW_USER)) {
                        ((HttpServletResponse) response).sendRedirect(REGISTRATION_URL + "?returnPath=" + URLEncoder.encode(getRequestUrl((HttpServletRequest) request), "UTF-8"));
                        return;
                    }
                } catch (AuthenticationException e) {
                    logger.error("Error occured while authenticating user", e);
                    // Authentication information was rejected by the authentication manager
                    failureHandler.onAuthenticationFailure((HttpServletRequest)request, (HttpServletResponse)response, e);
                    return;
                }
            } else logger.info("No Google User found");
        } else logger.info("Authentication : " + authentication);

        chain.doFilter(request, response);
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



    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }
}