package fr.untitled2.common.utils.introscpection;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 04/01/14
 * Time: 19:47
 * To change this template use File | Settings | File Templates.
 */
public class ClassDescriptor {

    private Class<?> type;

    private Constructor noArgConstructor;

    private Map<String, FieldDescriptor> fields = Maps.newHashMap();

    private List<Annotation> annontations = Lists.newArrayList();

    public Constructor getNoArgConstructor() {
        return noArgConstructor;
    }

    public void setNoArgConstructor(Constructor noArgConstructor) {
        this.noArgConstructor = noArgConstructor;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Map<String, FieldDescriptor> getFields() {
        return fields;
    }

    public void setFields(Map<String, FieldDescriptor> fields) {
        this.fields = fields;
    }

    public List<Annotation> getAnnontations() {
        return annontations;
    }

    public void setAnnontations(List<Annotation> annontations) {
        this.annontations = annontations;
    }
}
