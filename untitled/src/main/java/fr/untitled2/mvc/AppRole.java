package fr.untitled2.mvc;

import org.springframework.security.core.GrantedAuthority;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 7:26 PM
 * To change this template use File | Settings | File Templates.
 */
public enum AppRole implements GrantedAuthority {
    ADMIN (0),
    NEW_USER (1),
    USER (2);

    private int bit;

    AppRole(int bit) {
        this.bit = bit;
    }

    public String getAuthority() {
        return toString();
    }
}