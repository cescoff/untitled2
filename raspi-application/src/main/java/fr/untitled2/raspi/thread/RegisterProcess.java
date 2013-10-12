package fr.untitled2.raspi.thread;

import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.entities.raspi.ServerRegistrationConfig;
import fr.untitled2.common.entities.raspi.Statistics;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.raspi.utils.CommandLineUtils;
import fr.untitled2.utils.JAXBUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.javatuples.Triplet;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 11:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterProcess implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterProcess.class);

    private static Pattern macOSLoadAVGRegex = Pattern.compile("Load\\sAvg\\:\\s[0-9]*\\.[0-9W]{2}\\,\\s([0-9]*\\.[0-9W]{2})\\,\\s[0-9]*\\.[0-9W]{2}\\s*");
    private static Pattern macOsCPURegex = Pattern.compile("CPU\\susage\\:\\s([0-9]{2,3}\\.[0-9]{2})\\%\\suser\\,\\s[0-9]{2,3}\\.[0-9]{2}\\%\\ssys\\,\\s[0-9]{2,3}\\.[0-9]{2}\\%\\sidle\\s*");
    private static Pattern macOSMemRegex = Pattern.compile("PhysMem\\:\\s[0-9]*M\\swired\\,\\s[0-9]*M\\sactive\\,\\s[0-9]*M\\sinactive\\,\\s([0-9]*)M\\sused\\,\\s([0-9]*)M\\sfree\\.\\s*");

    private static Pattern linuxLoadAVGRegex = Pattern.compile("[0-9]+\\.[0-9]{2}\\s*([0-9]+\\.[0-9]{2})\\s*[0-9]+\\.[0-9]{2}\\s*[0-9]+\\/[0-9]+\\s[0-9]+\\s*");
    private static Pattern linuxCPURegex = Pattern.compile("[0-9]+\\:[0-9]+\\:[0-9]+\\s*all\\s*([0-9]+\\.[0-9]+)\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*");
    private static Pattern linuxMemRegex = Pattern.compile("Mem\\:\\s*[0-9]+\\s+([0-9]+)\\s+([0-9]+)\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s*");

    private AppEngineOAuthClient appEngineOAuthClient;

    private LocalDateTime lastRegisterCallDate;

    private File configurationFile;

    public RegisterProcess(AppEngineOAuthClient appEngineOAuthClient, File configurationFile) {
        this.appEngineOAuthClient = appEngineOAuthClient;
        this.configurationFile = configurationFile;
    }

    @Override
    public void run() {
        logger.info("Starting status process");
        try {
            ServerConfig serverConfig = JAXBUtils.unmarshal(ServerConfig.class, configurationFile);
            if (!serverConfig.isConnected()) {
                register(serverConfig);
            } else {
                logger.info("Server is connected now business is starting");
                Statistics statistics = new Statistics();
                if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_LINUX) {
                    CommandLine commandLine = new CommandLine("/usr/bin/uptime");
                    String upTime = CommandLineUtils.executedCommandLine(commandLine, 1000L);
                    if (upTime != null) upTime = upTime.trim();

                    statistics.setServerId(serverConfig.getServerId());
                    statistics.setUptime(upTime);
                }

                Triplet<Double, Double, Double> usageStats = getUsageStatistics();
                statistics.setLoadAverage(usageStats.getValue0());
                statistics.setCpuPercentage(usageStats.getValue1());
                statistics.setMemoryPercentage(usageStats.getValue2());
                appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());

                appEngineOAuthClient.pushServerStatistics(statistics);
            }

        } catch (Throwable t) {
            logger.error("An error has occured while registering server", t);
        }
    }

    private void register(ServerConfig serverConfig) throws Exception {
        logger.info("Server is not connected");
        if (StringUtils.isNotEmpty(serverConfig.getServerId())) logger.info("Found server id '" + serverConfig.getServerId() + "'");
        else logger.info("No service id found, it will be created soon");

        ServerRegistrationConfig serverRegistrationConfig = new ServerRegistrationConfig();
        serverRegistrationConfig.setUrlGenerationTime(LocalDateTime.now());

        if (lastRegisterCallDate == null || lastRegisterCallDate.plusMinutes(10).isBefore(LocalDateTime.now())) {
            serverRegistrationConfig.setTokenGenerationUrl(appEngineOAuthClient.getTokenValidationUrl());
            lastRegisterCallDate = LocalDateTime.now();
        }
        serverRegistrationConfig.setServerConfig(serverConfig);

        logger.info("Notifying the cloud of the new instance");
        serverRegistrationConfig = appEngineOAuthClient.registerServer(serverRegistrationConfig);

        if (StringUtils.isNotEmpty(serverRegistrationConfig.getServerConfig().getServerId())) {
            if (StringUtils.isEmpty(serverConfig.getServerId())) {
                logger.info("Found new server id '" + serverRegistrationConfig.getServerConfig().getServerId() + "'");
                serverConfig.setServerId(serverRegistrationConfig.getServerConfig().getServerId());
                JAXBUtils.marshal(serverConfig, configurationFile, true);
            } else if (!serverConfig.getServerId().equals(serverRegistrationConfig.getServerConfig().getServerId())) {
                logger.info("Found a renewed server id '" + serverRegistrationConfig.getServerConfig().getServerId() + "'");
                serverConfig.setServerId(serverRegistrationConfig.getServerConfig().getServerId());
                JAXBUtils.marshal(serverConfig, configurationFile, true);
            }
        } else {
            logger.error("No server id return by api");
        }
        if (StringUtils.isNotEmpty(serverRegistrationConfig.getTokenCode())) {
            logger.info("Found oauth verification code");
            appEngineOAuthClient.validateTokens(serverRegistrationConfig.getTokenCode());
            String key = appEngineOAuthClient.getAccessToken();
            String secret = appEngineOAuthClient.getTokenSecret();

            serverConfig.setAccessKey(key);
            serverConfig.setAccessSecret(secret);

            if (serverConfig.isConnected()) {
                JAXBUtils.marshal(serverConfig, configurationFile, true);
            }

            appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());
            appEngineOAuthClient.attachServer(serverConfig);
        }
    }

    private Triplet<Double, Double, Double> getUsageStatistics() throws IOException {
        if (SystemUtils.IS_OS_MAC_OSX) {
            CommandLine commandLine = new CommandLine("/usr/bin/top");
            commandLine.addArgument("-l").addArgument("1");

            String result = null;
            result = CommandLineUtils.executedCommandLine(commandLine, 10 * 60 * 1000);

            LineIterator lineIterator = new LineIterator(new StringReader(result));
            double loadAvg = -1.0;
            double cpuUsage = -1.0;
            double memUsage = -1.0;
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();

                Matcher loadAVGMatcher = macOSLoadAVGRegex.matcher(line);
                Matcher cpuUsageMatcher = macOsCPURegex.matcher(line);
                Matcher memMatcher = macOSMemRegex.matcher(line);

                if (loadAVGMatcher.matches() && loadAvg < 0) {
                    loadAvg = Double.parseDouble(loadAVGMatcher.group(1));
                }

                if (cpuUsageMatcher.matches() && cpuUsage < 0) {
                    cpuUsage = Double.parseDouble(cpuUsageMatcher.group(1));
                }

                if (memMatcher.matches() && memUsage < 0) {
                    double memUsed = Integer.parseInt(memMatcher.group(1));
                    double memFree = Integer.parseInt(memMatcher.group(2));
                    memUsage = (memUsed / (memFree + memUsed)) * 100;
                }

                if (loadAvg > 0 && cpuUsage > 0 && memUsage > 0) break;

            }
            return new Triplet<Double, Double, Double>(loadAvg, cpuUsage, memUsage);
        } else if (SystemUtils.IS_OS_LINUX) {
            double loadAvg = -1.0;
            double cpuUsage = -1.0;
            double memUsage = -1.0;

            CommandLine commandLine = new CommandLine("/bin/cat");
            commandLine.addArgument("/proc/loadavg");
            LineIterator lineIterator = new LineIterator(new StringReader(CommandLineUtils.executedCommandLine(commandLine, 10 * 60 * 1000)));
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();

                Matcher loadAVGMatcher = linuxLoadAVGRegex.matcher(line);

                if (loadAVGMatcher.matches() && loadAvg < 0) {
                    loadAvg = Double.parseDouble(loadAVGMatcher.group(1));
                }

                if (loadAvg > 0) break;

            }

            commandLine = new CommandLine("/usr/bin/mpstat");
            lineIterator = new LineIterator(new StringReader(CommandLineUtils.executedCommandLine(commandLine, 10 * 60 * 1000)));
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();

                Matcher cpuUsageMatcher = linuxCPURegex.matcher(line);
                if (cpuUsageMatcher.matches() && cpuUsage < 0) {
                    cpuUsage = Double.parseDouble(cpuUsageMatcher.group(1));
                }

                if (cpuUsage > 0) break;

            }

            commandLine = new CommandLine("/usr/bin/free");
            lineIterator = new LineIterator(new StringReader(CommandLineUtils.executedCommandLine(commandLine, 10 * 60 * 1000)));
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();

                Matcher memMatcher = linuxMemRegex.matcher(line);

                if (memMatcher.matches() && memUsage < 0) {
                    double memUsed = Integer.parseInt(memMatcher.group(1));
                    double memFree = Integer.parseInt(memMatcher.group(2));
                    memUsage = (memUsed / (memFree + memUsed)) * 100;
                }

                if (memUsage > 0) break;

            }
            return new Triplet<Double, Double, Double>(loadAvg, cpuUsage, memUsage);
        }
        return new Triplet<Double, Double, Double>(-1.0, -1.0, -1.0);
    }

}
