package fr.untitled2.tags;

import com.google.common.base.Throwables;
import fr.untitled2.business.BatchServerBusiness;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.User;
import fr.untitled2.mvc.AppRole;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.I18nUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 12:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyServerButtonTagSupport  extends TagSupport {

    private static Logger logger = LoggerFactory.getLogger(MyServerButtonTagSupport.class);

    private BatchServerBusiness batchServerBusiness = new BatchServerBusiness();

    private String href;

    private String imgSrc;

    private String messageResourceBundleKey;

    private String packageName;

    private String defaultValue;

    @Override
    public int doStartTag() throws JspException {
        String hostIp = pageContext.getRequest().getRemoteAddr();
        User user = getUser();
        if (user != null) {
            List<BatchServer> servers = batchServerBusiness.getUserServers(user, hostIp);
            if (CollectionUtils.isNotEmpty(servers)) {
                print("<div class=\"menuItem\">\n<a href=\"" + href + "\"><img src=\"" + imgSrc + "\" width=\"25\" title=\"" + getLabel(user) + "\"/></a>\n</div>");
            }
        }
        return SKIP_BODY;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getMessageResourceBundleKey() {
        return messageResourceBundleKey;
    }

    public void setMessageResourceBundleKey(String messageResourceBundleKey) {
        this.messageResourceBundleKey = messageResourceBundleKey;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    private String getLabel(User currentUser) {
        Locale locale = pageContext.getRequest().getLocale();
        if (locale == null) locale = Locale.ENGLISH;
        if (currentUser != null) {
            locale = new Locale(currentUser.getLocale());
        }
        if (!I18nUtils.supportedLanguages.contains(locale.getLanguage())) locale = Locale.ENGLISH;
        else locale = new Locale(locale.getLanguage());

        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(packageName, locale);
            if (!resourceBundle.containsKey(messageResourceBundleKey)) {
                return defaultValue;
            } else return new String(resourceBundle.getString(messageResourceBundleKey).getBytes("ISO-8859-1"), "UTF-8");
        } catch (Throwable t) {
            logger.error("Impossible de charger le bundle '" + packageName + "' avec la locale '" + locale + "'", t);
            if (currentUser != null && currentUser.getRoles().contains(AppRole.ADMIN)) {
                print("Impossible de charger le bundle '" + packageName + "' avec la locale '" + locale + "'");
                print(Throwables.getStackTraceAsString(t));
            }
            return defaultValue;
        }
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

}
