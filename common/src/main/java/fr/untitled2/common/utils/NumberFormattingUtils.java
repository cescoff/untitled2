package fr.untitled2.common.utils;

import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 08/12/13
 * Time: 17:59
 * To change this template use File | Settings | File Templates.
 */
public class NumberFormattingUtils {

    private static final DecimalFormat kilometerFormat = new DecimalFormat("0.###");

    private static final DecimalFormat speedFormat = new DecimalFormat("0.#");

    private static final DecimalFormat meterFormat = new DecimalFormat("0");

    private static double meter_2_feet_multiplier = 3.2808;

    public enum DistanceUnit {
        metric,
        feet
    }

    public enum PositionType {
        latitude,
        longitude
    }

    public static String toSpeed(double speed, DistanceUnit distanceUnit) {
        if (distanceUnit == DistanceUnit.metric) {
            speed = 3.6 * speed;
            return speedFormat.format(speed) + "km/h";
        } else if (distanceUnit == DistanceUnit.feet) {
            return speedFormat.format(convertDistance(speed, DistanceUnit.feet)) + "ft/s";
        }
        return "unknown";
    }

    public static String toLatitudeInDegreesMinutesSeconds(double value) {
        return toDegreesMinutesSeconds(value, PositionType.latitude);
    }

    public static String toLongitudeInDegreesMinutesSeconds(double value) {
        return toDegreesMinutesSeconds(value, PositionType.longitude);
    }

    private static String toDegreesMinutesSeconds(double value, PositionType positionType) {
        StringBuilder result = new StringBuilder();
        result.append(new Double(Math.abs(value)).intValue()).append("°");
        if (positionType == PositionType.latitude) {
            if (value > 0) result.append("N");
            else result.append("S");
        } else if (positionType == PositionType.longitude) {
            if (value > 0) result.append("E");
            else result.append("W");
        }
        value = Math.abs(value) - new Double(Math.abs(value)).intValue();
        if (value > 0) {
            result.append(" ");
            value = value * 60;
            result.append(new Double(value).intValue()).append("\"");
        }
        value = value - new Double(value).intValue();
        if (value > 0) {
            result.append(" ");
            result.append(new Double(value * 60).intValue()).append("'");
        }
        return result.toString();
    }

    public static String toDistance(double distance, DistanceUnit distanceUnit) {
        if (distanceUnit == DistanceUnit.metric) {
            if (distance <= 1000) {
                return meterFormat.format(distance) + "m";
            } else {
                return kilometerFormat.format(distance / 1000) + "km";
            }
        } else if (distanceUnit == DistanceUnit.feet) {
            return kilometerFormat.format(convertDistance(distance, DistanceUnit.feet)) + "ft";
        }
        return "unknown";
    }

    public static String toDisplayableDouble(double value) {
        return kilometerFormat.format(value);
    }

    private static double convertDistance(double distance, DistanceUnit distanceUnit) {
        return distance * meter_2_feet_multiplier;
    }

}
