package fr.untitled2.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/5/13
 * Time: 6:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class SignUtils {

    public static String calculateSha1Digest(String source) {
        return new String(DigestUtils.sha1Hex(source));
    }

}
