package fr.untitled2.android.sqlilite;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 09/12/13
 * Time: 22:35
 * To change this template use File | Settings | File Templates.
 */
public class SensorReport {

    private long id;

    private LocalDateTime dateTime;

    private Float temperature = null;

    private Float pressure = null;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getPressure() {
        return pressure;
    }

    public void setPressure(Float pressure) {
        this.pressure = pressure;
    }
}
