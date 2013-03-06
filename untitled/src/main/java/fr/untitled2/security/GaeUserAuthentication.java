package fr.untitled2.security;

import fr.untitled2.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 7:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class GaeUserAuthentication implements Authentication {

    private static final long serialVersionUID = 1L;

    private User user;

    private boolean authenticated = false;

    private Object credentials;

    private Object details;

    public GaeUserAuthentication(User user, Object details) {
        this.user = user;
        this.details = details;
    }

    public GaeUserAuthentication(User user, Object credentials, Object details) {
        this.user = user;
        this.credentials = credentials;
        this.details = details;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.user.getRoles();
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return "MyPictureLogAuthentication";
    }

    @Override
    public String toString() {
        return "GaeUserAuthentication{" +
                "user=" + user +
                ", authenticated=" + authenticated +
                ", credentials=" + credentials +
                ", details=" + details +
                '}';
    }
}
