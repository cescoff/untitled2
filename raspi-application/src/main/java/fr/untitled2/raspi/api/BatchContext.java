package fr.untitled2.raspi.api;

import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.entities.raspi.FileRef;
import fr.untitled2.common.entities.raspi.executables.KnownExecutables;
import org.apache.commons.exec.CommandLine;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/7/13
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BatchContext {

    public FileRef pushRemoteFile(File aFile) throws IOException;

    public File getRemoteFile(FileRef fileRef) throws IOException;

    public <T> String createNewBatchTask(T input, Class<? extends Batchlet> batchletClass) throws Exception;

    public File createFile(String fileName) throws IOException;

    public File createTempDir() throws IOException;

    public <F, T> T executeRemoteCommand(String commandName, F input, Class<T> outputClass) throws Exception;

    public Collection<KnownExecutables> getInstalledExecutables();

    public CommandLine getExecutableCommandLine(KnownExecutables executable) throws Exception;

    public String execute(CommandLine commandLine) throws Exception;

    public void logTrace(String message);

    public void logDebug(String message);

    public void logInfo(String message);

    public void logError(String message);

    public void logError(String message, Throwable t);

    public UserPreferences getUserPreferences() throws Exception;

    public String getLogs();

}
