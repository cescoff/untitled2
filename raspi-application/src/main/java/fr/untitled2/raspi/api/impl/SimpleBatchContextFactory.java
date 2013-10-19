package fr.untitled2.raspi.api.impl;

import fr.untitled2.common.entities.raspi.BatchTaskPayload;
import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.raspi.api.BatchContext;
import fr.untitled2.raspi.api.BatchContextFactory;
import fr.untitled2.raspi.api.Batchlet;
import fr.untitled2.raspi.utils.CommandLineUtils;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 10:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleBatchContextFactory implements BatchContextFactory {

    private AppEngineOAuthClient appEngineOAuthClient;

    @Override
    public <T extends Batchlet> BatchContext getBatchContext(BatchTaskPayload batchTaskPayload) throws Exception {
        if (appEngineOAuthClient == null) {
            ServerConfig serverConfig = null;

            try {
                serverConfig = CommandLineUtils.getServerConfig();
            } catch (Throwable t) {
                Thread.sleep(1000);
                serverConfig = CommandLineUtils.getServerConfig();
            }

            if (serverConfig != null && serverConfig.isConnected()) {
                appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());
            }
        }
        if (appEngineOAuthClient == null) throw new Exception("No connection to remote server");

        File workDir = CommandLineUtils.getWorkDir();

        return new SimpleBatchContext(appEngineOAuthClient, new File(workDir, batchTaskPayload.getBatchTaskId()), batchTaskPayload.getLogLevel());
    }
}
