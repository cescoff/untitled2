package fr.untitled2.utils;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/12/13
 * Time: 8:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileUtilsTest {
    @Test
    public void testSplitFile() throws Exception {
        File sourceFile = new File("/Users/corentinescoffier/Pictures/2013/2013-09-22/DSC_4680.NEF");
        long maxFileSize = 30 * 1024 * 1024;
        List<File> splitedFiles = FileUtils.splitFile(sourceFile, org.apache.commons.io.FileUtils.getTempDirectory(), maxFileSize);
        for (File splitedFile : splitedFiles) {
            System.out.println(splitedFile.getPath());
        }
        String sourceFilePath = sourceFile.getPath();
        FileUtils.rebuildSplitedFile(splitedFiles, new File(StringUtils.substring(sourceFilePath, 0, StringUtils.lastIndexOf(sourceFilePath, ".")) + "_rebuilt" + StringUtils.substring(sourceFilePath, StringUtils.lastIndexOf(sourceFilePath, "."))));
    }



}
