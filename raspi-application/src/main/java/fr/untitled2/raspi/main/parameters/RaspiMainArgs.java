package fr.untitled2.raspi.main.parameters;

import com.beust.jcommander.Parameter;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/23/13
 * Time: 11:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class RaspiMainArgs {

    @Parameter(names = "-inputDir", required = true)
    public File inputDir;

    @Parameter(names = "-outputDir", required = true)
    public File outputDir;



}
