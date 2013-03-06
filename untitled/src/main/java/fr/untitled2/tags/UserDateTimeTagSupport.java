package fr.untitled2.tags;

import com.google.common.base.Throwables;
import fr.untitled2.entities.User;
import fr.untitled2.mvc.AppRole;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/25/13
 * Time: 6:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserDateTimeTagSupport extends TagSupport {

    private static Logger logger = LoggerFactory.getLogger(UserDateTimeTagSupport.class);

    private String value;

    @Override
    public int doStartTag() throws JspException {
        if (StringUtils.isEmpty(value)) return SKIP_BODY;
        Object dateTimeObject = pageContext.getAttribute(value);

        String dateFormat = "yyyy/MM/dd";
        User user = getUser();
        if (user != null) dateFormat = user.getDateFormat();

        try {
            if (dateTimeObject instanceof LocalDate) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormat);
                pageContext.getOut().write(dateTimeFormatter.print((LocalDate) dateTimeObject));
            } else if (dateTimeObject instanceof ReadableInstant) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormat + " HH:mm");
                pageContext.getOut().write(dateTimeFormatter.print((ReadableInstant) dateTimeObject));
            } else if (dateTimeObject instanceof ReadablePartial) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormat + " HH:mm");
                pageContext.getOut().write(dateTimeFormatter.print((ReadablePartial) dateTimeObject));
            }
        } catch (Throwable t) {
            if (user != null && user.getRoles().contains(AppRole.ADMIN)) {
                try {
                    pageContext.getOut().write(Throwables.getStackTraceAsString(t));
                } catch (Throwable t2) {
                    logger.error("Impossible d'écrire sur le writer standard", t2);
                }
            }
            logger.error("Une erreur s'est produite lors du rendu d'une date", t);
        }


        return SKIP_BODY;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) return (User) principal;
        else return null;
    }


}
