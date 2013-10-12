package fr.untitled2.common.entities.raspi;

import com.beust.jcommander.internal.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class ServerStatistics {

    @XmlElement
    private List<ServerStatistic> statistics = Lists.newArrayList();

    public List<ServerStatistic> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<ServerStatistic> statistics) {
        this.statistics = statistics;
    }
}
