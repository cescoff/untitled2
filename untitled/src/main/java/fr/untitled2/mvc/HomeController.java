package fr.untitled2.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 6:12 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class HomeController {

    private static Logger logger = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping(value = "/")
    public String home() {
        logger.info("HomeController: Passing through...");
        return "home";
    }

}
