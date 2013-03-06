package fr.untitled2.business.beans;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 5:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProfileForm {

    private String dateTimeZone = "Europe/Paris";

    private String dateFormat = "dd/MM/yyyy";

    private String userLocale = "fr";

    private String returnPath = "/ihm/";

    public String getReturnPath() {
        return returnPath;
    }

    public void setReturnPath(String returnPath) {
        this.returnPath = returnPath;
    }

    public String getDateTimeZone() {
        return dateTimeZone;
    }

    public void setDateTimeZone(String dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getUserLocale() {
        return userLocale;
    }

    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }
}
