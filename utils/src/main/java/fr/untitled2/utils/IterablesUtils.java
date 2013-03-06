package fr.untitled2.utils;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/4/13
 * Time: 11:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class IterablesUtils {

    public static final <T> boolean isEmpty(Iterable<T> collection) {
        if (collection == null) return true;
        if (!collection.iterator().hasNext()) return true;
        return false;
    }

    public static final <T> boolean isNotEmpty(Iterable<T> collection) {
        return !isEmpty(collection);
    }

}
