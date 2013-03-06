package fr.untitled2.mvc;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.beans.ProfileForm;
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
        ProfileForm form = new ProfileForm();
        form.setDateFormat(user.getDateFormat());
        form.setDateTimeZone(user.getTimeZoneId());
        form.setUserLocale(user.getLocale());
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
        return profile(model);
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
