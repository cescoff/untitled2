package fr.untitled2.servlet;

import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.PhotoGallery;
import fr.untitled2.entities.*;

import javax.servlet.http.HttpServlet;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/29/13
 * Time: 1:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class InitServlet extends HttpServlet {

    static {
        ObjectifyService.register(Log.class);
        ObjectifyService.register(User.class);
        ObjectifyService.register(PendingLog.class);
        ObjectifyService.register(Image.class);
        ObjectifyService.register(ImageConversionJob.class);
        ObjectifyService.register(PictureMap.class);
        ObjectifyService.register(UserConnection.class);
        ObjectifyService.register(FilmCounter.class);
        ObjectifyService.register(LogStatistics.class);
        ObjectifyService.register(LogPersistenceJob.class);
        ObjectifyService.register(LogTrackPoints.class);
        ObjectifyService.register(BatchServer.class);
        ObjectifyService.register(PendingBatchServer.class);
        ObjectifyService.register(File.class);
        ObjectifyService.register(BatchTask.class);
        ObjectifyService.register(Batchlet.class);
        ObjectifyService.register(Gallery.class);
    }

}
