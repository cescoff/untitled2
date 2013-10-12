package fr.untitled2.raspi.api.impl;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Throwables;
import fr.untitled2.common.entities.raspi.FileRef;
import fr.untitled2.common.entities.raspi.executables.KnownExecutables;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.raspi.api.BatchContext;
import fr.untitled2.raspi.api.LogLevel;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.util.Collection;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/7/13
 * Time: 11:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleBatchContext implements BatchContext {

    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private AppEngineOAuthClient appEngineOAuthClient;

    private File workDir;

    private StringBuilder log = new StringBuilder();

    private LogLevel setupLogLevel;

    public SimpleBatchContext(AppEngineOAuthClient appEngineOAuthClient, File workDir, LogLevel setupLogLevel) {
        this.appEngineOAuthClient = appEngineOAuthClient;
        this.workDir = workDir;
        this.setupLogLevel = setupLogLevel;
    }

    @Override
    public FileRef pushRemoteFile(File aFile) throws IOException {
        try {
            return appEngineOAuthClient.pushFile(aFile);
        } catch (OAuthCommunicationException e) {
            throw new IOException("A communication error has occured", e);
        } catch (OAuthExpectationFailedException e) {
            throw new IOException("A communication error has occured", e);
        } catch (OAuthMessageSignerException e) {
            throw new IOException("A communication error has occured", e);
        }
    }

    @Override
    public File getRemoteFile(FileRef fileRef) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = appEngineOAuthClient.getFile(fileRef);
        } catch (OAuthCommunicationException e) {
            throw new IOException("A communication error has occured", e);
        } catch (OAuthExpectationFailedException e) {
            throw new IOException("A communication error has occured", e);
        } catch (OAuthMessageSignerException e) {
            throw new IOException("A communication error has occured", e);
        }

        if (inputStream != null) {
            String fileName = fileRef.getName();
            int fileSlashIndex = StringUtils.indexOf(fileName, "/");
            if (fileSlashIndex >= 0) {
                fileName = StringUtils.substring(fileName, fileSlashIndex);
            }
            File destinationFile = createFile(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.close();
            return destinationFile;
        }

        return null;
    }

    @Override
    public File createFile(String fileName) throws IOException {
        return new File(workDir, fileName);
    }

    @Override
    public <F, T> T executeRemoteCommand(String commandName, F input, Class<T> outputClass) throws Exception {
        return appEngineOAuthClient.executeCommand(input, outputClass, commandName);
    }

    @Override
    public Collection<KnownExecutables> getInstalledExecutables() {
        if (SystemUtils.IS_OS_LINUX) return Lists.newArrayList(KnownExecutables.values());
        else if (SystemUtils.IS_OS_MAC_OSX) return Lists.newArrayList(KnownExecutables.cat, KnownExecutables.exiftool, KnownExecutables.grep, KnownExecutables.unitex);
        return Collections.EMPTY_LIST;
    }

    @Override
    public CommandLine getExecutableCommandLine(KnownExecutables executable) throws Exception {
        if (SystemUtils.IS_OS_LINUX) {
            switch (executable) {
                case cat:
                    return new CommandLine("/bin/cat");
                case cjpeg:
                    return new CommandLine("/usr/local/bin/cjpeg");
                case exiftool:
                    return new CommandLine("/usr/bin/exiftool");
                case dcraw:
                    return new CommandLine("/usr/bin/dcraw");
                case grep:
                    return new CommandLine("/bin/grep");
                case unitex:
                    return new CommandLine(".myPictureLog/unitex/UnitexTool");
            }
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            switch (executable) {
                case cat:
                    return new CommandLine("/bin/cat");
                case exiftool:
                    return new CommandLine("/usr/bin/exiftool");
                case grep:
                    return new CommandLine("/usr/bin/grep");
                case unitex:
                    return new CommandLine(".myPictureLog/unitex/UnitexTool");
            }
        }
        return null;
    }

    @Override
    public String execute(CommandLine commandLine) throws Exception {
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(result);
        defaultExecutor.setStreamHandler(pumpStreamHandler);
        defaultExecutor.execute(commandLine);
        return new String(result.toByteArray());
    }

    @Override
    public void logTrace(String message) {
        if (setupLogLevel.isEnabled(setupLogLevel, LogLevel.TRACE)) log(LogLevel.TRACE, message, null);
    }

    @Override
    public void logDebug(String message) {
        if (setupLogLevel.isEnabled(setupLogLevel, LogLevel.DEBUG)) log(LogLevel.DEBUG, message, null);
    }

    @Override
    public void logInfo(String message) {
        if (setupLogLevel.isEnabled(setupLogLevel, LogLevel.INFO)) log(LogLevel.INFO, message, null);
    }

    @Override
    public void logError(String message) {
        if (setupLogLevel.isEnabled(setupLogLevel, LogLevel.ERROR)) log(LogLevel.ERROR, message, null);
    }

    @Override
    public void logError(String message, Throwable t) {
        if (setupLogLevel.isEnabled(setupLogLevel, LogLevel.ERROR)) log(LogLevel.ERROR, message, t);
    }

    private void log(LogLevel level, String message, Throwable t) {
        StringBuilder messageBuilder = new StringBuilder("[").append(level).append("] (").append(LOG_DATE_FORMAT.print(LocalDateTime.now())).append(") : ").append(message).append("\n");
        if (t != null) messageBuilder.append(Throwables.getStackTraceAsString(t)).append("\n");
        log.append(messageBuilder);
    }

}
