package fr.untitled2.imageconversion;

import fr.untitled2.utils.JAXBUtils;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 10:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnswerXmlTest {
    @Test
    public void testGetStatus() throws Exception {
        AnswerXml answerXml = new AnswerXml();

        AnswerXml.Status status = new AnswerXml.Status();
        status.setCode("100");
        status.setMessage("The file has been successfully converted.");

        answerXml.setStatus(status);

        AnswerXml.Params params = new AnswerXml.Params();

        params.setChecksum("679e4138e0769191d7a4eb44e9165b95");
        params.setConvertTo("jpg");
        params.setDateProcessed("1262110552");
        params.setDirectDownload("http://www.online-convert.com/download-file/<unique_id>");
        params.setDownloadCounter("2");
        params.setHash("c6c81eceeb4e64b6b9810b6d93920200");
        params.setMimeType("image/jpg");
        params.setTargetSize("6308");

        answerXml.setParams(params);

        System.out.println(JAXBUtils.marshal(answerXml, true));
    }
}
