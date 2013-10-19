package fr.untitled2.business;

import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.Batchlet;
import fr.untitled2.entities.User;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class BatchletBusiness {


    public Batchlet getMatchingBatchlet(String className, User user) {
        List<Batchlet> userBatchlets = ObjectifyService.ofy().load().type(Batchlet.class).filter("user", user).list();
        for (Batchlet userBatchlet : userBatchlets) {
            if (userBatchlet.getClassName().equals(className)) return userBatchlet;
        }
        return null;
    }

}
