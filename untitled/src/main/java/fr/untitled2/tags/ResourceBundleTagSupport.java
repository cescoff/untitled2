package fr.untitled2.tags;

import com.google.common.base.Throwables;
import fr.untitled2.entities.User;
import fr.untitled2.mvc.AppRole;
import fr.untitled2.utils.I18nUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/27/13
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceBundleTagSupport extends TagSupport {

    private static Logger logger = LoggerFactory.getLogger(ResourceBundleTagSupport.class);

    private String packageName;

    private String key;

    private String defaultValue;

    @Override
    public int doStartTag() throws JspException {
        User currentUser = getUser();
        Locale locale = pageContext.getRequest().getLocale();
        if (locale == null) locale = Locale.ENGLISH;
        if (currentUser != null) {
            locale = new Locale(currentUser.getLocale());
        }
        if (!I18nUtils.supportedLanguages.contains(locale.getLanguage())) locale = Locale.ENGLISH;
        else locale = new Locale(locale.getLanguage());

        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(packageName, locale);
            if (!resourceBundle.containsKey(key)) {
                print(defaultValue);
            } else print(new String(resourceBundle.getString(key).getBytes("ISO-8859-1"), "UTF-8"));
        } catch (Throwable t) {
            logger.error("Impossible de charger le bundle '" + packageName + "' avec la locale '" + locale + "'", t);
            if (currentUser != null && currentUser.getRoles().contains(AppRole.ADMIN)) {
                print("Impossible de charger le bundle '" + packageName + "' avec la locale '" + locale + "'");
                print(Throwables.getStackTraceAsString(t));
            }
            print(defaultValue);
        }

        return SKIP_BODY;
    }

    private void print(String message) {
        try {
            pageContext.getOut().print(message);
        } catch (IOException e) {
            logger.error("Impossible d'écrire sur la sortie standard", e);
        }
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) return (User) principal;
        else return null;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
