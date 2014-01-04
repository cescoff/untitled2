package fr.untitled2.servlet.process;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Deleter;
import fr.untitled2.business.JourneyBusiness;
import fr.untitled2.common.entities.Journey;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.utils.JourneyUtils;
import fr.untitled2.entities.*;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 23/11/13
 * Time: 16:09
 * To change this template use File | Settings | File Templates.
 */
public class JourneyCalculation extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(JourneyCalculation.class);

    private static JourneyBusiness journeyBusiness = new JourneyBusiness();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    private void print(String message, HttpServletResponse response) throws IOException {
        response.getOutputStream().write(message.getBytes());
        logger.info(message);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            Collection<User> users = ObjectifyService.ofy().load().type(User.class).list();

            for (User user : users) {
                Optional<JourneyCalculationJob> lastRunDate = journeyBusiness.getLastJob(user);

                if (!lastRunDate.isPresent() && getJourneyCount(DatastoreServiceFactory.getDatastoreService()) > 0) {
                    logger.info("Deleting '" + user.getEmail() + "'");
                    if (delete(resp, user) > 0) {
                        break;
                    }
                }

                LocalDateTime startDateTime = LocalDateTime.now();
                int computedJourneyCount = 0;

                logger.info("Sur le point de calculer les trajets pour l'utilisateur '" + user.getEmail() + "'");
                Collection<Pair<LogRecording, Log>> userLogRecordings = journeyBusiness.getPendingLogRecordings(user);
                logger.info("Nombre de LogRecording trouve pour l'utilisateur '" + user.getEmail() + "' : " + userLogRecordings.size() + "'");

                for (Pair<LogRecording, Log> logRecordingAndLog : userLogRecordings) {
                    LogRecording logRecording = logRecordingAndLog.getValue0();
                    Collection<Journey> journeys = JourneyUtils.getJourneys(logRecording, user.getKnownLocations());
                    logger.info("Nombre de trajets detectes pour le Log '" + logRecording.getName() + "' : " + journeys.size());
                    for (Journey journey : journeys) {
                        journeyBusiness.persist(journey, user, logRecordingAndLog.getValue1());
                        computedJourneyCount++;
                    }
                }

                JourneyCalculationJob journeyCalculationJob = new JourneyCalculationJob();
                journeyCalculationJob.setStartDateTime(startDateTime);
                journeyCalculationJob.setEndDateTime(LocalDateTime.now());
                journeyCalculationJob.setComputedJourneyCount(computedJourneyCount);
                journeyCalculationJob.setUser(user);
                journeyBusiness.persist(journeyCalculationJob);
            }

        } catch (Throwable t) {
            logger.error("Un erreur s'est produite lors du calcul des trajets", t);
        }
    }

    private int delete(HttpServletResponse resp, User user) throws IOException{
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        print("Deleting journey entities<br>", resp);
        Query query = new Query("JourneyEntity");
        List<Entity> journeyEntities = datastoreService.prepare(query).asList(FetchOptions.Builder.withLimit(500));
        print("Journey entities loaded (" + journeyEntities.size() + ")<br>", resp);

        for (Entity journeyEntity : journeyEntities) {
            JourneyEntity candidate = journeyBusiness.getJourney(journeyEntity.getKey().getName());
            if (candidate != null) {
                if (candidate.getUser().equals(user)) {
                    print("Deleting entity " + journeyEntity.getKey() + "'<br>", resp);

                    datastoreService.delete(journeyEntity.getKey());
                }
            } else logger.error("No journey for id '" + journeyEntity.getKey().getName() + "'");
        }
        print("Journey entities deleted<br>", resp);
        int result = datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults()).size();
        print(result + " to be deleted<br>", resp);
        return result;
    }

    private int getJourneyCount(DatastoreService datastoreService) {
        Query query = new Query("JourneyEntity");
        return datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults()).size();
    }

}
