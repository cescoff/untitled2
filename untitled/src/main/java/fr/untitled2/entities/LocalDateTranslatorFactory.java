package fr.untitled2.entities;

import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.Property;
import com.googlecode.objectify.impl.translate.*;
import org.joda.time.LocalDate;
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
public class LocalDateTranslatorFactory extends ValueTranslatorFactory<LocalDate, Date> {

    public LocalDateTranslatorFactory() {
        super(LocalDate.class);
    }

    @Override
    protected ValueTranslator<LocalDate, Date> createSafe(Path path, Property property, Type type, CreateContext createContext) {
        return new ValueTranslator<LocalDate, Date>(path, Date.class) {
            @Override
            protected LocalDate loadValue(Date s, LoadContext loadContext) throws SkipException {
                if (s != null) return new LocalDate(s);
                return null;
            }

            @Override
            protected Date saveValue(LocalDate dateTime, SaveContext saveContext) throws SkipException {
                if (dateTime != null) return dateTime.toDate();
                return null;
            }
        };
    }

}
