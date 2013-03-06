package fr.untitled2.servlet;

import fr.untitled2.entities.User;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 3:00 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ModelViewServlet extends ControlerServlet {

    @Override
    protected final void get(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        get(request, response, user, StringUtils.remove(request.getRequestURI(), getRootPath()));
    }

    @Override
    protected final void post(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        post(request, response, user, StringUtils.remove(request.getRequestURI(), getRootPath()));
    }

    protected void get(HttpServletRequest request, HttpServletResponse response, User user, String view) throws ServletException, IOException {
        response.sendError(405, "Not supported");
    }

    protected void post(HttpServletRequest request, HttpServletResponse response, User user, String view) throws ServletException, IOException {
        response.sendError(405, "Not supported");
    }

    protected abstract String getRootPath();

}
