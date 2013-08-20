package fr.untitled2.entities;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 20/08/13
 * Time: 11:20
 * To change this template use File | Settings | File Templates.
 */
public class LogPersistenceJobTest {
    @Test
    public void testGZIP() throws Exception {
        File inputFile = new File("C:\\Donnees\\PERSO\\escoffier_c\\untitled\\untitled2\\untitled\\src\\main\\java\\fr\\untitled2\\entities\\LogPersistenceJob.java");
        File outPutFile = new File("C:\\Documents and Settings\\escoffier_c\\Bureau\\test.txt");

        String fileString = toString(inputFile);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(base64OutputStream);

        gzipOutputStream.write(fileString.getBytes());
        gzipOutputStream.close();
        base64OutputStream.close();

        String fileStored = new String(byteArrayOutputStream.toByteArray());
        System.out.println(fileStored);
        System.out.println(fileStored.length() + "<=>" + fileString.length());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileStored.getBytes());
        Base64InputStream base64InputStream = new Base64InputStream(byteArrayInputStream);
        GZIPInputStream gzipInputStream = new GZIPInputStream(base64InputStream);

        FileOutputStream fileOutputStream = new FileOutputStream(outPutFile);
        IOUtils.copy(gzipInputStream, fileOutputStream);
    }

    private String toString(File aFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(aFile);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(fileInputStream, byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }

}
