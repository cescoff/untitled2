package fr.untitled2.security;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.labs.repackaged.com.google.common.base.Predicate;
import com.google.appengine.labs.repackaged.com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.User;
import fr.untitled2.utils.CollectionUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 7:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class GaeUserRegistry implements UserRegistry {

    @Override
    public User findUser(final String userEmail) {
        List<User> users = ObjectifyService.ofy().load().type(User.class).filter("email", userEmail).list();
        if (CollectionUtils.isEmpty(users)) {
            users = Lists.newArrayList(Collections2.filter(ObjectifyService.ofy().load().type(User.class).list(), new Predicate<User>() {
                @Override
                public boolean apply(User user) {
                    return userEmail.equals(user.getEmail());
                }
            }));
        }
        if (CollectionUtils.isNotEmpty(users)) return users.get(0);
        return null;
    }

    @Override
    public void registerUser(User newUser) {
        ObjectifyService.ofy().save().entity(newUser);
        try {
            MailService.Message message = new MailService.Message();
            message.setSender("corentin.escoffier@gmail.com");
            message.setTo("corentin.escoffier@gmail.com");
            message.setSubject("New user registration");
            message.setTextBody("Hi,\n\nA new user is registred.\n\nHis email address is " + newUser.getUserId() + "\n\nHope he will be happy");
            MailServiceFactory.getMailService().send(message);
        } catch (Throwable t) {
        }

    }

    @Override
    public void removeUser(String userEmail) {
        ObjectifyService.ofy().delete().entity(findUser(userEmail));
    }
}
