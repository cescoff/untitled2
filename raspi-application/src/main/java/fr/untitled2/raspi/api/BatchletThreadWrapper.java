package fr.untitled2.raspi.api;

import com.beust.jcommander.internal.Sets;
import fr.untitled2.common.entities.raspi.BatchTaskPayload;
import fr.untitled2.common.entities.raspi.LogLevel;
import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.raspi.utils.CommandLineUtils;
import fr.untitled2.utils.GzipUtils;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.SignUtils;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class BatchletThreadWrapper implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(BatchletThreadWrapper.class);

    private BatchletFactory batchletFactory;

    private BatchContextFactory batchContextFactory;

    private Class<? extends Batchlet> batchletClass;

    private Collection<Class> runningBatchlets = Sets.newHashSet();

    public BatchletThreadWrapper(BatchletFactory batchletFactory, BatchContextFactory batchContextFactory, Class<? extends Batchlet> batchletClass) {
        this.batchletFactory = batchletFactory;
        this.batchContextFactory = batchContextFactory;
        this.batchletClass = batchletClass;
    }

    @Override
    public void run() {
        try {
            Batchlet batchlet = null;
            try {
                batchlet = batchletFactory.getBatchlet(batchletClass);
            } catch (Throwable t) {
                logger.error("Battchlet class cannot be loaded '" + batchletClass + "'", t);
                return;
            }

            if (runningBatchlets.contains(batchletClass) && !batchlet.isThreadSafe()) {
                logger.info("Batchlet '" + batchletClass + "' is not threadsafe and already running");
                return;
            }

            if (batchlet instanceof MasterBatchlet) {
                logger.info("Starting Batchlet '" + batchletClass + "'");
                MasterBatchlet masterBatchlet = (MasterBatchlet) batchlet;
                BatchTaskPayload batchTaskPayload = new BatchTaskPayload();
                batchTaskPayload.setLogLevel(LogLevel.INFO);
                batchTaskPayload.setBatchTaskId(SignUtils.calculateSha1Digest(batchletClass.getName() + System.currentTimeMillis()));

                BatchContext batchContext = null;
                try {
                    batchContext = batchContextFactory.getBatchContext(batchTaskPayload);
                } catch (Exception e) {
                    logger.error("Unable to load batch context '" + batchletClass + "'", e);
                    runningBatchlets.remove(batchletClass);
                    return;
                }

                masterBatchlet.setBatchContext(batchContext);

                try {
                    masterBatchlet.execute();
                } catch (Throwable t) {
                    logger.error("An error has occured while executing batchlet '" + batchletClass + "'", t);
                    runningBatchlets.remove(batchletClass);
                    return;
                }

            } else if (batchlet instanceof SlaveBatchlet) {
                SlaveBatchlet slaveBatchlet = (SlaveBatchlet) batchlet;
                ServerConfig serverConfig = null;
                try {
                    serverConfig = CommandLineUtils.getServerConfig();
                } catch (IOException e) {
                    logger.error("ServerConfig cannot be loaded", e);
                    runningBatchlets.remove(batchletClass);
                    return;
                }

                AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());
                BatchTaskPayload batchTaskPayload = null;
                try {
                    batchTaskPayload = appEngineOAuthClient.executeCommand(serverConfig, BatchTaskPayload.class, "getNextBatchTask");
                } catch (IOException e) {
                    logger.error("Unable to load next task", e);
                    runningBatchlets.remove(batchletClass);
                    return;
                } catch (OAuthCommunicationException e) {
                    logger.error("Unable to load next task", e);
                    runningBatchlets.remove(batchletClass);
                    return;
                } catch (OAuthExpectationFailedException e) {
                    logger.error("Unable to load next task", e);
                    runningBatchlets.remove(batchletClass);
                    return;
                } catch (OAuthMessageSignerException e) {
                    logger.error("Unable to load next task", e);
                    runningBatchlets.remove(batchletClass);
                    return;
                }

                if (StringUtils.isEmpty(batchTaskPayload.getBatchTaskId())) {
                    logger.info("No task to do for batchlet '" + batchletClass + "'");
                    logger.info("Ending Batchlet '" + batchletClass + "'");
                    runningBatchlets.remove(batchletClass);
                    return;
                }
                logger.info("Starting Batchlet '" + batchletClass + "'->" + batchTaskPayload.getBatchTaskId());

                BatchContext batchContext = null;
                try {
                    batchContext = batchContextFactory.getBatchContext(batchTaskPayload);
                } catch (Exception e) {
                    logger.error("Unable to load batch context", e);
                    runningBatchlets.remove(batchletClass);
                    return;
                }

                slaveBatchlet.setBatchContext(batchContext);
                Object result = null;
                try {
                    result = slaveBatchlet.execute(JSonUtils.readJson(slaveBatchlet.getInputType(), GzipUtils.unzipString(batchTaskPayload.getZippedPayload())));
                    batchTaskPayload.setZippedPayload(GzipUtils.zipString(JSonUtils.writeJson(result)));
                    batchTaskPayload.setServerId(serverConfig.getServerId());
                    batchTaskPayload.setLog(GzipUtils.zipString(batchContext.getLogs()));
                    batchTaskPayload.setSuccess(true);
                } catch (Throwable t) {
                    batchTaskPayload.setSuccess(false);
                    batchContext.logError("A fatal error has occured", t);
                    logger.error("Fatal error occured while executing task '" + batchTaskPayload.getBatchTaskId() + "'", t);
                }

                try {
                    batchTaskPayload.setLog(GzipUtils.zipString(batchContext.getLogs()));
                } catch (IOException e) {
                    logger.error("Unable to zip log", e);
                }
                try {
                    appEngineOAuthClient.executeCommand(batchTaskPayload, SimpleResponse.class, "markBatchTaskAsDone");
                } catch (IOException e) {
                    logger.error("An error has occured while marking task '" + batchTaskPayload.getBatchTaskId() + "' as done", e);
                } catch (OAuthCommunicationException e) {
                    logger.error("An error has occured while marking task '" + batchTaskPayload.getBatchTaskId() + "' as done", e);
                } catch (OAuthExpectationFailedException e) {
                    logger.error("An error has occured while marking task '" + batchTaskPayload.getBatchTaskId() + "' as done", e);
                } catch (OAuthMessageSignerException e) {
                    logger.error("An error has occured while marking task '" + batchTaskPayload.getBatchTaskId() + "' as done", e);
                }
                batchContext.destroy();
            }
        } catch (Throwable t) {
            logger.error("An unexpected error has occured while executing Batchlet '"  + batchletClass + "'", t);
        }
        logger.info("Ending Batchlet '" + batchletClass + "'");
        runningBatchlets.remove(batchletClass);
    }
}
