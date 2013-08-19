package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 19/08/13
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class LogStatistics {

    @Id
    private String logKey;

    private int pointCount;

    private double distance;

    private LocalDateTime start;

    private LocalDateTime end;

    public String getLogKey() {
        return logKey;
    }

    public void setLogKey(String logKey) {
        this.logKey = logKey;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogStatistics that = (LogStatistics) o;

        if (Double.compare(that.distance, distance) != 0) return false;
        if (pointCount != that.pointCount) return false;
        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        if (logKey != null ? !logKey.equals(that.logKey) : that.logKey != null) return false;
        if (start != null ? !start.equals(that.start) : that.start != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = logKey != null ? logKey.hashCode() : 0;
        result = 31 * result + pointCount;
        temp = Double.doubleToLongBits(distance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }
}
