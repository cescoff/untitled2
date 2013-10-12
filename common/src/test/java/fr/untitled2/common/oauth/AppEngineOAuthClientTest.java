package fr.untitled2.common.oauth;

import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.entities.raspi.FileRef;
import fr.untitled2.common.entities.raspi.ServerConfig;
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

}
