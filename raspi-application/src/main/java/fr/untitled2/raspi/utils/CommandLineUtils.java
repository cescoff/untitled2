package fr.untitled2.raspi.utils;

import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.utils.JAXBUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.SystemUtils;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 11:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandLineUtils {

    private static final String CONFIG_FILE_NAME = "serverConfig.xml";

    private static final String CONFIG_DIR_NAME = ".myPictureLog";

    public static String executedCommandLine(CommandLine commandLine, long maxDurationMillis) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        executor.execute(commandLine);

        return new String(outputStream.toByteArray());
    }

    public static ServerConfig getServerConfig() throws IOException {
        try {
            return JAXBUtils.unmarshal(ServerConfig.class, getConfigFile());
        } catch (Throwable t) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            try {
                return JAXBUtils.unmarshal(ServerConfig.class, getConfigFile());
            } catch (JAXBException e) {
                throw new IOException("Malformated config file");
            }
        }
    }

    public static File getConfigDir() {
        File configDir = new File(SystemUtils.getUserHome(), CONFIG_DIR_NAME);
        if (!configDir.exists()) configDir.mkdir();
        return configDir;
    }

    public static File getConfigFile() {
        return new File(getConfigDir(), CONFIG_FILE_NAME);
    }

    public static File getWorkDir() {
        File result = new File(getConfigDir(), "work");
        if (!result.exists()) result.mkdirs();
        return result;
    }

}
