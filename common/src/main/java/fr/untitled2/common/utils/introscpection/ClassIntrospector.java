package fr.untitled2.common.utils.introscpection;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 04/01/14
 * Time: 19:49
 * To change this template use File | Settings | File Templates.
 */
public class ClassIntrospector {

    public static ClassDescriptor getClassDescriptor(Class<?> aClass) throws Exception {
        ClassDescriptor descriptor = new ClassDescriptor();

        try {
            descriptor.setNoArgConstructor(aClass.getConstructor(null));
        } catch (NoSuchMethodException e) {
            throw new Exception("No constructor with no args (" + aClass.getName() + ")", e);
        }

        descriptor.setType(aClass);
        descriptor.getAnnontations().addAll(Lists.newArrayList(aClass.getAnnotations()));

        for (Field field : aClass.getDeclaredFields()) {
            FieldDescriptor fieldDescriptor = new FieldDescriptor();
            fieldDescriptor.setType(field.getType());
            fieldDescriptor.getAnnotations().addAll(Lists.newArrayList(field.getAnnotations()));
            fieldDescriptor.setName(field.getName());
            for (Class<?> parentInterface : field.getType().getInterfaces()) {
                if (Collection.class.equals(parentInterface)) {
                    fieldDescriptor.setType(Collection.class);

                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    if (!parameterizedType.getActualTypeArguments()[0].getClass().equals(aClass)) fieldDescriptor.setSubType(Optional.of(getClassDescriptor((Class<?>) parameterizedType.getActualTypeArguments()[0])));
                    else fieldDescriptor.setSubType(Optional.of(descriptor));
                }
            }
            if (field.getType().equals(Collection.class)) {

                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                if (!parameterizedType.getActualTypeArguments()[0].getClass().equals(aClass)) fieldDescriptor.setSubType(Optional.of(getClassDescriptor((Class<?>) parameterizedType.getActualTypeArguments()[0])));
                else fieldDescriptor.setSubType(Optional.of(descriptor));
            }

            fieldDescriptor.setPrimitive(field.getType().isPrimitive());

            try {
                Constructor constructor = field.getType().getConstructor(String.class);
                if (constructor != null) {
                    fieldDescriptor.setContructorFromString(Optional.of(constructor));
                }
            } catch (NoSuchMethodException e) {
            }
            String methodSuffix = getMethodSuffixName(field.getName());
            try {
                fieldDescriptor.setSetter(aClass.getMethod("set" + methodSuffix, field.getType()));
            } catch (NoSuchMethodException e) {
            }

            try {
                fieldDescriptor.setGetter(aClass.getMethod("get" + methodSuffix, null));
            } catch (NoSuchMethodException e) {
            }

            descriptor.getFields().put(field.getName(), fieldDescriptor);
        }
        return descriptor;
    }

    private static String getMethodSuffixName(String fieldName) {
        if (StringUtils.isEmpty(fieldName)) return null;
        if (fieldName.length() == 1) return fieldName.toUpperCase();
        return new StringBuilder(StringUtils.substring(fieldName, 0, 1).toUpperCase()).append(StringUtils.substring(fieldName, 1)).toString();
    }

}
