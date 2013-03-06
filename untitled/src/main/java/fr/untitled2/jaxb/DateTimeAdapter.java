package fr.untitled2.jaxb;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/28/13
 * Time: 6:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateTimeAdapter extends XmlAdapter<String, DateTime> {

    @Override
    public DateTime unmarshal(String s) throws Exception {
        return new DateTime(s);
    }

    @Override
    public String marshal(DateTime dateTime) throws Exception {
        return dateTime.toString();
    }
}
