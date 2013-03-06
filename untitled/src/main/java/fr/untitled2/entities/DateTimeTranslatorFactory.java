package fr.untitled2.entities;

import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.Property;
import com.googlecode.objectify.impl.translate.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/29/13
 * Time: 2:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class DateTimeTranslatorFactory extends ValueTranslatorFactory<DateTime, Date> {

    public DateTimeTranslatorFactory() {
        super(DateTime.class);
    }

    @Override
    protected ValueTranslator<DateTime, Date> createSafe(Path path, Property property, Type type, CreateContext createContext) {
        return new ValueTranslator<DateTime, Date>(path, Date.class) {
            @Override
            protected DateTime loadValue(Date s, LoadContext loadContext) throws SkipException {
                if (s != null) return new DateTime(s);
                return null;
            }

            @Override
            protected Date saveValue(DateTime dateTime, SaveContext saveContext) throws SkipException {
                if (dateTime != null) return dateTime.toDate();
                return null;
            }
        };
    }

}
