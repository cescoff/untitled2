package fr.untitled2.raspi.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 11:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandLineUtils {

    public static String executedCommandLine(CommandLine commandLine, long maxDurationMillis) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        executor.execute(commandLine);

        return new String(outputStream.toByteArray());
    }

}
