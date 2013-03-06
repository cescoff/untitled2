package fr.untitled2.mvc.multipart;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 7:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class BlobStoreMultipartResolver implements MultipartResolver, ServletContextAware {

    private static Logger logger = LoggerFactory.getLogger(BlobStoreMultipartResolver.class);

    private boolean resolveLazily = false;

    private FileUpload fileUpload = new ServletFileUpload(new BlobStoreFileItemFactory());

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        return (request != null && ServletFileUpload.isMultipartContent(request));
    }

    @Override
    public MultipartHttpServletRequest resolveMultipart(final HttpServletRequest request) throws MultipartException {
        Assert.notNull(request, "Request must not be null");
        if (this.resolveLazily) {
            return new DefaultMultipartHttpServletRequest(request) {
                @Override
                protected void initializeMultipart() {
                    MultipartParsingResult parsingResult = parseRequest(request);
                    setMultipartFiles(parsingResult.getMultipartFiles());
                    setMultipartParameters(parsingResult.getMultipartParameters());
                    setMultipartParameterContentTypes(parsingResult.getMultipartParameterContentTypes());
                }
            };
        }
        else {
            MultipartParsingResult parsingResult = parseRequest(request);
            return new DefaultMultipartHttpServletRequest(request, parsingResult.getMultipartFiles(),
                    parsingResult.getMultipartParameters(), parsingResult.getMultipartParameterContentTypes());
        }
    }

    protected MultipartParsingResult parseRequest(HttpServletRequest request) throws MultipartException {
        String encoding = determineEncoding(request);
        FileUpload fileUpload = prepareFileUpload(encoding);
        try {
            List<FileItem> fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
            return parseFileItems(fileItems, encoding);
        }
        catch (FileUploadBase.SizeLimitExceededException ex) {
            throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
        }
        catch (FileUploadException ex) {
            throw new MultipartException("Could not parse multipart servlet request", ex);
        }
    }

    protected MultipartParsingResult parseFileItems(List<FileItem> fileItems, String encoding) {
        MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<String, MultipartFile>();
        Map<String, String[]> multipartParameters = new HashMap<String, String[]>();
        Map<String, String> multipartParameterContentTypes = new HashMap<String, String>();

        // Extract multipart files and multipart parameters.
        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField()) {
                String value;
                String partEncoding = determineEncoding(fileItem.getContentType(), encoding);
                if (partEncoding != null) {
                    try {
                        value = fileItem.getString(partEncoding);
                    }
                    catch (UnsupportedEncodingException ex) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Could not decode multipart item '" + fileItem.getFieldName() +
                                    "' with encoding '" + partEncoding + "': using platform default");
                        }
                        value = fileItem.getString();
                    }
                }
                else {
                    value = fileItem.getString();
                }
                String[] curParam = multipartParameters.get(fileItem.getFieldName());
                if (curParam == null) {
                    // simple form field
                    multipartParameters.put(fileItem.getFieldName(), new String[] {value});
                }
                else {
                    // array of simple form fields
                    String[] newParam = StringUtils.addStringToArray(curParam, value);
                    multipartParameters.put(fileItem.getFieldName(), newParam);
                }
                multipartParameterContentTypes.put(fileItem.getFieldName(), fileItem.getContentType());
            }
            else {
                // multipart file field
                CommonsMultipartFile file = new CommonsMultipartFile(fileItem);
                multipartFiles.add(file.getName(), file);
                if (logger.isDebugEnabled()) {
                    logger.debug("Found multipart file [" + file.getName() + "] of size " + file.getSize() +
                            " bytes with original filename [" + file.getOriginalFilename() + "], stored " +
                            file.getStorageDescription());
                }
            }
        }
        return new MultipartParsingResult(multipartFiles, multipartParameters, multipartParameterContentTypes);
    }

    protected FileUpload prepareFileUpload(String encoding) {
        return fileUpload;
    }



    protected String determineEncoding(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = getDefaultEncoding();
        }
        return encoding;
    }

    private String determineEncoding(String contentTypeHeader, String defaultEncoding) {
        if (!StringUtils.hasText(contentTypeHeader)) {
            return defaultEncoding;
        }
        MediaType contentType = MediaType.parseMediaType(contentTypeHeader);
        Charset charset = contentType.getCharSet();
        return (charset != null ? charset.name() : defaultEncoding);
    }

    protected String getDefaultEncoding() {
        String encoding = this.fileUpload.getHeaderEncoding();
        if (encoding == null) {
            encoding = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        return encoding;
    }

    @Override
    public void cleanupMultipart(MultipartHttpServletRequest request) {
        if (request != null) {
            try {
                cleanupFileItems(request.getMultiFileMap());
            }
            catch (Throwable ex) {
                logger.warn("Failed to perform multipart cleanup for servlet request", ex);
            }
        }
    }

    protected void cleanupFileItems(MultiValueMap<String, MultipartFile> multipartFiles) {
        for (List<MultipartFile> files : multipartFiles.values()) {
            for (MultipartFile file : files) {
                if (file instanceof CommonsMultipartFile) {
                    CommonsMultipartFile cmf = (CommonsMultipartFile) file;
                    cmf.getFileItem().delete();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cleaning up multipart file [" + cmf.getName() + "] with original filename [" +
                                cmf.getOriginalFilename() + "], stored " + cmf.getStorageDescription());
                    }
                }
            }
        }
    }

    public void setMaxUploadSize(long maxUploadSize) {
        this.fileUpload.setSizeMax(maxUploadSize);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
    }


    protected static class MultipartParsingResult {

        private final MultiValueMap<String, MultipartFile> multipartFiles;

        private final Map<String, String[]> multipartParameters;

        private final Map<String, String> multipartParameterContentTypes;

        public MultipartParsingResult(MultiValueMap<String, MultipartFile> mpFiles,
                                      Map<String, String[]> mpParams, Map<String, String> mpParamContentTypes) {
            this.multipartFiles = mpFiles;
            this.multipartParameters = mpParams;
            this.multipartParameterContentTypes = mpParamContentTypes;
        }

        public MultiValueMap<String, MultipartFile> getMultipartFiles() {
            return this.multipartFiles;
        }

        public Map<String, String[]> getMultipartParameters() {
            return this.multipartParameters;
        }

        public Map<String, String> getMultipartParameterContentTypes() {
            return this.multipartParameterContentTypes;
        }
    }
}
