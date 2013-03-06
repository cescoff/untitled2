package fr.untitled2.entities;

import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.Property;
import com.googlecode.objectify.impl.translate.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
public class LocalDateTimeTranslatorFactory extends ValueTranslatorFactory<LocalDateTime, Date> {

    public LocalDateTimeTranslatorFactory() {
        super(LocalDateTime.class);
    }

    @Override
    protected ValueTranslator<LocalDateTime, Date> createSafe(Path path, Property property, Type type, CreateContext createContext) {
        return new ValueTranslator<LocalDateTime, Date>(path, Date.class) {
            @Override
            protected LocalDateTime loadValue(Date s, LoadContext loadContext) throws SkipException {
                if (s != null) return new LocalDateTime(s);
                return null;
            }

            @Override
            protected Date saveValue(LocalDateTime dateTime, SaveContext saveContext) throws SkipException {
                if (dateTime != null) return dateTime.toDate();
                return null;
            }
        };
    }

}
