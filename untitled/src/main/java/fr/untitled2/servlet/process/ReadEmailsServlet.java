package fr.untitled2.servlet.process;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.labs.repackaged.com.google.common.base.Predicate;
import com.google.appengine.labs.repackaged.com.google.common.collect.Collections2;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.LogBusiness;
import fr.untitled2.entities.PendingLog;
import fr.untitled2.entities.TrackPoint;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.User;
import fr.untitled2.transformers.GPXMapping;
import fr.untitled2.transformers.Transformer;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.JAXBUtils;
import fr.untitled2.utils.PersistenceUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/4/13
 * Time: 6:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReadEmailsServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(ReadEmailsServlet.class);

    private static Map<String, Transformer<String, Log>> folderFileNameRegexToTransformers = Maps.newHashMap();

    public static Transformer<String, Log> GPX_TRAILS_TRANSFORMER = new Transformer<String, Log>() {
        @Override
        public Log apply(User user, String from) throws Exception {
            StringBuilder xml = new StringBuilder();
            LineIterator lineIterator = new LineIterator(new StringReader(from));
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (StringUtils.indexOf(line, "<gpx") >= 0) {
                    line = "<gpx>" + StringUtils.substring(line, StringUtils.indexOf(line, ">") + 1);
                }
                while (StringUtils.indexOf(line, "<extensions>") > 0) {
                    line = StringUtils.substring(line, 0, StringUtils.indexOf(line, "<extensions>")) + StringUtils.substring(line, StringUtils.indexOf(line, "</extensions>") + "</extensions>".length());
                }
                xml.append(line);
            }
            GPXMapping gpxMapping = JAXBUtils.unmarshal(GPXMapping.class, xml.toString());
            Log log = new Log();
            for (GPXMapping.Trk trk : gpxMapping.getTracks()) {
                log.setName(trk.getName());
                for (GPXMapping.TrkSeg trkSeg : trk.getTrackSegments()) {
                    for (GPXMapping.TrkPt trkPt : trkSeg.getTrackPoints()) {
                        TrackPoint trackPoint = new TrackPoint();
                        trackPoint.setLongitude(trkPt.getLongitude());
                        trackPoint.setLatitude(trkPt.getLatitude());
                        trackPoint.setAltitude(trkPt.getAltitude());
                        trackPoint.setPointDate(trkPt.getDateTime().toDateTime(DateTimeZone.UTC).toLocalDateTime());
                        log.getTrackPoints().add(trackPoint);
                    }
                }
            }
            return log;
        }
    };

    static {
        folderFileNameRegexToTransformers.put(".*\\.gpx", GPX_TRAILS_TRANSFORMER);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Message incomingMessage = null;
        logger.info("Reception d'un nouveau message");
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            incomingMessage = new MimeMessage(session, req.getInputStream());
        } catch (Throwable t) {
            logger.error("Impossible de charger la liste des messages en attente", t);
            notifyError("Impossible de charger la liste des messages en attente", t);
            resp.sendError(500, "Impossible de charger la liste des messages en attente : " + Throwables.getStackTraceAsString(t));
            return;
        }
        logger.info("Le mime a ete extrait de la request");
        for (String fileNameRegex : folderFileNameRegexToTransformers.keySet()) {

            List<String> attachementStrings = Lists.newArrayList();
            try {
                attachementStrings.addAll(getAttachementsAsString(incomingMessage, fileNameRegex));
            } catch (Throwable t) {
                logger.error("Impossible de charger les pièces jointe d'un message", t);
                notifyError("Impossible de charger les pièces jointe d'un message", t);
            }
            logger.debug(attachementStrings.size() + " pieces jointes ont ete extraites");

            try {
                if (CollectionUtils.isEmpty(attachementStrings)) {
                    logger.info("Pas d'attachement");
                    MailService.Message message = new MailService.Message();
                    Object contentObject = incomingMessage.getContent();
                    String text = null;
                    if (contentObject instanceof String) {
                        text = (String) contentObject;

                    } else if (contentObject instanceof MimeMultipart) {
                        MimeMultipart mimeMultipart = (MimeMultipart) contentObject;
                        for (int index = 0; index < mimeMultipart.getCount(); index++) {
                            BodyPart bodyPart = mimeMultipart.getBodyPart(index);
                            if (bodyPart.getContent() instanceof String) {
                                text = (String) bodyPart.getContent();
                                break;
                            }
                        }
                    } else logger.info("Content '" + contentObject.getClass().getName() + "' n'est pas valide");

                    if (text != null) {
                        message.setSender("corentin.escoffier@gmail.com");
                        message.setTo("corentin.escoffier@gmail.com");
                        message.setSubject(incomingMessage.getSubject());
                        message.setTextBody(text);
                        MailServiceFactory.getMailService().send(message);
                    }
                    return;
                }
            } catch (Throwable t) {
                logger.error("Impossible de créer un message de transfert", t);
            }

            String emailAddress = null;

            try {
                for (Address address : incomingMessage.getFrom()) {
                    emailAddress = address.toString();
                    if (StringUtils.contains(emailAddress, "<") && StringUtils.contains(emailAddress, ">")) emailAddress = StringUtils.substring(emailAddress, StringUtils.indexOf(emailAddress, "<") + 1, StringUtils.indexOf(emailAddress, ">"));
                }
            } catch (Throwable t) {
                logger.error("Impossible de charger l'adresse de l'expéditeur", t);
                notifyError("Impossible de charger l'adresse de l'expéditeur", t);
                break;
            }
            logger.info("Loading user '" + emailAddress + "'");

            final String userEmailFilter = emailAddress;
            List<User> users = ObjectifyService.ofy().load().type(User.class).filter("email", emailAddress).list();
            if (CollectionUtils.isEmpty(users)) {
                logger.info("Utilisation du mode degrade pour le filtrage des users");
                users = Lists.newArrayList(Collections2.filter(ObjectifyService.ofy().load().type(User.class).list(), new Predicate<User>() {
                    @Override
                    public boolean apply(User user) {
                        return user.getEmail().equals(userEmailFilter);
                    }
                }));
            }
            User user = null;
            if (CollectionUtils.isNotEmpty(users)) user = users.get(0);
            if (user == null) {
                logger.info("L'utilisateur '" + emailAddress + "' n'existe pas");
                MailService.Message message = new MailService.Message();
                message.setTo(emailAddress);
                message.setSender("corentin.escoffier@gmail.com");
                message.setSubject("You are not registred");
                message.setTextBody("Hi,\n\nYou are not registred with this email address to MyPictureLog, you need to register before sending logs\n\nhttp://www.mypicturelog.com\n\nRegards\n\nMyPictureLogTeam");
                MailServiceFactory.getMailService().send(message);
                return;
            }
            logger.info("Des pieces jointes vont etre persistees pour l'utilisateur '" + emailAddress + "'. Nombre de pieces jointes : " + attachementStrings.size());
            for (String attachementString : attachementStrings) {
                Log log = null;
                try {
                    log = folderFileNameRegexToTransformers.get(fileNameRegex).apply(user, attachementString);
                    logger.debug("Le log a ete extrait de la piece jointe (" + emailAddress + ")");
                } catch (Throwable t) {
                    logger.error("Impossible d'extraire le log depuis les pieces jointes ", t);
                    notifyError(emailAddress, "File attachement format is incorrect", "Impossible d'extraire le log depuis les pieces jointes ", t);
                    break;
                }
                try {
                    log.setTimeZoneId(user.getTimeZoneId());
                    LogBusiness logBusiness = new LogBusiness();

                    Key<Log> key = logBusiness.persistLog(user, log);
                    PendingLog pendingLog = new PendingLog();
                    pendingLog.setTrip(log, key);

                    PersistenceUtils.persist(pendingLog);
                    notifyUser(emailAddress, getValidationMessageBody(pendingLog));
                    logger.info("Le log a ete persiste (" + emailAddress + ")");
                } catch (Throwable t) {
                    logger.error("Impossible de persister le log de l'utilisateur '" + emailAddress + "'", t);
                    notifyError(emailAddress, "File attachement format is incorrect", "Impossible de persister le log de l'utilisateur '" + emailAddress + "'", t);
                    break;
                }
            }
        }
    }

    private String getValidationMessageBody(PendingLog pendingLog) {
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("This is an auto generated message\n\n");

        bodyBuilder.append("You have sent a new log to MyPictureLog application\n\n");

        bodyBuilder.append("To be sure that you are the real sender of this email you need to validate this trip by clicking on the following link\n");
        bodyBuilder.append("http://application.mypicturelog.com/ihm/logs/validate-pending-log?key=").append(pendingLog.getIdentificationKey()).append("\n\n");

        bodyBuilder.append("You can see all your imported logs at the following address : http://application.mypicturelog.com/ihm/logs\n\n");

        bodyBuilder.append("Regards\n\n");
        bodyBuilder.append("MyPictureLogTeam (corentin.escoffier@mypicturelog.com)");

        return bodyBuilder.toString();

    }

    private void notifyUser(String userEmail, String messageString) {
        MailService.Message message = new MailService.Message("corentin.escoffier@gmail.com", userEmail, "Mail import information", messageString);
        try {
            MailServiceFactory.getMailService().send(message);
        } catch (Throwable t) {
            logger.error("Impossible d'envoyer un email", t);
        }
    }

    private void notifyError(String technicalMessage, Throwable t) {
        notifyError(null, null, technicalMessage, t);
    }

    private void notifyError(String userEmail, String functionnalMessage, String technicalMessage, Throwable t) {
        MailService mailService = MailServiceFactory.getMailService();

        MailService.Message adminMessage = new MailService.Message("corentin.escoffier@gmail.com", "corentin.escoffier@gmail.com", "Erreur technique", technicalMessage + "\n\n" + Throwables.getStackTraceAsString(t));

        MailService.Message userMessage = null;

        if (StringUtils.isNotEmpty(userEmail)) userMessage = new MailService.Message("corentin.escoffier@gmail.com", userEmail, "Erreur technique", functionnalMessage);
        try {
            mailService.send(adminMessage);
            if (userMessage != null) mailService.send(userMessage);
        } catch (Throwable throwable) {
            logger.error("Impossible d'envoyer un email", throwable);
        }

    }

    private List<String> getAttachementsAsString(Message message, String fileNameRegex) throws Exception {
        List<String> attachments = Lists.newArrayList();
        if (! (message.getContent() instanceof Multipart)) return Collections.EMPTY_LIST;

        Multipart multipart = (Multipart) message.getContent();

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            if(Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                if (StringUtils.isNotEmpty(bodyPart.getFileName()) && Pattern.compile(fileNameRegex).matcher(bodyPart.getFileName()).matches()) {
                    logger.info("Piece jointe trouvee : '" + bodyPart.getFileName() + "'");
                    InputStream is = bodyPart.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int bytesRead;
                    while((bytesRead = is.read(buf))!=-1) {
                        bos.write(buf, 0, bytesRead);
                    }
                    bos.close();
                    attachments.add(new String(bos.toByteArray(), Charset.forName("UTF-8")));
                } else {
                    logger.info("La piece jointe '" + bodyPart.getFileName() + "' ne matche pas '" + fileNameRegex + "'");
                }
            } else if (bodyPart.getContent() instanceof Multipart) {
                Multipart subBodyMultiPart = (Multipart) bodyPart.getContent();
                for (int index = 0; index < subBodyMultiPart.getCount(); index++) {
                    BodyPart subBodyPart = subBodyMultiPart.getBodyPart(index);

                    if(Part.ATTACHMENT.equalsIgnoreCase(subBodyPart.getDisposition())) {
                        if (StringUtils.isNotEmpty(subBodyPart.getFileName()) && Pattern.compile(fileNameRegex).matcher(subBodyPart.getFileName()).matches()) {
                            logger.info("Piece jointe trouvee : '" + subBodyPart.getFileName() + "'");
                            InputStream is = subBodyPart.getInputStream();
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            byte[] buf = new byte[4096];
                            int bytesRead;
                            while((bytesRead = is.read(buf))!=-1) {
                                bos.write(buf, 0, bytesRead);
                            }
                            bos.close();
                            attachments.add(new String(bos.toByteArray(), Charset.forName("UTF-8")));
                        } else {
                            logger.info("La piece jointe '" + subBodyPart.getFileName() + "' ne matche pas '" + fileNameRegex + "'");
                        }
                    }
                }
            }
        }
        return attachments;
    }

}
