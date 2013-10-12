package fr.untitled2.common.entities;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import fr.untitled2.common.entities.jaxb.LocalDateTimeAdapter;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.*;
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
public class LogRecording implements Serializable {

    public static final Ordering<LogRecord> DATE_ORDERING = Ordering.natural().onResultOf(new Function<LogRecord, LocalDateTime>() {
        @Override
        public LocalDateTime apply(LogRecord logRecord) {
            return logRecord.getDateTime();
        }
    });

    @XmlTransient
    private long id;

    @XmlElement
    private String name;

    @XmlElement
    private String dateTimeZone;

    @XmlTransient
    private FileInputStream recordingsFileInputStream;

    @XmlTransient
    private LogRecord lastLogRecord;

    @XmlTransient
    private Integer pointCount;

    @XmlTransient
    private Double distance;

    @XmlElement(name = "record") @XmlElementWrapper(name = "records")
    private List<LogRecord> records = Lists.newArrayList();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateTimeZone() {
        return dateTimeZone;
    }

    public void setDateTimeZone(String dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }

    public void setRecordingsFileInputStream(FileInputStream recordingsFileInputStream) {
        this.recordingsFileInputStream = recordingsFileInputStream;
    }

    public List<LogRecord> getRecords() {
        if (CollectionUtils.isEmpty(records) && recordingsFileInputStream != null) {
            LineIterator lineIterator = new LineIterator(new InputStreamReader(recordingsFileInputStream));
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();

                LogRecord logRecord = LogRecord.fromLine(line);
                records.add(logRecord);
            }

            try {
                recordingsFileInputStream.close();
            } catch (IOException e) {
            }
        }
        return records;
    }

    public void setRecords(List<LogRecord> records) {
        this.records = records;
    }

    public LocalDateTime getStartPointDate() {
        List<LogRecord> allRecords = getRecords();
        if (allRecords.size() == 0) return null;
        allRecords = DATE_ORDERING.sortedCopy(allRecords);
        return allRecords.get(0).getDateTime();
    }

    public LocalDateTime getEndPointDate() {
        if (records.size() == 0) return null;
        records = DATE_ORDERING.reverse().sortedCopy(records);
        return records.get(0).getDateTime();
    }

    public String getUniqueId(String userId) {
        return SignUtils.calculateSha1Digest(userId + name + getStartPointDate() + getEndPointDate());
    }

    public LogRecord getLastLogRecord() {
        if (lastLogRecord == null) {
            List<LogRecord> logRecords = getRecords();

            if (logRecords.size() == 0) return null;

            logRecords = DATE_ORDERING.reverse().sortedCopy(logRecords);
            return logRecords.get(0);
        }
        return lastLogRecord;
    }

    public void setLastLogRecord(LogRecord lastLogRecord) {
        this.lastLogRecord = lastLogRecord;
    }

    public Integer getPointCount() {
        return pointCount;
    }

    public void setPointCount(Integer pointCount) {
        this.pointCount = pointCount;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LogRecord implements Serializable {

        @XmlTransient
        private long logRecordingId;

        @XmlElement
        private LocalDateTime dateTime;

        @XmlElement
        private double latitude;

        @XmlElement
        private double longitude;

        public LogRecord() {
        }

        public long getLogRecordingId() {
            return logRecordingId;
        }

        public void setLogRecordingId(long logRecordingId) {
            this.logRecordingId = logRecordingId;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String toLineString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(dateTime.toString()).append("|").append(latitude).append("|").append(longitude).append("\n");
            return stringBuilder.toString();
        }

        public static LogRecord fromLine(String line) {
            String[] elements = StringUtils.split(line, "|");
            if (elements.length != 3) return null;
            LogRecord result = new LogRecord();
            result.setDateTime(new LocalDateTime(elements[0]));
            result.setLatitude(Double.parseDouble(elements[1]));
            result.setLongitude(Double.parseDouble(elements[2]));

            return result;
        }

    }

}
