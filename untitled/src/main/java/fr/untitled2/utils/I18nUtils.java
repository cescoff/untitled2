package fr.untitled2.utils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/25/13
 * Time: 7:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class I18nUtils {

    private static Map<Locale, String> date_format_by_locale = Maps.newHashMap();

    static {
        date_format_by_locale.put(Locale.FRENCH, "dd/MM/yyyy");
        date_format_by_locale.put(Locale.ENGLISH, "yyyy/MM/dd");
        date_format_by_locale.put(Locale.GERMAN, "dd/MM/yyyy");
        date_format_by_locale.put(Locale.UK, "MM/dd/yyyy");
    }

    public static Collection<String> supportedLanguages = Sets.newHashSet("fr", "en", "de");

    public static String getDateFormatFromRequest(HttpServletRequest request) {
        if (!date_format_by_locale.containsKey(request.getLocale())) return "yyyy/MM/dd";
        return date_format_by_locale.get(request.getLocale());
    }

}
