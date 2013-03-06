package fr.untitled2.imageconversion;

import fr.untitled2.entities.ImageConversionJob;
import fr.untitled2.utils.JAXBUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 10:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequesterTest {
    @Test
    public void testPostImageToConvert() throws Exception {
        File nefFile = new File("/Users/corentinescoffier/Desktop/TestImages/D22_3099.NEF");

        AnswerXml answerXml = Requester.postImageToConvert(null);
        System.out.println("File posted");

        ImageConversionJob imageConversionJob = new ImageConversionJob();
        imageConversionJob.setHashCode(answerXml.getParams().getHash());

        System.out.println("Preparing to sleep");
        Thread.sleep(30000);

        while (true) {
            answerXml = Requester.getQueueStatus(imageConversionJob);
            System.out.println(JAXBUtils.marshal(answerXml, true));
            if (Requester.isReadyToDownload(answerXml)) {
                System.out.println("File is ready");
                File outputFile = new File("/Users/corentinescoffier/Desktop/TestImages/D22_3099_converted.jpg");
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                fileOutputStream.write(Requester.getConvertedFile(answerXml));
                fileOutputStream.close();
                return;
            } else {
                System.out.println("File is NOT ready");
            }
            Thread.sleep(30000);

        }

    }
}
