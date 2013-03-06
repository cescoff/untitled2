package fr.untitled2.mvc;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.ImageBusiness;
import fr.untitled2.business.LogBusiness;
import fr.untitled2.business.MapBusiness;
import fr.untitled2.business.beans.*;
import fr.untitled2.entities.*;
import fr.untitled2.servlet.ImageDisplayMode;
import fr.untitled2.utils.I18nUtils;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.TimeZone;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 6:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class MapController extends AuthenticatedController implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(MapController.class);

    @Autowired
    private MapBusiness mapBusiness;

    @Autowired
    private ImageBusiness imageBusiness;

    @RequestMapping(value = "/maps")
    public String home(Model model) {
        return list("0", model);
    }

    @RequestMapping(value = "/maps/list", method = RequestMethod.GET)
    public String list(@RequestParam("pageNumber") String pageNumber, Model model) {
        int pageNr = 0;
        if (StringUtils.isNotEmpty(pageNumber)) pageNr = Integer.parseInt(pageNumber);
        logger.info("PageNumber:" + pageNumber + "->" + pageNr);
        MapList mapList = mapBusiness.getMapList(pageNr, getUser());
        if (mapList != null) model.addAttribute(MVCConstants.map_list_attribute, mapList);

        return MVCConstants.map_list_view;
    }

    @RequestMapping(value = "/maps/add", method = RequestMethod.GET)
    public String addView(Model model) {
        User user = getUser();
        final MapCreationForm form = new MapCreationForm();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(user.getDateFormat());
        form.setDateStart(dateTimeFormatter.print(LocalDateTime.now().minusMonths(1)));
        form.setDateEnd(dateTimeFormatter.print(LocalDateTime.now()));
        model.addAttribute(MVCConstants.map_form_attribute, form);
        model.addAttribute(MVCConstants.map_all_time_zones, Lists.newArrayList(TimeZone.getAvailableIDs()));

        return MVCConstants.map_add_view;
    }

    @RequestMapping(value = "/maps/create", method = RequestMethod.POST)
    public String createMap(@Valid MapCreationForm mapCreationForm, Model model) {
        PictureMap pictureMap = new PictureMap();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(getUser().getDateFormat());
        pictureMap.setPeriodEnd(dateTimeFormatter.parseLocalDate(mapCreationForm.getDateEnd()));
        pictureMap.setPeriodStart(dateTimeFormatter.parseLocalDate(mapCreationForm.getDateStart()));
        pictureMap.setName(mapCreationForm.getName());
        pictureMap.setCreationDate(DateTime.now().toLocalDateTime());
        pictureMap.setUser(getUser());
        pictureMap.setSharingKey(SignUtils.calculateSha1Digest(pictureMap.getName() + pictureMap.getPeriodStart() + pictureMap.getPeriodEnd() + getUser().getUserId() + mapCreationForm.getTimeZoneId() + pictureMap.getCreationDate()));
        ObjectifyService.ofy().save().entity(pictureMap).now();
        return list("0", model);
    }

    @RequestMapping(value = "/maps/view", method = RequestMethod.GET)
    public String viewMap(@RequestParam("mapKey") String mapKey, Model model, HttpServletRequest request) {
        final PictureMap pictureMap = ObjectifyService.ofy().load().key(Key.create(PictureMap.class, mapKey)).get();

        model.addAttribute(MVCConstants.map_attribute, pictureMap);
        User user = getUser();
        DateTimeFormatter dateTimeFormatter = null;
        if (user == null) dateTimeFormatter = DateTimeFormat.forPattern(I18nUtils.getDateFormatFromRequest(request) + " HH:mm:ss");
        else dateTimeFormatter = DateTimeFormat.forPattern(user.getDateFormat() + " HH:mm:ss");
        model.addAttribute(MVCConstants.map_marker_attribute, mapBusiness.getMapMarkers(dateTimeFormatter, mapKey, getUser()));
        return MVCConstants.map_view_view;
    }

    @RequestMapping(value = "/maps/json", method = RequestMethod.GET)
    public void getJsonMap(@RequestParam("mapKey") String mapKey, HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = getUser();
        DateTimeFormatter dateTimeFormatter = null;
        if (user == null) dateTimeFormatter = DateTimeFormat.forPattern(I18nUtils.getDateFormatFromRequest(request) + " HH:mm:ss");
        else dateTimeFormatter = DateTimeFormat.forPattern(user.getDateFormat() + " HH:mm:ss");
        response.getOutputStream().write(JSonUtils.writeJson(mapBusiness.getMapMarkers(dateTimeFormatter, mapKey, getUser())).getBytes());
    }

}
