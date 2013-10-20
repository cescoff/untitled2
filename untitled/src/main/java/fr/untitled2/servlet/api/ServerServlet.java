package fr.untitled2.servlet.api;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.com.google.common.base.Throwables;
import com.google.appengine.tools.cloudstorage.*;
import com.google.common.collect.Maps;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.raspi.FileRef;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.File;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.api.command.*;
import fr.untitled2.servlet.api.command.batchlet.GetBatchTaskLogs;
import fr.untitled2.servlet.api.command.batchlet.GetServerBatchTasks;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.SignUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/26/13
 * Time: 10:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerServlet extends HttpServlet {

    private static final String URI_PREFIX = "/api/server/";

    private static final String PUSH_FILE_PATH = URI_PREFIX + "files/push";

    private static final String PUSH_LARGE_FILE_PATH = URI_PREFIX + "files/pushLarge";

    private static final String GET_FILE_PATH = URI_PREFIX + "files/get";

    private static final String GET_LARGE_FILE_PATH = URI_PREFIX + "files/getLarge";

    private static final String FILE_ID_REQ_PARAMETER = "fileId";

    private static final String FILE_PART_POSITION_REQ_PARAMETER = "filePartPosition";

    private static final String FILE_PART_COUNT_REQ_PARAMETER = "filePartCount";

    private static Logger logger = LoggerFactory.getLogger(ServerServlet.class);

    private static Map<String, Command> commands = Maps.newHashMap();

    private static final String GCS_BUCKET_NAME = "mpl-cloud";

    static {
        commands.put("registerServer", new RegisterBatchServer());
        commands.put("pushStatistics", new StatisticsRegister());
        commands.put("attacheServer", new AttachServer());
        commands.put("serverList", new GetServers());
        commands.put("pushVerificationCode", new PushVerificationCode());
        commands.put("getNextBatchTask", new GetNextBatchTask());
        commands.put("markBatchTaskAsDone", new MarkBatchTaskAsDone());
        commands.put("registerBatchTask", new RegisterBatchTask());
        commands.put("batchletManager", new BatchletManager());
        commands.put("pushPhotoGallery", new PushPhotoGallery());
        commands.put("addPhotoGallery", new AddPhotoGallery());
        commands.put("getBatchTaskLogs", new GetBatchTaskLogs());
        commands.put("getServerBatchTasks", new GetServerBatchTasks());
        commands.put("getGalleries", new GetGalleries());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().indexOf(GET_LARGE_FILE_PATH) >= 0) {
            if (StringUtils.isNotEmpty(req.getParameter(FILE_ID_REQ_PARAMETER)) && StringUtils.isNotEmpty(req.getParameter(FILE_PART_POSITION_REQ_PARAMETER))) {
                int filePartPosition = -1;
                try {
                    filePartPosition = Integer.parseInt(req.getParameter(FILE_PART_POSITION_REQ_PARAMETER));
                } catch (NumberFormatException nfe) {
                    resp.sendError(500, "Parameter '" + FILE_PART_POSITION_REQ_PARAMETER + "' is not a valid integer");
                    return;
                }
                try {
                    File file = ObjectifyService.ofy().load().key(Key.create(File.class, req.getParameter(FILE_ID_REQ_PARAMETER))).get();
                    InputStream inputStream = getLargeFilePart(getUser(resp), file, filePartPosition);
                    IOUtils.copy(inputStream, resp.getOutputStream());
                } catch (Throwable t) {
                    logger.error("Enable to load file with id '" + req.getParameter(FILE_ID_REQ_PARAMETER), t);
                    resp.sendError(500, Throwables.getStackTraceAsString(t));
                    return;
                }
            } else {
                resp.sendError(404, "Missing required parameter '" + FILE_ID_REQ_PARAMETER + "' or '" + FILE_PART_POSITION_REQ_PARAMETER + "'");
            }
            return;
        }

        if (req.getRequestURI().indexOf(GET_FILE_PATH) >= 0) {
            if (StringUtils.isNotEmpty(req.getParameter(FILE_ID_REQ_PARAMETER))) {
                try {
                    File file = ObjectifyService.ofy().load().key(Key.create(File.class, req.getParameter(FILE_ID_REQ_PARAMETER))).get();
                    InputStream inputStream = getFile(getUser(resp), file);
                    IOUtils.copy(inputStream, resp.getOutputStream());
                } catch (Throwable t) {
                    logger.error("Enable to load file with id '" + req.getParameter(FILE_ID_REQ_PARAMETER), t);
                    resp.sendError(500, Throwables.getStackTraceAsString(t));
                    return;
                }
            } else {
                resp.sendError(404, "Missing required parameter '" + FILE_ID_REQ_PARAMETER + "'");
                return;
            }
            return;
        }

        Command command = getCommand(req);
        if (command == null) {
            resp.sendError(404, "Command not found");
            return;
        }
        if (command.isPublic()) {
            try {
                String status = command.executeStatus();
                if (status != null) resp.getOutputStream().write(status.getBytes());
            } catch (Throwable t) {
                logger.error("An error has occured", t);
                resp.sendError(500, "An error has occured " + Throwables.getStackTraceAsString(t));
            }
        } else {
            try {
                String result = command.executeStatus(getUser(resp));
                if (result != null) resp.getOutputStream().write(result.getBytes());
            } catch (Throwable t) {
                logger.error("An error has occured while executing command '" + command.getClass().getName() + "'", t);
                resp.sendError(500, "An error has occured " + Throwables.getStackTraceAsString(t));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().indexOf(PUSH_LARGE_FILE_PATH) >= 0) {
            int filePartPosition = -1;
            int filePartCount = -1;
            if (StringUtils.isNotEmpty(req.getParameter(FILE_PART_POSITION_REQ_PARAMETER))) {
                try {
                    filePartPosition = Integer.parseInt(req.getParameter(FILE_PART_POSITION_REQ_PARAMETER));
                } catch (NumberFormatException nfe) {
                    resp.sendError(500, "Parameter '" + FILE_PART_POSITION_REQ_PARAMETER + "' is not a valid integer");
                    return;
                }
            }
            if (StringUtils.isNotEmpty(req.getParameter(FILE_PART_COUNT_REQ_PARAMETER))) {
                try {
                    filePartCount = Integer.parseInt(req.getParameter(FILE_PART_COUNT_REQ_PARAMETER));
                } catch (NumberFormatException nfe) {
                    resp.sendError(500, "Parameter '" + FILE_PART_COUNT_REQ_PARAMETER + "' is not a valid integer");
                    return;
                }
            }
            String filePath = StringUtils.remove(req.getRequestURI(), PUSH_LARGE_FILE_PATH + "/");
            if (StringUtils.isEmpty(filePath)) {
                logger.error("No file path provided");
                resp.sendError(500, "NO FILE PATH PROVIDED");
            }

            logger.info("Pushing file large with path '" + filePath + "'");
            if (filePartPosition >= 0) {
                logger.info("Pushing large file with position '" + filePartPosition + "'");
                try {
                    User user = getUser(resp);
                    pushLargeFilePart(user, filePath, filePartPosition, req.getInputStream());
                    SimpleResponse simpleResponse = new SimpleResponse(true);
                    resp.getOutputStream().write(JSonUtils.writeJson(simpleResponse).getBytes());
                } catch (Throwable t) {
                    logger.error("An error has occured whild pushing file '" + filePath + "'", t);
                    resp.sendError(500, Throwables.getStackTraceAsString(t));
                }
            } else if (filePartCount > 0) {
                logger.info("Generating a new large file reference with split length " + filePartCount);
                try {
                    FileRef fileRef = getLargeFileRef(getUser(resp), filePath, filePartCount);
                    resp.getOutputStream().write(JSonUtils.writeJson(fileRef).getBytes());
                } catch (Throwable t) {
                    logger.error("An error has occured whild pushing file '" + filePath + "'", t);
                    resp.sendError(500, Throwables.getStackTraceAsString(t));
                }
            }

            return;
        }

        if (req.getRequestURI().indexOf(PUSH_FILE_PATH) >= 0) {
            String filePath = StringUtils.remove(req.getRequestURI(), PUSH_FILE_PATH + "/");
            if (StringUtils.isEmpty(filePath)) {
                logger.error("No file path provided");
                resp.sendError(500, "NO FILE PATH PROVIDED");
            }

            logger.info("Pushing file with path '" + filePath + "'");
            try {
                User user = getUser(resp);
                FileRef fileRef = pushFile(user, filePath, req.getInputStream());
                resp.getOutputStream().write(JSonUtils.writeJson(fileRef).getBytes());
            } catch (Throwable t) {
                logger.error("An error has occured whild pushing file '" + filePath + "'", t);
                resp.sendError(500, Throwables.getStackTraceAsString(t));
            }

            return;
        }

        Command command = getCommand(req);
        if (command == null) {
            resp.sendError(404, "Command not found");
            return;
        }

        if (command.isPublic()) {
            String json = IOUtils.toString(req.getInputStream());
            logger.info("JSON:'" + json + "'");
            try {
                String result = command.execute(json, req.getRemoteAddr());
                if (result != null) resp.getOutputStream().write(result.getBytes());
            } catch (Throwable t) {
                logger.error("An error has occured while executing command '" + command.getClass().getName() + "'", t);
                resp.sendError(500, "An error has occured");
            }
        } else {
            String json = IOUtils.toString(req.getInputStream());
            logger.info("JSON:'" + json + "'");
            try {
                String result = command.execute(json, getUser(resp), req.getRemoteAddr());
                if (result != null) {
                    logger.info("Found ");
                    resp.getOutputStream().write(result.getBytes());
                }

            } catch (Throwable t) {
                logger.error("An error has occured while executing command '" + command.getClass().getName() + "'", t);
                resp.sendError(500, "An error has occured");
            }

        }


    }

    private User getUser(HttpServletResponse resp) throws Exception {
        com.google.appengine.api.users.User user = UserServiceFactory.getUserService().getCurrentUser();

        if (user == null) {
            try {
                user = OAuthServiceFactory.getOAuthService().getCurrentUser();
            } catch (OAuthRequestException e) {
                logger.error("Impossible d'authentifier l'utilisateur", e);
            }
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
        if (applicationUser == null) {
            logger.error("Impossible de trouver l'utilisateur '" + user.getEmail() + "'");
            resp.sendError(403, "Not registred");
        }
        return applicationUser;
    }

    private Command getCommand(HttpServletRequest request) {
        return commands.get(getCommandName(request));
    }

    private String getCommandName(HttpServletRequest request) {
        logger.info("URI->'" + request.getRequestURI() + "'");
        return StringUtils.remove(request.getRequestURI(), URI_PREFIX);
    }

    private InputStream getFile(User user, File file) throws IOException {
        if (file == null) throw new FileNotFoundException("The file does not exist");
        if (file.isLargeFile()) throw new IOException("Large file, this method is not supported");
        if (!user.equals(file.getUser())) throw new IOException("You are not the owner of file '" + file.getGsFilePath() + "'");
        GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        GcsFilename gcsFilename = new GcsFilename(GCS_BUCKET_NAME, file.getGsFilePath());
        GcsInputChannel gcsInputChannel = gcsService.openReadChannel(gcsFilename, 0);
        return Channels.newInputStream(gcsInputChannel);
    }

    private InputStream getLargeFilePart(User user, File file, int partPosition) throws IOException {
        if (file == null) throw new FileNotFoundException("The file does not exist");
        if (!file.isLargeFile()) throw new IOException("Large file, this method is not supported");
        if (!user.equals(file.getUser())) throw new IOException("You are not the owner of file '" + file.getGsFilePath() + "'");
        GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        String filePath = file.getGsFilePath() + "." + partPosition;
        logger.info("Loading file '" + filePath + "'");
        GcsFilename gcsFilename = new GcsFilename(GCS_BUCKET_NAME, filePath);
        GcsInputChannel gcsInputChannel = gcsService.openReadChannel(gcsFilename, 0);
        return Channels.newInputStream(gcsInputChannel);
    }

    private FileRef pushFile(User user, String fileName, InputStream inputStream) throws IOException {
        String id = SignUtils.calculateSha1Digest(user.getEmail() + fileName);
        GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        String filePath = user.getEmail() + "/" + fileName;
        GcsFilename gcsFilename = new GcsFilename(GCS_BUCKET_NAME, filePath);
        GcsOutputChannel gcsOutputChannel = gcsService.createOrReplace(gcsFilename, GcsFileOptions.getDefaultInstance());

        OutputStream outputStream = Channels.newOutputStream(gcsOutputChannel);
        IOUtils.copy(inputStream, outputStream);
        outputStream.close();
        gcsOutputChannel.close();
        File file = new File();
        file.setGsFilePath(filePath);
        file.setId(id);
        file.setUser(user);
        ObjectifyService.ofy().save().entity(file);

        FileRef fileRef = new FileRef();
        fileRef.setId(id);
        fileRef.setName(fileName);

        return fileRef;
    }

    private FileRef getLargeFileRef(User user, String fileName, int filePartCount) {
        String id = SignUtils.calculateSha1Digest(user.getEmail() + fileName);
        String filePath = user.getEmail() + "/" + fileName;
        File file = new File();
        file.setGsFilePath(filePath);
        file.setId(id);
        file.setUser(user);
        file.setFilePartCount(filePartCount);
        ObjectifyService.ofy().save().entity(file);

        FileRef fileRef = new FileRef();
        fileRef.setFilePartCount(filePartCount);
        fileRef.setName(fileName);
        fileRef.setId(id);
        return fileRef;
    }

    private void pushLargeFilePart(User user, String fileName, int partPosition, InputStream inputStream) throws IOException {
        String filePath = user.getEmail() + "/" + fileName + "." + partPosition;

        GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        GcsFilename gcsFilename = new GcsFilename(GCS_BUCKET_NAME, filePath);
        GcsOutputChannel gcsOutputChannel = gcsService.createOrReplace(gcsFilename, GcsFileOptions.getDefaultInstance());

        OutputStream outputStream = Channels.newOutputStream(gcsOutputChannel);
        IOUtils.copy(inputStream, outputStream);
        outputStream.close();
        gcsOutputChannel.close();
    }

}
