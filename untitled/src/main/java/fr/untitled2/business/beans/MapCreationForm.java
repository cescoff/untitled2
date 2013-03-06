package fr.untitled2.business.beans;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/11/13
 * Time: 7:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class MapCreationForm {

    private String name;

    private String dateStart;

    private String dateEnd;

    private String timeZoneId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}
