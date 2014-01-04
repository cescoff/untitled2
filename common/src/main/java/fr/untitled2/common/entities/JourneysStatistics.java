package fr.untitled2.common.entities;

import com.beust.jcommander.internal.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 04/12/13
 * Time: 22:48
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class JourneysStatistics {

    private List<JourneyStatistic> statistics = Lists.newArrayList();

    public List<JourneyStatistic> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<JourneyStatistic> statistics) {
        this.statistics = statistics;
    }
}
