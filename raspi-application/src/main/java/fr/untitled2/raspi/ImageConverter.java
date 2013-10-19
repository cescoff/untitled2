package fr.untitled2.raspi;

import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    public File convertIntoJpg(int maxDimPixels) throws IOException {
        if (!inputFile.exists()) throw new IOException("InputFile does not exist");
        if (!outputDir.exists()) throw new IOException("Output dir does not exist");
        if (!outputDir.isDirectory()) throw new IOException("Output dir is not a directory");
        BufferedImage image = ImageIO.read(inputFile);

        int currentWidth = image.getWidth();
        int currentHeight = image.getHeight();

        int newWidth = maxDimPixels;
        int newHeight = (maxDimPixels * currentHeight) / currentWidth;

        if (currentHeight > currentWidth) {
            newHeight = maxDimPixels;
            newWidth = (maxDimPixels * currentWidth) / currentHeight;
        }

        String newFileName = StringUtils.replaceEach(inputFile.getName(), new String[]{".NEF", ".Nef", ".nef", ".jpg", ".JPG", ".jpeg", ".JPEG"}, new String[]{"", "", "", "", "", "", ""}) + "_" + maxDimPixels + ".jpg";

        File outputFile = new File(outputDir, newFileName);

        BufferedImage newDimensionsImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newDimensionsImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, newWidth, newHeight, 0, 0, currentWidth, currentHeight, null);
        g.dispose();

        ImageIO.write(newDimensionsImage, "jpg", outputFile);
        return outputFile;
    }


}
