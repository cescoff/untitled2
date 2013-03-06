package fr.untitled2.imageconversion;

import com.google.appengine.api.blobstore.BlobKey;
import fr.untitled2.entities.ImageConversionJob;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.JAXBUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 10:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class Requester {

    private static Logger logger = LoggerFactory.getLogger(Requester.class);

    private static String apiKey = "bbe3e652aec9200772769aa3ae07dafd";

    private static String enqueueUrl = "http://api.online-convert.com/queue-insert";

    private static String queueStatusUrl = "http://api.online-convert.com/queue-status ";

    private static String deleteFileUrl = "http://api.online-convert.com/queue-manager";

    public static AnswerXml postImageToConvert(String blobKey) throws Exception {
        PostXml postXml = new PostXml();
        postXml.setApiKey(apiKey);
        postXml.setTestMode("false");
        postXml.setSourceUrl("http://x5-teak-clarity-4.appspot.com/fileServe?" + ServletConstants.blobstore_key_parameter + "=" + blobKey);
/*
        postXml.setNotificationUrl(responseUrl);
*/

        String xmlToPost = JAXBUtils.marshal(postXml);
        String query = String.format("queue=%s", URLEncoder.encode(xmlToPost, "UTF-8"));

        HttpURLConnection connection = (HttpURLConnection) new URL(enqueueUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.getOutputStream().write(query.getBytes());
        logger.info("File posted");
        InputStream inputStream = connection.getInputStream();
        return JAXBUtils.unmarshal(AnswerXml.class, IOUtils.toString(inputStream));
    }

    public static AnswerXml getQueueStatus(String hash) throws Exception {
        PostXml postXml = new PostXml();
        postXml.setApiKey(apiKey);
        postXml.setHash(hash);

        String xmlToPost = JAXBUtils.marshal(postXml);
        String query = String.format("queue=%s", URLEncoder.encode(xmlToPost, "UTF-8"));
        logger.info("Query : " + query);
        HttpURLConnection connection = (HttpURLConnection) new URL(queueStatusUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.getOutputStream().write(query.getBytes());

        InputStream inputStream = connection.getInputStream();
        AnswerXml answerXml = JAXBUtils.unmarshal(AnswerXml.class, IOUtils.toString(inputStream));
        if ("105".equals(answerXml.getStatus().getCode()) || "106".equals(answerXml.getStatus().getCode())) {
            throw new Exception("Code retour '" + answerXml.getStatus().getCode() + "' non valide");
        }
        return answerXml;
    }

    public static AnswerXml getQueueStatus(ImageConversionJob imageConversionJob) throws Exception {
        return getQueueStatus(imageConversionJob.getHashCode());
    }

    public static boolean isReadyToDownload(AnswerXml answerXml) {
        return "100".equals(answerXml.getStatus().getCode());
    }

    public static void deleteImage(ImageConversionJob imageConversionJob) throws Exception {
        PostXml postXml = new PostXml();
        postXml.setHash(imageConversionJob.getHashCode());
        postXml.setApiKey(apiKey);
        postXml.setTargetMethod("deleteFile");

        String xmlToPost = JAXBUtils.marshal(postXml);
        String query = String.format("queue=%s", URLEncoder.encode(xmlToPost, "UTF-8"));
        logger.info("Query : " + query);
        HttpURLConnection connection = (HttpURLConnection) new URL(queueStatusUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.getOutputStream().write(query.getBytes());

    }

    public static byte[] getConvertedFile(AnswerXml answerXml) throws Exception {
        if (isReadyToDownload(answerXml)) {
            HttpURLConnection connection = (HttpURLConnection) new URL(answerXml.getParams().getDirectDownload()).openConnection();
            connection.setDoInput(true);
            return IOUtils.toByteArray(connection.getInputStream());
        }
        return null;
    }

}
