package fr.untitled2.security;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.mvc.AppRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 7:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleAccountsAuthenticationProvider implements AuthenticationProvider {

    private static Logger logger = LoggerFactory.getLogger(GoogleAccountsAuthenticationProvider.class);

    private UserRegistry userRegistry;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.info("ClassForPrincipal:" + authentication.getPrincipal().getClass().getName());

        if (authentication.getPrincipal() instanceof User) {
            User googleUser = (User) authentication.getPrincipal();

            fr.untitled2.entities.User user = userRegistry.findUser(googleUser.getEmail());

            if (user == null) {
                // User not in registry. Needs to register
                logger.info("Creating a new user");
                user = new fr.untitled2.entities.User(googleUser.getEmail());
                user.setUserId("gmail:" + googleUser.getEmail());
                user.getRoles().add(AppRole.NEW_USER);
            } else if (!user.getKnwonUserIds().contains("gmail:" + googleUser.getEmail())) {
                user.getKnwonUserIds().add("gmail:" + googleUser.getEmail());
                ObjectifyService.ofy().save().entity(user);
            }

            if (!user.isEnabled()) {
                logger.info("User is disabled");
                throw new DisabledException("Account is disabled");
            }
            Authentication result = new GaeUserAuthentication(user, authentication.getDetails());
            result.setAuthenticated(true);
            return result;
        } else if (authentication.getPrincipal() instanceof String) {
            authentication.setAuthenticated(false);
        }
        return authentication;
    }

    public final boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setUserRegistry(UserRegistry userRegistry) {
        this.userRegistry = userRegistry;
    }
}