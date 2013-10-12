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
 * Time: 6:49 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class ServerList {

    @XmlElement
    private List<ServerInfos> servers = Lists.newArrayList();

    public List<ServerInfos> getServers() {
        return servers;
    }

    public void setServers(List<ServerInfos> servers) {
        this.servers = servers;
    }
}
