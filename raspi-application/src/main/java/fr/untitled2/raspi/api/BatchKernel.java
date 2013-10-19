package fr.untitled2.raspi.api;

import com.beust.jcommander.internal.Lists;
import fr.untitled2.common.entities.raspi.BatchletPayLoad;
import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.raspi.utils.CommandLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 11:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class BatchKernel {

    private static Logger logger = LoggerFactory.getLogger(BatchKernel.class);

    private BatchContextFactory batchContextFactory;

    private BatchletFactory batchletFactory;

    public void start() {
        try {
            ServerConfig serverConfig = CommandLineUtils.getServerConfig();
            if (!serverConfig.isConnected()) return;
            AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(serverConfig.getCpuCoreCount());
            for (BatchletPayLoad batchletPayLoad : appEngineOAuthClient.getAvailableBatchlet()) {
                BatchletThreadWrapper batchletThreadWrapper = new BatchletThreadWrapper(batchletFactory, batchContextFactory, (Class<? extends Batchlet>) Class.forName(batchletPayLoad.getBatchletClass()));
                scheduler.scheduleAtFixedRate(batchletThreadWrapper, 0, batchletPayLoad.getFrequency(), batchletPayLoad.getFrequencyTimeUnit());
            }
        } catch (Throwable t) {
            logger.error("An error has occured while starting the batch kernel", t);
        }

    }

    public BatchContextFactory getBatchContextFactory() {
        return batchContextFactory;
    }

    public void setBatchContextFactory(BatchContextFactory batchContextFactory) {
        this.batchContextFactory = batchContextFactory;
    }

    public BatchletFactory getBatchletFactory() {
        return batchletFactory;
    }

    public void setBatchletFactory(BatchletFactory batchletFactory) {
        this.batchletFactory = batchletFactory;
    }
}
