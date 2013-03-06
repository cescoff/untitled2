package fr.untitled2.utils;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.User;
import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/5/13
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersistenceUtils {

    public static Key<Log> persistTrip(Log aLog, User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null for persistence");
        aLog.setUser(user);
        return ObjectifyService.ofy().save().entity(aLog).now();
    }

    public static <T> void persist(T object) {
        ObjectifyService.ofy().save().entity(object);
    }

}
