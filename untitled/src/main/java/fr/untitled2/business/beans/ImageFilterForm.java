package fr.untitled2.business.beans;

import org.joda.time.LocalDate;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/26/13
 * Time: 5:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageFilterForm {

    private LocalDate dateStart;

    private LocalDate dateEnd;

    public LocalDate getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDate getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDate dateEnd) {
        this.dateEnd = dateEnd;
    }
}
