package fr.untitled2.mvc;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 6:16 AM
 * To change this template use File | Settings | File Templates.
 */
import org.springframework.web.context.request.WebRequest;

public class AjaxUtils {

    public static boolean isAjaxRequest(WebRequest webRequest) {
        String requestedWith = webRequest.getHeader("X-Requested-With");
        return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
    }

    public static boolean isAjaxUploadRequest(WebRequest webRequest) {
        return webRequest.getParameter("ajaxUpload") != null;
    }

    private AjaxUtils() {}

}
