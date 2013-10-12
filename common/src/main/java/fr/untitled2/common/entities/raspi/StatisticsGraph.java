package fr.untitled2.common.entities.raspi;

import com.beust.jcommander.internal.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/30/13
 * Time: 10:27 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class StatisticsGraph {

    private List<StatisticsGraphEntry> stats  = Lists.newArrayList();

    public List<StatisticsGraphEntry> getStats() {
        return stats;
    }

    public void setStats(List<StatisticsGraphEntry> stats) {
        this.stats = stats;
    }
}
