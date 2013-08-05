package fr.untitled2.common.oauth;

import fr.untitled2.common.entities.UserPreferences;
import org.junit.Test;

import java.io.BufferedReader;
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
}
