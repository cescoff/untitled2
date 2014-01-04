package fr.untitled2.mvc;

import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.entities.BatchServer;
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
public class JourneysController extends AuthenticatedController implements Serializable {

    private static BatchServerBusiness batchServerBusiness = new BatchServerBusiness();

    @RequestMapping(value = "/journeys/list", method = RequestMethod.GET)
    public String list(Model model) {
        return MVCConstants.journeys_list_view;
    }

}
