package fr.untitled2.android.service;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.sqlilite.SensorReport;
import fr.untitled2.common.utils.DateTimeUtils;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 09/12/13
 * Time: 22:32
 * To change this template use File | Settings | File Templates.
 */
public class SensorListener implements SensorEventListener {

    private DbHelper dbHelper;

    private SensorType sensorType;

    private float lastTemperature = 0f;

    private float lastPressure = 0f;

    public SensorListener(DbHelper dbHelper, SensorType sensorType) {
        this.dbHelper = dbHelper;
        this.sensorType = sensorType;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values.length == 0) return;

        if (sensorType == SensorType.pressure) {
            if (lastPressure == event.values[0]) return;
            if (event.values[0] > lastPressure * 0.999 && event.values[0] < lastPressure * 1.001) return;
            lastPressure = event.values[0];
        }
        if (sensorType == SensorType.temperature) {
            if (lastTemperature == event.values[0]) return;
            if (event.values[0] > lastTemperature * 0.97 && event.values[0] < lastTemperature * 1.03) return;
            lastTemperature = event.values[0];
        }

        SensorReport sensorReport = new SensorReport();
        sensorReport.setDateTime(DateTimeUtils.getCurrentDateTimeInUTC());
        if (sensorType == SensorType.pressure) sensorReport.setPressure(event.values[0]);
        else sensorReport.setTemperature(event.values[0]);
        dbHelper.addSensorValues(sensorReport);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public enum SensorType {
        temperature,
        pressure
    }

}
