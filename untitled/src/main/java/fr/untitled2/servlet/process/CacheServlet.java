package fr.untitled2.servlet.process;

import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.CacheHelper;
import fr.untitled2.business.ImageBusiness;
import fr.untitled2.business.MapBusiness;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.PictureMap;
import fr.untitled2.entities.User;
import org.joda.time.format.DateTimeFormat;
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
 * Date: 2/24/13
 * Time: 1:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class CacheServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(CacheServlet.class);

    private ImageBusiness imageBusiness = new ImageBusiness();

    private MapBusiness mapBusiness = new MapBusiness();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        cleanCache();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        cleanCache();
    }

    private void cleanCache() {
        Collection<User> users = ObjectifyService.ofy().load().type(User.class).list();
        for (User user : users) {
            logger.info("Suppression du cache pour '" + user.getUserId() + "'");
            CacheHelper.removeImageList(user, 0);

            imageBusiness.getImageList(0, user);
            for (PictureMap pictureMap : ObjectifyService.ofy().load().type(PictureMap.class).filter("user", user).list()) {
                logger.info("Suppression du cache map pour le user '" + user.getUserId() + "' et la map '" + pictureMap.getName() + "'");
                CacheHelper.removePictureMapImageList(pictureMap);
                mapBusiness.getMapMarkers(DateTimeFormat.forPattern(user.getDateFormat()), pictureMap.getSharingKey(), user);
            }

            for (Image image : ObjectifyService.ofy().load().type(Image.class).filter("user", user).list()) {
                CacheHelper.removeImageBelongsToMap(image);
                imageBusiness.isInMap(image);
            }

        }
    }

}
