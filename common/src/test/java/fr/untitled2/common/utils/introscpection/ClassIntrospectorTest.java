package fr.untitled2.common.utils.introscpection;

import fr.untitled2.common.entities.Journey;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.utils.CollectionUtils;
import org.junit.Test;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 04/01/14
 * Time: 20:13
 * To change this template use File | Settings | File Templates.
 */
public class ClassIntrospectorTest {
    @Test
    public void testGetClassDescriptor() throws Exception {
        ClassDescriptor descriptor = ClassIntrospector.getClassDescriptor(LogRecording.class);
        System.out.println("Class : '" + descriptor.getType().getName() + "'");
        for (String name : descriptor.getFields().keySet()) {
            System.out.println("\tField : '" + name + "' :");
            System.out.println("\t\tClass : '" + descriptor.getFields().get(name).getType().getName() + "'");

            if (descriptor.getFields().get(name).getGetter() != null) System.out.println("\t\tGetter : '" + descriptor.getFields().get(name).getGetter().getName() + "'");
            if (descriptor.getFields().get(name).getSetter() != null) System.out.println("\t\tGetter : '" + descriptor.getFields().get(name).getSetter().getName() + "'");

            System.out.println("\t\tPrimitive : '" + descriptor.getFields().get(name).isPrimitive() + "'");
            System.out.println("\t\tConstructor from string : '" + descriptor.getFields().get(name).getContructorFromString().isPresent() + "'");
            if (descriptor.getFields().get(name).getSubType().isPresent()) System.out.println("\t\tSub type : '" + descriptor.getFields().get(name).getSubType().get().getType().getName() + "'");

            if (CollectionUtils.isNotEmpty(descriptor.getFields().get(name).getAnnotations())) {
                System.out.println("\t\tAnnotations : ");
                for (Annotation annotation : descriptor.getFields().get(name).getAnnotations()) {
                    System.out.println("\t\t\t- '" + annotation.annotationType() + "'");
                }
            }
        }
    }
}
