package fr.untitled2.utils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 11:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class MemFileItemFactory implements FileItemFactory {

    @Override
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        return new MemFileItem(contentType, fileName, fieldName, isFormField);
    }

    public static class MemFileItem implements FileItem {

        private byte[] content;

        private String contentType;

        private String name;

        private String fieldName;

        private boolean formField;

        private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        public MemFileItem(String contentType, String name, String fieldName, boolean formField) {
            this.contentType = contentType;
            this.name = name;
            this.fieldName = fieldName;
            this.formField = formField;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(get());
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] get() {
            if (content == null) content = outputStream.toByteArray();
            return content;
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
            throw new Exception("Not supported");
        }

        @Override
        public void delete() {
            content = null;
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

        @Override
        public OutputStream getOutputStream() throws IOException {
            return outputStream;
        }
    }

}
