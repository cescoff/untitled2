package fr.untitled2.utils;

import com.google.common.collect.Iterables;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/28/13
 * Time: 7:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollectionUtils {

    public static final <T> boolean isEmpty(Iterable<T> iterable) {
        return Iterables.isEmpty(iterable);
    }

    public static final <T> boolean isEmpty(Collection<T> collection) {
        if (collection == null || collection.size() == 0) return true;
        if (!collection.iterator().hasNext()) return true;
        return false;
    }

    public static final <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    public static final <T> boolean isNotEmpty(Iterable<T> collection) {
        return !isEmpty(collection);
    }

}
