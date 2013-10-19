package fr.untitled2.raspi.main;

import com.beust.jcommander.JCommander;
import fr.untitled2.common.entities.raspi.BatchletPayLoad;
import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.raspi.api.MasterBatchlet;
import fr.untitled2.raspi.api.SlaveBatchlet;
import fr.untitled2.raspi.main.parameters.RegisterBatchletShellCommand;
import fr.untitled2.raspi.utils.CommandLineUtils;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class RaspiShell {

    public static void main(String[] args) throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        RegisterBatchletShellCommand registerBatchletShellCommand = new RegisterBatchletShellCommand();
        JCommander jCommander = new JCommander(registerBatchletShellCommand);

        try {
            jCommander.parse(args);
        } catch (Throwable t) {
            t.printStackTrace();
            jCommander.usage();
            return;
        }

        Class batchletClass = null;
        try {
            batchletClass = Class.forName(registerBatchletShellCommand.className);
        } catch (ClassNotFoundException e) {
            System.out.println("The class '" + registerBatchletShellCommand.className + "' does not exist");
            return;
        }

        if (!isBatcletClass(batchletClass)) {
            System.out.println("Class '" + batchletClass.getName() + "' is not of type '" + MasterBatchlet.class + "' or '" + SlaveBatchlet.class + "'");
        }

        ServerConfig serverConfig = CommandLineUtils.getServerConfig();

        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());
        BatchletPayLoad batchletPayLoad = new BatchletPayLoad();
        batchletPayLoad.setFrequencyTimeUnit(registerBatchletShellCommand.timeUnit);
        batchletPayLoad.setFrequency(registerBatchletShellCommand.frequency);
        batchletPayLoad.setBatchletClass(batchletClass.getName());
        SimpleResponse simpleResponse = appEngineOAuthClient.executeCommand(batchletPayLoad, SimpleResponse.class, "batchletManager");

        if (simpleResponse.isState()) {
            System.out.println("Batchlet sucessfully registerd");
        }

    }

    private static boolean isBatcletClass(Class aClass) {
        Class parent = aClass.getSuperclass();
        while (!Object.class.equals(parent)){
            if (parent.equals(MasterBatchlet.class) || parent.equals(SlaveBatchlet.class)) return true;
            parent = aClass.getSuperclass();
        }
        return false;
    }

}
