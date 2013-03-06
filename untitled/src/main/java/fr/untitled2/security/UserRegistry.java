package fr.untitled2.security;

import fr.untitled2.entities.User;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 7:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserRegistry {

    User findUser(String userEmail);
    void registerUser(User newUser);
    void removeUser(String userEmail);

}
