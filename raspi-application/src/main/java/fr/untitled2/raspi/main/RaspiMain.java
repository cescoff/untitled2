package fr.untitled2.raspi.main;

import com.beust.jcommander.JCommander;
import fr.untitled2.raspi.ImageConverter;
import fr.untitled2.raspi.main.parameters.RaspiMainArgs;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/23/13
 * Time: 11:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class RaspiMain {

    public static void main(String[] args) {
        RaspiMainArgs programmArgs = new RaspiMainArgs();
        JCommander jCommander = new JCommander(programmArgs);

        try {
            jCommander.parse(args);
        } catch (Throwable t) {
            jCommander.usage();
            return;
        }

        Collection<File> sourceFiles = FileUtils.listFiles(programmArgs.inputDir, new String[] {"jpg", "jpeg", "JPG", "NEF", "nef"}, true);

        for (File sourceFile : sourceFiles) {
            ImageConverter imageConverter = new ImageConverter(sourceFile, programmArgs.outputDir);
            try {
                long timer = System.currentTimeMillis();
                System.out.println("Handling file '" + sourceFile + "'");
                imageConverter.convertIntoJpg(1920);
                System.out.println("File '" + sourceFile + "' handled in " + (System.currentTimeMillis() - timer) + "ms");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

}
