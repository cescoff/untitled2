package fr.untitled2.mvc;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.beans.ProfileForm;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.entities.User;
import fr.untitled2.security.GaeUserAuthentication;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 5:02 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class SecurityController extends AuthenticatedController {

    private static Logger logger = LoggerFactory.getLogger(SecurityController.class);

    @RequestMapping(value = "/register.htm", method = RequestMethod.GET)
    public String register(final Model model) {
        final ProfileForm form = new ProfileForm();
        model.addAttribute(MVCConstants.registration_form_bean, form);
        model.addAttribute(MVCConstants.registration_all_time_zones, Lists.newArrayList(TimeZone.getAvailableIDs()));
        Map<String, String> localeMap = Maps.newHashMap();
        localeMap.put(Locale.FRENCH.toString(), "Français");
        localeMap.put(Locale.ENGLISH.toString(), "English");
        localeMap.put(Locale.GERMAN.toString(), "Deutsch");
        model.addAttribute(MVCConstants.registration_all_supportedLocales, localeMap);
        model.addAttribute(MVCConstants.registration_all_date_formats, Lists.newArrayList("dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd", "yyyy/dd/MM"));
        model.addAttribute(MVCConstants.registration_form_mode_attribute, "true");
        return MVCConstants.profile_view;
    }

    @RequestMapping(value = "/start.htm", method = RequestMethod.GET)
    public String start(@RequestParam("returnPath") String returnPath, final Model model) {
        if (StringUtils.isEmpty(returnPath)) returnPath = "/ihm/logs";
        model.addAttribute(MVCConstants.google_login_attribute, UserServiceFactory.getUserService().createLoginURL(returnPath));
        model.addAttribute(MVCConstants.facebook_login_attribute, "/ihm/signin/facebook");
        return MVCConstants.start_view;
    }

    @RequestMapping(value = "/profile.htm", method = RequestMethod.GET)
    public String profile(final Model model) {
        User user = getUser();

        List<KnownLocation> knownLocations = Lists.newArrayList(user.getKnownLocations());
        logger.info("Found '" + knownLocations.size() + "' known locations");

        ProfileForm form = new ProfileForm();
        form.setDateFormat(user.getDateFormat());
        form.setDateTimeZone(user.getTimeZoneId());
        form.setUserLocale(user.getLocale());

        if (knownLocations.size() > 0) {
            form.setKnownLocationName1(knownLocations.get(0).getName());
            form.setKnownLocationLatitude1(knownLocations.get(0).getLatitude() + "");
            form.setKnownLocationLongitude1(knownLocations.get(0).getLongitude() + "");
        }

        if (knownLocations.size() > 1) {
            form.setKnownLocationName2(knownLocations.get(1).getName());
            form.setKnownLocationLatitude2(knownLocations.get(1).getLatitude() + "");
            form.setKnownLocationLongitude2(knownLocations.get(1).getLongitude() + "");
        }

        if (knownLocations.size() > 2) {
            form.setKnownLocationName3(knownLocations.get(2).getName());
            form.setKnownLocationLatitude3(knownLocations.get(2).getLatitude() + "");
            form.setKnownLocationLongitude3(knownLocations.get(2).getLongitude() + "");
        }

        if (knownLocations.size() > 3) {
            form.setKnownLocationName4(knownLocations.get(3).getName());
            form.setKnownLocationLatitude4(knownLocations.get(3).getLatitude() + "");
            form.setKnownLocationLongitude4(knownLocations.get(3).getLongitude() + "");
        }

        if (knownLocations.size() > 4) {
            form.setKnownLocationName5(knownLocations.get(4).getName());
            form.setKnownLocationLatitude5(knownLocations.get(4).getLatitude() + "");
            form.setKnownLocationLongitude5(knownLocations.get(4).getLongitude() + "");
        }

        if (knownLocations.size() > 5) {
            form.setKnownLocationName6(knownLocations.get(5).getName());
            form.setKnownLocationLatitude6(knownLocations.get(5).getLatitude() + "");
            form.setKnownLocationLongitude6(knownLocations.get(5).getLongitude() + "");
        }

        if (knownLocations.size() > 6) {
            form.setKnownLocationName7(knownLocations.get(6).getName());
            form.setKnownLocationLatitude7(knownLocations.get(6).getLatitude() + "");
            form.setKnownLocationLongitude7(knownLocations.get(6).getLongitude() + "");
        }

        if (knownLocations.size() > 7) {
            form.setKnownLocationName8(knownLocations.get(7).getName());
            form.setKnownLocationLatitude8(knownLocations.get(7).getLatitude() + "");
            form.setKnownLocationLongitude8(knownLocations.get(7).getLongitude() + "");
        }

        if (knownLocations.size() > 8) {
            form.setKnownLocationName9(knownLocations.get(8).getName());
            form.setKnownLocationLatitude9(knownLocations.get(8).getLatitude() + "");
            form.setKnownLocationLongitude9(knownLocations.get(8).getLongitude() + "");
        }

        if (knownLocations.size() > 9) {
            form.setKnownLocationName10(knownLocations.get(9).getName());
            form.setKnownLocationLatitude10(knownLocations.get(9).getLatitude() + "");
            form.setKnownLocationLongitude10(knownLocations.get(9).getLongitude() + "");
        }

        if (knownLocations.size() > 10) {
            form.setKnownLocationName11(knownLocations.get(10).getName());
            form.setKnownLocationLatitude11(knownLocations.get(10).getLatitude() + "");
            form.setKnownLocationLongitude11(knownLocations.get(10).getLongitude() + "");
        }

        if (knownLocations.size() > 11) {
            form.setKnownLocationName12(knownLocations.get(11).getName());
            form.setKnownLocationLatitude12(knownLocations.get(11).getLatitude() + "");
            form.setKnownLocationLongitude12(knownLocations.get(11).getLongitude() + "");
        }

        if (knownLocations.size() > 12) {
            form.setKnownLocationName13(knownLocations.get(12).getName());
            form.setKnownLocationLatitude13(knownLocations.get(12).getLatitude() + "");
            form.setKnownLocationLongitude13(knownLocations.get(12).getLongitude() + "");
        }

        if (knownLocations.size() > 13) {
            form.setKnownLocationName14(knownLocations.get(13).getName());
            form.setKnownLocationLatitude14(knownLocations.get(13).getLatitude() + "");
            form.setKnownLocationLongitude14(knownLocations.get(13).getLongitude() + "");
        }

        if (knownLocations.size() > 14) {
            form.setKnownLocationName15(knownLocations.get(14).getName());
            form.setKnownLocationLatitude15(knownLocations.get(14).getLatitude() + "");
            form.setKnownLocationLongitude15(knownLocations.get(14).getLongitude() + "");
        }

        model.addAttribute(MVCConstants.registration_form_bean, form);
        model.addAttribute(MVCConstants.registration_all_time_zones, Lists.newArrayList(TimeZone.getAvailableIDs()));

        Map<String, String> localeMap = Maps.newHashMap();
        localeMap.put(Locale.FRENCH.toString(), "Français");
        localeMap.put(Locale.ENGLISH.toString(), "English");
        localeMap.put(Locale.GERMAN.toString(), "Deutsch");
        model.addAttribute(MVCConstants.registration_all_supportedLocales, localeMap);
        model.addAttribute(MVCConstants.registration_all_date_formats, Lists.newArrayList("dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd", "yyyy/dd/MM"));




        return MVCConstants.profile_view;
    }



    @RequestMapping(value = "/profile/validation", method = RequestMethod.POST)
    public String register(@Valid ProfileForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return null;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User)authentication.getPrincipal();
        Set<AppRole> roles = EnumSet.of(AppRole.USER);

        if (UserServiceFactory.getUserService().isUserLoggedIn() && UserServiceFactory.getUserService().isUserAdmin()) {
            roles.add(AppRole.ADMIN);
        }

        User user = new User(currentUser.getEmail());
        user.setTimeZoneId(form.getDateTimeZone());
        user.setUserId(currentUser.getUserId());
        user.setDateFormat(form.getDateFormat());
        user.setLocale(form.getUserLocale());
        user.setNickName(currentUser.getNickName());
        user.setAuthMode(currentUser.getAuthMode());
        user.getRoles().addAll(roles);
        user.setEnabled(true);

        List<KnownLocation> knownLocations = Lists.newArrayList();
        if (StringUtils.isNotEmpty(form.getKnownLocationName1()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude1()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude1())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName1());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude1()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude1()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName2()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude2()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude2())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName2());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude2()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude2()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName3()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude3()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude3())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName3());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude3()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude3()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName4()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude4()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude4())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName4());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude4()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude4()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName5()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude5()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude5())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName5());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude5()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude5()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName6()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude6()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude6())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName6());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude6()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude6()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName7()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude7()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude7())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName7());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude7()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude7()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName8()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude8()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude8())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName8());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude8()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude8()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName9()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude9()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude9())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName9());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude9()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude9()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName10()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude10()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude10())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName10());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude10()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude10()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName11()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude11()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude11())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName11());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude11()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude11()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName12()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude12()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude12())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName12());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude12()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude12()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName13()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude13()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude13())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName13());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude13()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude13()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName13()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude13()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude13())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName13());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude13()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude13()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName14()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude14()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude14())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName14());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude14()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude14()));
            knownLocations.add(knownLocation);
        }

        if (StringUtils.isNotEmpty(form.getKnownLocationName15()) && StringUtils.isNotEmpty(form.getKnownLocationLatitude15()) && StringUtils.isNotEmpty(form.getKnownLocationLongitude15())) {
            KnownLocation knownLocation = new KnownLocation();
            knownLocation.setName(form.getKnownLocationName15());
            knownLocation.setLatitude(Double.parseDouble(form.getKnownLocationLatitude15()));
            knownLocation.setLongitude(Double.parseDouble(form.getKnownLocationLongitude15()));
            knownLocations.add(knownLocation);
        }

        user.setKnownLocations(knownLocations);

        ObjectifyService.ofy().save().entity(user);

        // Update the context with the full authentication
        GaeUserAuthentication gaeUserAuthentication = new GaeUserAuthentication(user, authentication.getDetails());
        gaeUserAuthentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(gaeUserAuthentication);
        String returnPath = form.getReturnPath();
        if (StringUtils.isNotEmpty(returnPath)) try {
            returnPath = URLDecoder.decode(returnPath, "UTF-8");
            return "redirect:" + returnPath;
        } catch (UnsupportedEncodingException e) {
        }
        return "redirect:/ihm/profile.htm";
    }

    @RequestMapping(value = "/logout.htm", method = RequestMethod.GET)
    public String logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:" + UserServiceFactory.getUserService().createLogoutURL("/ihm/");
    }

}
