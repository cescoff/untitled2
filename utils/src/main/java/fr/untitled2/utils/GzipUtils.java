package fr.untitled2.utils;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class GzipUtils {

    public static String zipString(String string) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(base64OutputStream);
        gzipOutputStream.write(string.getBytes());
        gzipOutputStream.close();
        base64OutputStream.close();
        byteArrayOutputStream.close();
        return new String(byteArrayOutputStream.toByteArray());
    }

    public static String unzipString(String string) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(string.getBytes());
        Base64InputStream base64InputStream = new Base64InputStream(byteArrayInputStream);
        GZIPInputStream gzipInputStream = new GZIPInputStream(base64InputStream);

        return IOUtils.toString(gzipInputStream);
    }

}
