package fr.untitled2.raspi.api;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/7/13
 * Time: 11:54 PM
 * To change this template use File | Settings | File Templates.
 */
public enum LogLevel {

    TRACE,
    DEBUG,
    INFO,
    ERROR;

    public boolean isEnabled(LogLevel setupLevel, LogLevel currentLogLevel) {
        if (setupLevel == TRACE) return true;
        if (setupLevel == DEBUG) {
            switch (currentLogLevel) {
                case DEBUG:
                    return true;
                case INFO:
                    return true;
                case ERROR:
                    return true;
            }
        } else if (setupLevel == INFO) {
            switch (currentLogLevel) {
                case INFO:
                    return true;
                case ERROR:
                    return true;
            }
        } else if (setupLevel == ERROR) {
            switch (currentLogLevel) {
                case ERROR:
                    return true;
            }
        }
        return false;
    }

}
