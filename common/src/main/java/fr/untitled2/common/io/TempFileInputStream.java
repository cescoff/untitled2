package fr.untitled2.common.io;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/12/13
 * Time: 11:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class TempFileInputStream extends InputStream {

    private File tempFile;

    private InputStream inputStream;

    public TempFileInputStream(File tempFile) {
        this.tempFile = tempFile;
    }

    @Override
    public int read() throws IOException {
        if (inputStream == null) inputStream = new FileInputStream(tempFile);
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        super.close();
        inputStream.close();
        FileUtils.deleteQuietly(tempFile);
    }
}
