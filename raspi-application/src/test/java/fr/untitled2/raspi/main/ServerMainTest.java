package fr.untitled2.raspi.main;

import oauth.signpost.OAuth;
import oauth.signpost.signature.SignatureBaseString;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/29/13
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerMainTest {

    private static final String MAC_NAME = "HmacSHA1";

    private static Base64 base64 = new Base64();;

    @Test
    public void testMain() throws Exception {
        String keyString = OAuth.percentEncode("1068597606057.apps.googleusercontent.com") + '&'
                + OAuth.percentEncode("zFwtAxv4h1ohMIlFaNl93FmS");
        byte[] keyBytes = keyString.getBytes(OAuth.ENCODING);

        SecretKey key = new SecretKeySpec(keyBytes, MAC_NAME);
        Mac mac = Mac.getInstance(MAC_NAME);
        mac.init(key);

        String sbs = "POST&https%3A%2F%2Fx5-teak-clarity-4.appspot.com%2F_ah%2FOAuthGetRequestToken&oauth_callback%3Doob%26oauth_consumer_key%3D1068597606057.apps.googleusercontent.com%26oauth_nonce%3D-5522802700466929699%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1380467521%26oauth_version%3D1.0";
        OAuth.debugOut("SBS", sbs);
        byte[] text = sbs.getBytes(OAuth.ENCODING);

        System.out.println(base64Encode(mac.doFinal(text)).trim());

    }

    protected String base64Encode(byte[] b) {
        return new String(base64.encode(b));
    }
}
