package fr.untitled2.mvc;

import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.business.beans.LogList;
import fr.untitled2.entities.BatchServer;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 7:37 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class ServerListController extends AuthenticatedController implements Serializable {

    private static BatchServerBusiness batchServerBusiness = new BatchServerBusiness();

    @RequestMapping(value = "/servers/list", method = RequestMethod.GET)
    public String list(Model model) {
        return MVCConstants.server_list_view;
    }

    @RequestMapping(value = "/servers/detail", method = RequestMethod.GET)
    public String list(@RequestParam("serverId") String serverId, Model model) {
        BatchServer batchServer = batchServerBusiness.getBatchServer(getUser(), serverId);
        if (batchServer != null) {
            if (!batchServer.isConnected()) {
                model.addAttribute("url", batchServer.getGenerateTokenUrl());
                model.addAttribute("serverId", batchServer.getServerId());
                return MVCConstants.server_connect_view;
            } else {
                return MVCConstants.server_details_view;
            }
        }
        return MVCConstants.server_list_view;
    }



}
