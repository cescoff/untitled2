package fr.untitled2.common.oauth;

import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.entities.raspi.*;
import fr.untitled2.utils.JAXBUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/16/13
 * Time: 12:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class AppEngineOAuthClientTest {
    @Test
    public void testGetUserInfos() throws Exception {
        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient();
        String url = appEngineOAuthClient.getTokenValidationUrl();
        System.out.println(url);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String verificationCode = br.readLine();
        appEngineOAuthClient.validateTokens(verificationCode.trim());
        appEngineOAuthClient.getUserInfos(new UserPreferences());
    }

    @Test
    public void testPushFile() throws Exception {
        File aFile = new File("/Users/corentinescoffier/Pictures/2013/2013-09-22/DSC_4680.NEF");
        //File aFile = new File("/Users/corentinescoffier/Desktop/D22_3530.jpg");
        //File aFile = new File("/Users/corentinescoffier/Desktop/maps.html");
        ServerConfig serverConfig = JAXBUtils.unmarshal(ServerConfig.class, new File("/Users/corentinescoffier/.myPictureLog/serverConfig.xml"));
        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());
        UserPreferences userPreferences = appEngineOAuthClient.getUserPreferences();
        System.out.println(userPreferences.getCameraDateTimeZone());
        FileRef fileRef = appEngineOAuthClient.pushFile(aFile);
        File newFile = new File(aFile.getPath() + ".2");
        FileOutputStream fileOutputStream = new FileOutputStream(newFile);
        IOUtils.copy(appEngineOAuthClient.getFile(fileRef), fileOutputStream);
        fileOutputStream.close();
    }

    @Test
    public void testBatchTaskApi() throws Exception {
        /*
        "getNextBatchTask"
        "markBatchTaskAsDone"
        "registerBatchTask"
        */

        String batchletClassName = "fr.untitled2.raspi.batchlet.PhotoScanBatchlet";

        ServerConfig serverConfig = JAXBUtils.unmarshal(ServerConfig.class, new File("/Users/corentinescoffier/.myPictureLog/serverConfig.xml"));
        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());
        RegisterBatchTaskPayload registerBatchTaskPayload = new RegisterBatchTaskPayload();
        registerBatchTaskPayload.setZippedPayload("TOTO");
        registerBatchTaskPayload.setBatchletClass(batchletClassName);
        registerBatchTaskPayload.setServerId(serverConfig.getServerId());


        BatchTaskPayload batchTaskPayload = appEngineOAuthClient.executeCommand(registerBatchTaskPayload, BatchTaskPayload.class, "registerBatchTask");
        System.out.println("REGISTERED BATCHTASK->'" + batchTaskPayload.getBatchTaskId() + "'");

        BatchTaskPayload executionContext = appEngineOAuthClient.executeCommand(serverConfig, BatchTaskPayload.class, "getNextBatchTask");
        System.out.println("EXECUTION_CONTEXT->BatchId->'" + executionContext.getBatchTaskId() + "'");
        System.out.println("EXECUTION_CONTEXT->TaskName->'" + executionContext.getBatchletClassName() + "'");
        System.out.println("EXECUTION_CONTEXT->PayLoad->'" + executionContext.getZippedPayload() + "'");

        BatchTaskPayload executionResult = new BatchTaskPayload();
        executionResult.setLog("LOG");
        executionResult.setZippedPayload("TITI");
        executionResult.setBatchTaskId(executionContext.getBatchTaskId());
        executionResult.setSuccess(true);

        SimpleResponse simpleResponse = appEngineOAuthClient.executeCommand(executionResult, SimpleResponse.class, "markBatchTaskAsDone");
        System.out.println(simpleResponse.isState());

    }

}
