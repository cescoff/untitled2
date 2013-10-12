package fr.untitled2.raspi.main;

import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.entities.raspi.ServerRegistrationConfig;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.raspi.thread.RegisterProcess;
import fr.untitled2.utils.JAXBUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 9:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    private static final String CONFIG_FILE_NAME = "serverConfig.xml";

    private static final String CONFIG_DIR_NAME = ".myPictureLog";

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public static void main(String[] args) {
        File configDir = new File(SystemUtils.getUserHome(), CONFIG_DIR_NAME);
        if (!configDir.exists()) configDir.mkdir();
        File configFile = new File(configDir, CONFIG_FILE_NAME);
        ServerConfig serverConfig = new ServerConfig();
        if (!configFile.exists()) {
            logger.info("No config file found, building a new config file");
            if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_LINUX) {
                CommandLine commandLine = new CommandLine("/bin/hostname");
                DefaultExecutor executor = new DefaultExecutor();
                ByteArrayOutputStream hostNameOutputStream = new ByteArrayOutputStream();
                PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(hostNameOutputStream);

                executor.setStreamHandler(pumpStreamHandler);
                try {
                    executor.execute(commandLine);
                } catch (IOException e) {
                    logger.error("An error has occured while execting command '" + commandLine + "'", e);
                    die();
                }
                String hostName = new String(hostNameOutputStream.toByteArray()).trim();
                serverConfig.setServerName(hostName);

                if (SystemUtils.IS_OS_MAC_OSX) {
                    commandLine = new CommandLine("/usr/sbin/sysctl");
                    commandLine.addArgument("hw.ncpu");
                    ByteArrayOutputStream numberOfCPUsOutputStream = new ByteArrayOutputStream();
                    pumpStreamHandler = new PumpStreamHandler(numberOfCPUsOutputStream);

                    executor.setStreamHandler(pumpStreamHandler);
                    try {
                        executor.execute(commandLine);
                    } catch (IOException e) {
                        logger.error("An error has occured while execting command '" + commandLine + "'", e);
                        die();
                    }
                    try {
                        serverConfig.setCpuCoreCount(Integer.parseInt(StringUtils.remove(new String(numberOfCPUsOutputStream.toByteArray()).trim(), "hw.ncpu: ")));
                    } catch (Throwable t) {
                        logger.error("Unable to find number of CPU core available", t);
                        serverConfig.setCpuCoreCount(1);
                    }
                } else if (SystemUtils.IS_OS_LINUX) {
                    commandLine = new CommandLine("/usr/bin/nproc");
                    ByteArrayOutputStream numberOfCPUsOutputStream = new ByteArrayOutputStream();
                    pumpStreamHandler = new PumpStreamHandler(numberOfCPUsOutputStream);

                    executor.setStreamHandler(pumpStreamHandler);
                    try {
                        executor.execute(commandLine);
                    } catch (IOException e) {
                        logger.error("An error has occured while execting command '" + commandLine + "'", e);
                        die();
                    }
                    try {
                        serverConfig.setCpuCoreCount(Integer.parseInt(new String(numberOfCPUsOutputStream.toByteArray()).trim()));
                    } catch (Throwable t) {
                        logger.error("Unable to find number of CPU core available", t);
                        serverConfig.setCpuCoreCount(1);
                    }
                }
            } else if (SystemUtils.IS_OS_WINDOWS) {
                CommandLine commandLine = new CommandLine("hostname");
                DefaultExecutor executor = new DefaultExecutor();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(bos);

                executor.setStreamHandler(pumpStreamHandler);
                try {
                    executor.execute(commandLine);
                } catch (IOException e) {
                    logger.error("An error has occured while execting command '" + commandLine + "'");
                    System.exit(1);
                }
                String hostName = new String(bos.toByteArray());
                serverConfig.setServerName(hostName);

            }
            logger.info("Config file will be persisted");
            try {
                JAXBUtils.marshal(serverConfig, configFile, true);
            } catch (JAXBException e) {
                logger.error("An error has occured while writing config file", e);
                die();
            } catch (IOException e) {
                logger.error("An error has occured while writing config file", e);
                die();
            }
            logger.info("Config file persisted");
        } else {
            try {
                serverConfig = JAXBUtils.unmarshal(ServerConfig.class, configFile);
            } catch (JAXBException e) {
                logger.error("Unable to deserialize config file", e);
                die();
            } catch (IOException e) {
                logger.error("Unable to deserialize config file", e);
                die();
            }
        }

        RegisterProcess registerProcess = new RegisterProcess(new AppEngineOAuthClient(), configFile);
        scheduler.scheduleAtFixedRate(registerProcess, 0, 30, TimeUnit.SECONDS);

    }

    private static void die() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        System.exit(1);
    }

}
