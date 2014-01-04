package fr.untitled2.common.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 08/12/13
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public class DateTimeUtils {

    public static LocalDateTime getCurrentDateTimeInUTC() {
        return getDateTimeInUTC(DateTime.now());
    }

    public static LocalDateTime getDateTimeInUTC(DateTime dateTime) {
        return dateTime.toDateTime(DateTimeZone.UTC).toLocalDateTime();
    }

    public static DateTime getDateTimeInTimeZone(LocalDateTime localDateTime, String dateTimeZone) {
        return localDateTime.toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID(dateTimeZone));
    }

    public static DateTime getDateTimeInTimeZone(LocalDateTime localDateTime, DateTimeZone dateTimeZone) {
        return localDateTime.toDateTime(DateTimeZone.UTC).toDateTime(dateTimeZone);
    }

}
