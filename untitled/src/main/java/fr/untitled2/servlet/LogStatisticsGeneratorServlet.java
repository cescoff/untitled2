package fr.untitled2.servlet;

import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.LogBusiness;
import fr.untitled2.business.beans.LogList;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.User;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 19/08/13
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */
public class LogStatisticsGeneratorServlet extends HttpServlet {

    private static LogBusiness logBusiness = new LogBusiness();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String logKey = req.getParameter("logKey");
        if (StringUtils.isNotEmpty(logKey)) {
            Log log = ObjectifyService.ofy().load().key(Key.create(Log.class, logKey)).get();
            logBusiness.updateLogStatistics(log);
        } else {
            int pageNumber = 0;
            LogList logList;
            User user = getUser();

            do {
                logList = logBusiness.getLogList(pageNumber, user);
                for (Log log : logList.getLogs()) {
                    logBusiness.updateLogStatistics(log);
                }
            } while (logList.getNextPageNumber() > 0);

        }
    }

    private User getUser() {
        User applicationUser = null;
        for (User userCandidate : ObjectifyService.ofy().load().type(User.class).list()) {
            if (userCandidate.getEmail().equals(UserServiceFactory.getUserService().getCurrentUser().getEmail())) {
                applicationUser = userCandidate;
                break;
            }
        }
        return applicationUser;
    }

}
