package fr.untitled2.common.entities.raspi;

import fr.untitled2.utils.JSonUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 9:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerRegistrationConfigTest {
    @Test
    public void testXml() throws Exception {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCpuCoreCount(2);
        serverConfig.setServerName("raspimedia");

        ServerRegistrationConfig serverRegistrationConfig = new ServerRegistrationConfig();
        serverRegistrationConfig.setServerConfig(serverConfig);
        serverRegistrationConfig.setTokenGenerationUrl("http://www.google.com");
        serverRegistrationConfig.setUrlGenerationTime(LocalDateTime.now());

        JSonUtils.writeJson(serverRegistrationConfig, System.out);
    }
}
