package fr.untitled2.common.entities.raspi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/27/13
 * Time: 12:53 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class SimpleResponse {

    private boolean state;

    public SimpleResponse() {
    }

    public SimpleResponse(boolean state) {
        this.state = state;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
