package fr.untitled2.raspi.main.parameters;

import com.beust.jcommander.Parameter;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterBatchletShellCommand {

    @Parameter(names = "-class-name", required = true, description = "The class signature of the batchlet")
    public String className;

    @Parameter(names = "-frequency", required = true, description = "The execution frequency of the batchlet")
    public Integer frequency;

    @Parameter(names = "-time-unit", required = false, description = "The time unit of the frequency execution, by default seconds")
    public TimeUnit timeUnit = TimeUnit.SECONDS;


}
