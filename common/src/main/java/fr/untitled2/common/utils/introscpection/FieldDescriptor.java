package fr.untitled2.common.utils.introscpection;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Optional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 04/01/14
 * Time: 19:45
 * To change this template use File | Settings | File Templates.
 */
public class FieldDescriptor {

    private Class<?> type;

    private String name;

    private Optional<ClassDescriptor> subType = Optional.absent();

    private List<Annotation> annotations = Lists.newArrayList();

    private boolean primitive = false;

    private Optional<Constructor> contructorFromString = Optional.absent();

    private Method getter;

    private Method setter;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<ClassDescriptor> getSubType() {
        return subType;
    }

    public void setSubType(Optional<ClassDescriptor> subType) {
        this.subType = subType;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }

    public Optional<Constructor> getContructorFromString() {
        return contructorFromString;
    }

    public void setContructorFromString(Optional<Constructor> contructorFromString) {
        this.contructorFromString = contructorFromString;
    }
}
