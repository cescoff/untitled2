package fr.untitled2.servlet;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/5/13
 * Time: 6:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class WarmupServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
        urlFetchService.fetch(new URL("http://application.mypicturelog.com/ihm/"));
    }
}
