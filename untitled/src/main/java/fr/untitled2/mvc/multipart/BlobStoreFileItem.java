package fr.untitled2.mvc.multipart;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.*;
import com.google.common.io.CountingOutputStream;
import fr.untitled2.entities.User;
import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 6:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class BlobStoreFileItem implements FileItem {

    private static Logger logger = LoggerFactory.getLogger(BlobStoreFileItem.class);

    private AppEngineFile appEngineFile;

    private String contentType;

    private String name;

    private String fieldName;

    private boolean formField;

    private CountingOutputStream countingOutputStream;

    private OutputStream outputStream;

    private FileWriteChannel fileWriteChannel;

    private ByteArrayOutputStream memoryCopy = new ByteArrayOutputStream();

    private FileService fileService;

    public BlobStoreFileItem(String contentType, String name, String fieldName, boolean formField) {
        this.contentType = contentType;
        this.name = name;
        this.fieldName = fieldName;
        this.formField = formField;
    }

    protected void init() throws IOException {
        this.fileService = FileServiceFactory.getFileService();
        String type = "application/octet-stream";
        if (name != null && (name.endsWith("jpeg") || name.endsWith("jpg"))) type = "image/jpg";
        this.appEngineFile = FileServiceFactory.getFileService().createNewBlobFile(type, name);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        FileReadChannel fileReadChannel = fileService.openReadChannel(appEngineFile, false);
        return Channels.newInputStream(fileReadChannel);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        closeOutputStreams();
        return this.name;
    }

    public BlobKey getBlobKey() {
        closeOutputStreams();
        return fileService.getBlobKey(appEngineFile);
    }

    private void closeOutputStreams() {
        try {
            outputStream.close();
            this.fileWriteChannel.closeFinally();
        } catch (IOException e) {
            logger.info("Impossible de fermer l'outputstrem", e);
            try {
                Thread.sleep(10000);
                outputStream.close();
                fileWriteChannel.closeFinally();
            } catch (Throwable t) {
                logger.error("Impossible de clore correctement l'outputstream", t);
            }

        }
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public long getSize() {
        if (countingOutputStream == null) return 0;
        return countingOutputStream.getCount();
    }

    @Override
    public byte[] get() {
        return memoryCopy.toByteArray();
    }

    @Override
    public String getString(String encoding) throws UnsupportedEncodingException {
        return new String(get(), Charset.forName(encoding));
    }

    @Override
    public String getString() {
        return new String(get());
    }

    @Override
    public void write(File file) throws Exception {
        throw new IllegalArgumentException("File is not supported in google app engine");
    }

    @Override
    public void delete() {
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String name) {
        this.fieldName = name;
    }

    @Override
    public boolean isFormField() {
        return formField;
    }

    @Override
    public void setFormField(boolean state) {
        this.formField = state;
    }

    public AppEngineFile getAppEngineFile() {
        return appEngineFile;
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return  (User)authentication.getPrincipal();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        this.fileWriteChannel = fileService.openWriteChannel(appEngineFile, true);
        this.outputStream = Channels.newOutputStream(fileWriteChannel);
        this.countingOutputStream = new CountingOutputStream(outputStream);

        return new CopyOutputStream(countingOutputStream, memoryCopy);
    }

    private class CopyOutputStream extends OutputStream {

        private OutputStream outputStream1;
        private OutputStream outputStream2;

        private CopyOutputStream(OutputStream outputStream1, OutputStream outputStream2) {
            this.outputStream1 = outputStream1;
            this.outputStream2 = outputStream2;
        }

        @Override
        public void write(int i) throws IOException {
            outputStream1.write(i);
            outputStream2.write(i);
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            outputStream1.write(bytes);
            outputStream2.write(bytes);
        }

        @Override
        public void write(byte[] bytes, int i, int i2) throws IOException {
            outputStream1.write(bytes, i, i2);
            outputStream2.write(bytes, i, i2);
        }

        @Override
        public void close() throws IOException {
            outputStream1.close();
            outputStream2.close();
        }
    }

}
