package fr.untitled2.raspi;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Ordering;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.javatuples.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/23/13
 * Time: 11:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageConverter {

    private File inputFile;

    private File outputDir;

    public ImageConverter(File inputFile, File outputDir) {
        this.inputFile = inputFile;
        this.outputDir = outputDir;
    }

    public Collection<Pair<Integer, File>> convertIntoJpg(Iterable<Integer> maxDimPixelsForEachGeneration) throws IOException {
        if (!inputFile.exists()) throw new IOException("InputFile does not exist");
        if (!outputDir.exists()) throw new IOException("Output dir does not exist");
        if (!outputDir.isDirectory()) throw new IOException("Output dir is not a directory");

        File conversionScript = new File(SystemUtils.USER_HOME + "/.myPictureLog/bin/convert_nef.sh");

        maxDimPixelsForEachGeneration = Ordering.natural().reverse().sortedCopy(maxDimPixelsForEachGeneration);

        Collection<Pair<Integer, File>> result = Lists.newArrayList();
        if (!conversionScript.exists()) {
            File previousDimensionFile = inputFile;
            for (Integer maxDimPixels : maxDimPixelsForEachGeneration) {
                File outputFile = new File(outputDir, FilenameUtils.getBaseName(inputFile.getName()) + "_" + maxDimPixels + ".jpg");
                resize(previousDimensionFile, outputFile, maxDimPixels);
                previousDimensionFile = outputFile;
                result.add(Pair.with(maxDimPixels, outputFile));
            }
        } else {
            File previousDimensionFile =  new File(outputDir, FilenameUtils.getBaseName(inputFile.getName()) + ".jpg");
            if (FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("nef")) {
                CommandLine commandLine = new CommandLine(conversionScript);
                commandLine.addArgument(inputFile.getPath());
                commandLine.addArgument(previousDimensionFile.getPath());
                DefaultExecutor defaultExecutor = new DefaultExecutor();
                defaultExecutor.execute(commandLine);
            } else previousDimensionFile = inputFile;

            for (Integer maxDimPixels : maxDimPixelsForEachGeneration) {
                File outputFile = new File(outputDir, FilenameUtils.getBaseName(inputFile.getName()) + "_" + maxDimPixels + ".jpg");
                resize(previousDimensionFile, outputFile, maxDimPixels);
                previousDimensionFile = outputFile;
                result.add(Pair.with(maxDimPixels, outputFile));
            }

        }
        return result;
    }

    private void resize(File inputImageFile, File outputFile, int maxDimPixels) throws IOException {
        BufferedImage image = ImageIO.read(inputImageFile);

        int currentWidth = image.getWidth();
        int currentHeight = image.getHeight();

        int newWidth = maxDimPixels;
        int newHeight = (maxDimPixels * currentHeight) / currentWidth;

        if (currentHeight > currentWidth) {
            newHeight = maxDimPixels;
            newWidth = (maxDimPixels * currentWidth) / currentHeight;
        }

        BufferedImage newDimensionsImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newDimensionsImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, newWidth, newHeight, 0, 0, currentWidth, currentHeight, null);
        g.dispose();

        ImageIO.write(newDimensionsImage, "jpg", outputFile);
    }

}
