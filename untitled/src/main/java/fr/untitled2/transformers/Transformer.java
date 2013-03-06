package fr.untitled2.transformers;

import fr.untitled2.entities.User;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/4/13
 * Time: 7:47 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Transformer<F, T> {

    public T apply(User user, F from) throws Exception;

}
