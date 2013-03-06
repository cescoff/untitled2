package fr.untitled2.servlet.process;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.ImageConversionJob;
import fr.untitled2.servlet.ServletConstants;
import fr.untitled2.utils.IterablesUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/7/13
 * Time: 12:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileServeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Image image = ObjectifyService.ofy().load().key(Key.create(Image.class, req.getParameter(ServletConstants.blobstore_key_parameter))).get();
        Iterable<ImageConversionJob> imageConversionJobs = ObjectifyService.ofy().load().type(ImageConversionJob.class).filter("image", image);

        if (IterablesUtils.isNotEmpty(imageConversionJobs)) {
            BlobstoreServiceFactory.getBlobstoreService().serve(new BlobKey(image.getImageKey()), resp);
        } else {
            resp.sendError(403, "No conversion job in progress");
        }
    }
}
