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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String logPersistenceJobKey = req.getParameter(ServletConstants.log_peristence_job_key);
        try {

            LogPersistenceJob logPersistenceJob = ObjectifyService.ofy().load().key(Key.create(LogPersistenceJob.class, logPersistenceJobKey)).get();

            logger.info("Chargement du user '" + logPersistenceJob.getUserKey() + "'");

            User user = ObjectifyService.ofy().load().key(logPersistenceJob.getUserKey()).get();

            logger.info("User chargé : '" + user + "'");

            AppEngineFile appEngineFile = new AppEngineFile(logPersistenceJob.getFilePath());
            FileService fileService = FileServiceFactory.getFileService();
            FileReadChannel fileReadChannel = fileService.openReadChannel(appEngineFile, false);

            InputStream inputStream = Channels.newInputStream(fileReadChannel);

            LogRecording logRecording = null;

            try {
                logRecording = JSonUtils.readJson(LogRecording.class, inputStream);
            } catch (Throwable t) {
                logger.error("Impossible de charger le fichier '" + logPersistenceJob.getFilePath() + "'");
            } finally {
                inputStream.close();
                fileReadChannel.close();
            }

            logger.info("LogRecording Chargé : " + logRecording);

            Log log = new Log();
            log.setTimeZoneId(logRecording.getDateTimeZone());
            log.setName(logRecording.getName());
            if (CollectionUtils.isNotEmpty(logRecording.getRecords())) {
                log.setStartTime(LogRecording.DATE_ORDERING.sortedCopy(logRecording.getRecords()).get(0).getDateTime());
                log.setEndTime(LogRecording.DATE_ORDERING.reverse().sortedCopy(logRecording.getRecords()).get(0).getDateTime());
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
            fileService.delete(appEngineFile);
            ObjectifyService.ofy().delete().entity(logPersistenceJob);
        } catch (Throwable t) {
            logger.error("Un erreur s'est produite lors du traitement du fichier '" + logPersistenceJobKey + "'", t);
        }
    }


}
