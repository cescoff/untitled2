package fr.untitled2.mvc;

import fr.untitled2.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 7:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class AuthenticatedController {

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) return (User) principal;
        return null;
    }

}
