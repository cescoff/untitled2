package fr.untitled2.mvc;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.ImageBusiness;
import fr.untitled2.business.LogBusiness;
import fr.untitled2.business.beans.LogList;
import fr.untitled2.business.beans.MapMarkers;
import fr.untitled2.business.beans.Marker;
import fr.untitled2.entities.*;
import fr.untitled2.servlet.ImageDisplayMode;
import fr.untitled2.utils.IterablesUtils;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.StatisticsUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 6:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class LogController extends AuthenticatedController implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(LogController.class);

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yy HH:mm");

    @Autowired
    private LogBusiness logBusiness;

    @Autowired
    private ImageBusiness imageBusiness;

    @RequestMapping(value = "/logs")
    public String home(Model model) {
        return list("0", model);
    }

    @RequestMapping(value = "/logs/list", method = RequestMethod.GET)
    public String list(@RequestParam("pageNumber") String pageNumber, Model model) {
        int pageNr = 0;
        if (StringUtils.isNotEmpty(pageNumber)) pageNr = Integer.parseInt(pageNumber);
        logger.info("PageNumber:" + pageNumber + "->" + pageNr);
        LogList logList = logBusiness.getLogList(pageNr, getUser());


        if (logList != null) model.addAttribute(MVCConstants.log_list_attribute, logList);

        return MVCConstants.log_list_view;
    }

    @RequestMapping(value = "/logs/validate-pending-log", method = RequestMethod.GET)
    public String validatePendingLog(@RequestParam("key") String key, Model model) {
        logger.info("Clef de PendingLog trouvee");
        PendingLog pendingLog = ObjectifyService.ofy().load().key(Key.create(PendingLog.class, key)).get();
        if (pendingLog == null) {
            logger.info("Aucun pending trip trouve");
        } else {
            logger.info("PendingLog trouve avec date " + pendingLog.getCreationDate());
            Objectify objectify = ObjectifyService.ofy();

            Log log = objectify.load().key(pendingLog.getTripKey()).get();
            log.setValidated(true);
            logger.info("Validation du log '" + log.getName() + "'");
            objectify.save().entity(log).now();
            objectify.delete().entity(pendingLog).now();
        }
        list("0", model);
        return MVCConstants.log_list_view;
    }

    @RequestMapping(value = "/logs/validate-log", method = RequestMethod.GET)
    public String validateLog(@RequestParam("logKey") String logKey, Model model) {
        logger.info("Clef de trip trouvee");
        Objectify objectify = ObjectifyService.ofy();
        Log logToBeValidated = objectify.load().key(Key.create(Log.class, logKey)).get();
        logToBeValidated.setValidated(true);
        objectify.save().entity(logToBeValidated);
        Iterable<PendingLog> pendingTrips = ObjectifyService.ofy().load().type(PendingLog.class).filter("trip", logToBeValidated);
        for (PendingLog pendingLog : pendingTrips) {
            ObjectifyService.ofy().delete().entity(pendingLog);
        }
        list("0", model);
        return MVCConstants.log_list_view;
    }

    @RequestMapping(value = "/logs/map", method = RequestMethod.GET)
    public String logMap(@RequestParam("logKey") String logKey, Model model) {
        Log log = ObjectifyService.ofy().load().key(Key.create(Log.class, logKey)).get();

        model.addAttribute(MVCConstants.log_attribute, log);
        return MVCConstants.log_map_view;
    }

    @RequestMapping(value = "/logs/json", method = RequestMethod.GET)
    public void getJsonMap(@RequestParam("logKey") String logKey, HttpServletResponse response) throws IOException {
        final Log log = ObjectifyService.ofy().load().key(Key.create(Log.class, logKey)).get();

        MapMarkers mapMarkers = new MapMarkers();
        for (TrackPoint trackPoint: StatisticsUtils.TRACK_POINT_SORT.sortedCopy(log.getTrackPoints())) {
            mapMarkers.getMarkers().add(new Marker(trackPoint.getLatitude() + "", trackPoint.getLongitude() + ""));
        }
        response.getOutputStream().write(JSonUtils.writeJson(mapMarkers).getBytes());
    }

    @RequestMapping(value = "/logs/images/json", method = RequestMethod.GET)
    public void getJsonMapImages(@RequestParam("logKey") String logKey, HttpServletResponse response) throws IOException {
        final Log log = ObjectifyService.ofy().load().key(Key.create(Log.class, logKey)).get();

        Iterable<Image> userImages = ObjectifyService.ofy().load().type(Image.class).filter("user", log.getUser());
        if (IterablesUtils.isEmpty(userImages)) {
            logger.info("Aucune image après le filtre par user (" + log.getUser() + ")");
        }
        userImages = Iterables.filter(userImages, new Predicate<Image>() {
            @Override
            public boolean apply(Image image) {
                return image.getDateTaken().isAfter(log.getStartTime()) && image.getDateTaken().isBefore(log.getEndTime());
            }
        });
        if (IterablesUtils.isEmpty(userImages)) {
            logger.info("Aucune image après le filtre par date");
        }
        userImages = Iterables.filter(userImages, new Predicate<Image>() {
            @Override
            public boolean apply(Image image) {
                return image.getLatitude() != null && image.getLongitude() != null;
            }
        });
        if (IterablesUtils.isEmpty(userImages)) {
            logger.info("Aucune image après le filtre par localisation");
        }
        List<Image> sortedimages = StatisticsUtils.IMAGE_SORT.sortedCopy(userImages);
        MapMarkers mapMarkers = new MapMarkers();
        for (Image sortedimage : sortedimages) {
            mapMarkers.getMarkers().add(new Marker(sortedimage.getLatitude() + "", sortedimage.getLongitude() + "", dateTimeFormatter.print(sortedimage.getDateTaken()), "/ihm/images/view?imageKey=" + sortedimage.getImageKey() + "&displayMode=" + ImageDisplayMode.low.getCode(), "/ihm/images/view?imageKey=" + sortedimage.getImageKey() + "&displayMode=" + ImageDisplayMode.lowSquare.getCode(), "/ihm/images/view?imageKey=" + sortedimage.getImageKey() + "&displayMode=" + ImageDisplayMode.medium));
        }
        response.getOutputStream().write(JSonUtils.writeJson(mapMarkers).getBytes());
    }
}
