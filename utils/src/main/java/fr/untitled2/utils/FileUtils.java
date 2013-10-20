package fr.untitled2.utils;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/12/13
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {

    public static List<File> splitFile(File sourceFile, File tempDir, long maxFileSize) throws IOException {
        List<File> result = Lists.newArrayList();
        long fileSize = org.apache.commons.io.FileUtils.sizeOf(sourceFile);
        if (fileSize < maxFileSize) return Lists.newArrayList(sourceFile);

        int fileCount = new Double(fileSize / maxFileSize).intValue();
        if (fileSize % maxFileSize > 0) {
            fileCount++;
        }
        byte[] buffer = new byte[10 * 1024];
        FileInputStream fileInputStream = new FileInputStream(sourceFile);
        int readLength = fileInputStream.read(buffer);
        if (readLength > 0) {
            for (int index = 0; index < fileCount; index++) {
                File splitedFile = new File(tempDir, sourceFile.getName() + "." + index);
                FileOutputStream splitFileOutputStream = new FileOutputStream(splitedFile);
                long totalReadLength = readLength;
                while (readLength > 0 && totalReadLength <= maxFileSize) {
                    splitFileOutputStream.write(buffer, 0, readLength);

                    readLength = fileInputStream.read(buffer);
                    totalReadLength+=readLength;
                }
                splitFileOutputStream.close();
                result.add(splitedFile);
            }
        }
        return result;
    }

    public static void rebuildSplitedFile(List<File> splits, File targetFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(targetFile);

        for (File split : splits) {
            FileInputStream fileInputStream = new FileInputStream(split);
            byte[] buffer = new byte[10 * 1024];
            int readLength = fileInputStream.read(buffer);
            while (readLength > 0) {
                fileOutputStream.write(buffer, 0, readLength);
                readLength = fileInputStream.read(buffer);
            }
            fileInputStream.close();
            org.apache.commons.io.FileUtils.deleteQuietly(split);
        }
        fileOutputStream.close();

    }

}
