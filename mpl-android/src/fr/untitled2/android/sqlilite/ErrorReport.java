package fr.untitled2.android.sqlilite;

import com.google.common.base.Throwables;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/12/13
 * Time: 20:59
 * To change this template use File | Settings | File Templates.
 */
public class ErrorReport {

    private Long id;

    private String className;

    private String message;

    private String stackTrace;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public static ErrorReport fromThrowable(Class callerClass, String message, Throwable t) {
        ErrorReport result = new ErrorReport();
        result.setClassName(callerClass.getName());
        result.setMessage(message);
        result.setStackTrace(Throwables.getStackTraceAsString(t));
        return result;
    }

}
