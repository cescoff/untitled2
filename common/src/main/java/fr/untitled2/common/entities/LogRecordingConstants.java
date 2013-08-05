package fr.untitled2.common.entities;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import fr.untitled2.common.entities.jaxb.LocalDateTimeAdapter;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/9/13
 * Time: 2:48 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "logRecording")
public class LogRecordingConstants implements Serializable {

    public static final Ordering<LogRecording.LogRecord> log_record_sorter = Ordering.natural().onResultOf(new Function<LogRecording.LogRecord, LocalDateTime>() {
        @Override
        public LocalDateTime apply(LogRecording.LogRecord logRecord) {
            return logRecord.getDateTime();
        }
    });


}
