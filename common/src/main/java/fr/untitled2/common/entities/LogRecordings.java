package fr.untitled2.common.entities;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 6/27/13
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class LogRecordings {

    @XmlElement(name = "logRecording") @XmlElementWrapper(name = "logRecordings")
    private Collection<LogRecording> logRecordings = Lists.newArrayList();

    public Collection<LogRecording> getLogRecordings() {
        return logRecordings;
    }

    public void setLogRecordings(Collection<LogRecording> logRecordings) {
        this.logRecordings = logRecordings;
    }
}
