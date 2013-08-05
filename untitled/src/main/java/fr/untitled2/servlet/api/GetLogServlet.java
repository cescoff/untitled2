package fr.untitled2.servlet.api;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.labs.repackaged.com.google.common.base.Function;
import com.google.appengine.labs.repackaged.com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.LogBusiness;
import fr.untitled2.business.beans.LogList;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.LogRecordings;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.TrackPoint;
import fr.untitled2.entities.User;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.JSonUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/16/13
 * Time: 12:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class GetLogServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(GetLogServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String startDateString = req.getParameter("startDate");
        String endDateString = req.getParameter("endDate");

        LocalDateTime start = new LocalDateTime(startDateString);
        LocalDateTime end = new LocalDateTime(endDateString);

        com.google.appengine.api.users.User user = null;
        try {
            user = OAuthServiceFactory.getOAuthService().getCurrentUser();
        } catch (OAuthRequestException e) {
            logger.error("Impossible d'authentifier l'utilisateur", e);
        }
        if (user == null) {
            logger.error("Utilisateur non connecté");
            resp.sendError(403, "Forbidden");
        }
        User applicationUser = null;
        for (User userCandidate : ObjectifyService.ofy().load().type(User.class).list()) {
            if (userCandidate.getEmail().equals(user.getEmail())) {
                applicationUser = userCandidate;
                break;
            }
        }

        LogBusiness logBusiness = new LogBusiness();
        LogRecordings logRecordings = new LogRecordings();


        LogList logList = logBusiness.getLogList(0, applicationUser);

        Collection<Log> candidates = getCandidates(logList.getLogs(), start, end);
        if (CollectionUtils.isNotEmpty(candidates)) {
            logger.info("Log trouvé (" + logList.getNextPageNumber() + ")");
            logRecordings.getLogRecordings().addAll(Collections2.transform(candidates, GetLogRecording));
        }

        logger.info("Nombre de log trouves : " + logRecordings.getLogRecordings().size() + " recherche d'autre candidats (" + logList.getNextPageNumber() + ")");

        while (logList.getNextPageNumber() > 0) {
            logList = logBusiness.getLogList(logList.getNextPageNumber(), applicationUser);
            logger.info("Nouvelle page chargee (" + logList.getNextPageNumber() + ")");
            candidates = getCandidates(logList.getLogs(), start, end);
            if (CollectionUtils.isNotEmpty(candidates)) {
                logRecordings.getLogRecordings().addAll(Collections2.transform(candidates, GetLogRecording));
            }
        }
        if (CollectionUtils.isEmpty(logRecordings.getLogRecordings())) resp.sendError(404, "No matching log");
        else JSonUtils.writeJson(logRecordings, resp.getOutputStream());
    }

    private Collection<Log> getCandidates(Collection<Log> logs, LocalDateTime start, LocalDateTime end) {
        Collection<Log> result = Lists.newArrayList();
        for (Log log : logs) {
            if (((log.getStartTime().isAfter(start.minusHours(6)) && log.getEndTime().isBefore(end.plusHours(6))))
                    || (start.isAfter(log.getStartTime().minusHours(1)) && start.isBefore(log.getEndTime().plusHours(1)))
                    || (end.isAfter(log.getStartTime().minusHours(1)) && end.isBefore(log.getEndTime().plusHours(1)))) {
                result.add(log);
            }
        }
        return result;
    }

    private static Function<Log, LogRecording> GetLogRecording = new Function<Log, LogRecording>() {
        @Override
        public LogRecording apply(Log log) {
            LogRecording result = new LogRecording();
            result.setName(log.getName());
            result.setDateTimeZone(log.getTimeZoneId());
            result.getRecords().addAll(Collections2.transform(log.getTrackPoints(), new Function<TrackPoint, LogRecording.LogRecord>() {
                @Override
                public LogRecording.LogRecord apply(TrackPoint trackPoint) {
                    LogRecording.LogRecord logRecord = new LogRecording.LogRecord();
                    logRecord.setDateTime(trackPoint.getPointDate());
                    logRecord.setLatitude(trackPoint.getLatitude());
                    logRecord.setLongitude(trackPoint.getLongitude());
                    return logRecord;
                }
            }));

            return result;
        }
    };

}
