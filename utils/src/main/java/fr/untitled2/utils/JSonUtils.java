package fr.untitled2.utils;

import com.google.common.collect.Maps;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/28/13
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSonUtils {

    public static <T> String writeJson(T object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeJson(object, bos);
        return new String(bos.toByteArray());
    }

    public static <T> void writeJson(T object, OutputStream outputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().setAnnotationIntrospector(introspector);


        mapper.writeValue( outputStream, object);
    }

    public static <T> T readJson(Class<T> objectClass, InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().setAnnotationIntrospector(introspector);

        return mapper.readValue(inputStream, objectClass);
    }

    public static <T> T readJson(Class<T> objectClass, String json) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8")));
        return readJson(objectClass, bis);
    }

}
