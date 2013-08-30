package fr.untitled2.servlet.process;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.LogBusiness;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.LogPersistenceJob;
import fr.untitled2.entities.TrackPoint;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.JSonUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 19/08/13
 * Time: 12:25
 * To change this template use File | Settings | File Templates.
 */
public class LogPersistenceServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(LogPersistenceServlet.class);

    private static final LogBusiness logBusiness = new LogBusiness();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String logPersistenceJobKey = req.getParameter(ServletConstants.log_peristence_job_key);
        if (StringUtils.isEmpty(logPersistenceJobKey)) {
            logger.error("Clef de persitence job vide, non supporté");
            return;
        }
        logger.info("Voici la clef de log persistence '" + logPersistenceJobKey + "'");
        try {

            LogPersistenceJob logPersistenceJob = ObjectifyService.ofy().load().key(Key.create(LogPersistenceJob.class, logPersistenceJobKey)).get();

            logger.info("Chargement du user '" + logPersistenceJob.getUserKey() + "'");

            User user = ObjectifyService.ofy().load().key(logPersistenceJob.getUserKey()).get();

            logger.info("User chargé : '" + user + "'");

            LogRecording logRecording = logPersistenceJob.getLogRecording();

            logger.info("LogRecording Chargé : " + logRecording);

            Log log = new Log();
            log.setTimeZoneId(logRecording.getDateTimeZone());
            log.setName(logRecording.getName());
            List<LogRecording.LogRecord> logRecords = LogRecording.DATE_ORDERING.sortedCopy(logRecording.getRecords());
            if (CollectionUtils.isNotEmpty(logRecording.getRecords())) {
                log.setStartTime(logRecords.get(0).getDateTime());
                log.setEndTime(logRecords.get(logRecords.size() - 1).getDateTime());
            }
            log.setUser(user);
            log.setValidated(true);

            for (LogRecording.LogRecord logRecord : logRecording.getRecords()) {
                TrackPoint trackPoint = new TrackPoint();
                trackPoint.setPointDate(logRecord.getDateTime());
                trackPoint.setLatitude(logRecord.getLatitude());
                trackPoint.setLongitude(logRecord.getLongitude());
                log.getTrackPoints().add(trackPoint);
            }

            logBusiness.persistLog(user, log);
            ObjectifyService.ofy().delete().entity(logPersistenceJob);
        } catch (Throwable t) {
            logger.error("Un erreur s'est produite lors du traitement du fichier '" + logPersistenceJobKey + "'", t);
        }
    }


}
