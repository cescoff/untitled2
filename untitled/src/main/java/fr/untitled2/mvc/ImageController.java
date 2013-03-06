package fr.untitled2.mvc;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.ImageBusiness;
import fr.untitled2.business.beans.ImageFilterForm;
import fr.untitled2.business.beans.ImageList;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.ImageDisplayMode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 6:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class ImageController extends AuthenticatedController implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private ImageBusiness imageBusiness;

    @RequestMapping(value = "/images")
    public String home(Model model) {
        return list("0", model);
    }

    @RequestMapping(value = "/images/list", method = RequestMethod.GET)
    public String list(@RequestParam("pageNumber") String pageNumber, Model model) {
        try {
            int pageNr = 0;
            if (StringUtils.isNotEmpty(pageNumber)) pageNr = Integer.parseInt(pageNumber);
            ImageList imageList = imageBusiness.getImageList(pageNr, getUser());
            if (imageList != null) model.addAttribute(MVCConstants.image_list_attribute, imageList);
        } catch (Throwable t) {
            logger.error("Une erreur s'est produite lors du chargement de la page", t);
            return MVCConstants.error_view;
        }

        ImageFilterForm imageFilterForm = new ImageFilterForm();
        imageFilterForm.setDateStart(LocalDate.now().minusMonths(2));
        imageFilterForm.setDateEnd(LocalDate.now().minusMonths(1));
        model.addAttribute(MVCConstants.image_form_attribute, imageFilterForm);

        return MVCConstants.image_list_view;
    }

    @RequestMapping(value = "/images/list", method = RequestMethod.POST)
    public String list(@RequestParam("dateStart") String dateStart, @RequestParam("dateEnd") String dateEnd, Model model) {
        try {
            User user = getUser();

            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(user.getDateFormat());

            LocalDate start = dateTimeFormatter.parseLocalDate(dateStart);
            LocalDate end = dateTimeFormatter.parseLocalDate(dateEnd);

            ImageList imageList = imageBusiness.getImageList(start, end, user);
            if (imageList != null) model.addAttribute(MVCConstants.image_list_attribute, imageList);

            ImageFilterForm imageFilterForm = new ImageFilterForm();
            imageFilterForm.setDateStart(start);
            imageFilterForm.setDateEnd(end);
            model.addAttribute(MVCConstants.image_form_attribute, imageFilterForm);

            return MVCConstants.image_list_view;
        } catch (Throwable t) {
            logger.error("Une erreur s'est produite lors du chargement des images", t);
            return MVCConstants.error_view;
        }
    }

    @RequestMapping(value = "/images/view", method = RequestMethod.GET)
    public void displayImage(@RequestParam("imageKey") String imageKey, @RequestParam("displayMode") String displayMode, HttpServletResponse response) throws IOException {
        try {
            ImageDisplayMode imageDisplayMode = ImageDisplayMode.low;
            int displayModeCode = Integer.parseInt(displayMode);
            for (ImageDisplayMode mode : ImageDisplayMode.values()) {
                if (displayModeCode == mode.getCode()) imageDisplayMode = mode;
            }

            final Image image = ObjectifyService.ofy().load().key(Key.create(Image.class, imageKey)).get();

            if (image == null) {
                logger.error("Aucune image ne correspond a l'id " + imageKey);
                response.sendError(404, "Image not found");
                return;
            }

            if (!image.isReady()) {
                logger.error("Image non prete pour l'id " + imageKey);
                response.sendError(404, "Image not ready");
                return;
            }

            // Security check
            if (!imageBusiness.isInMap(image)) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Object principal = authentication.getPrincipal();
                logger.info("Principal:" + principal);

                if (!(principal instanceof User) || principal == null) {
                    response.sendError(403, "This image is not public");
                    return;
                } else if (principal instanceof User) {
                    if (!getUser().getUserId().equals(image.getUser().getUserId())) {
                        logger.info("L'image appartient a un autre utilisateur : " + getUser().getUserId() + "<->" + image.getUser().getUserId());
                        response.sendError(403, "This image belongs to another user (" + getUser().getUserId() + ")");
                        return;
                    }
                }
            }

            String fileNameWithoutExtension = StringUtils.substring(image.getName(), 0, StringUtils.lastIndexOf(image.getName(), "."));
            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
            if (image.getName().toLowerCase().endsWith("jpg")) response.setHeader("Content-Type", "image/jpg");
            else response.setHeader("Content-Type", "application/octet-stream");
            if (imageDisplayMode == ImageDisplayMode.low) {
                response.setContentType("image/jpg");
                response.setHeader("Content-Disposition", "attachment;filename=\"low_res_" + fileNameWithoutExtension + ".jpg\"");
                blobstoreService.serve(image.getLowResolutionPreview(), response);
            } else if (imageDisplayMode == ImageDisplayMode.lowSquare) {
                response.setContentType("image/jpg");
                response.setHeader("Content-Disposition", "attachment;filename=\"low_square_res_" + fileNameWithoutExtension + ".jpg\"");
                blobstoreService.serve(image.getSquareLowResolutionPreview(), response);
            } else if (imageDisplayMode == ImageDisplayMode.medium) {
                response.setContentType("image/jpg");
                response.setHeader("Content-Disposition", "attachment;filename=\"med_res_" + fileNameWithoutExtension + ".jpg\"");
                blobstoreService.serve(image.getMediumResolutionPreview(), response);
            } else if (imageDisplayMode == ImageDisplayMode.high) {
                response.setContentType("image/jpg");
                response.setHeader("Content-Disposition", "attachment;filename=\"high_res_" + fileNameWithoutExtension + ".jpg\"");
                blobstoreService.serve(image.getHighResolutionPreview(), response);
            } else if (imageDisplayMode == ImageDisplayMode.orginal) {
                response.setContentType("image/jpg");
                response.setHeader("Content-Disposition", "attachment;filename=\"" + image.getName() + "\"");
                blobstoreService.serve(new BlobKey(image.getImageKey()), response);
            }
        } catch (Throwable t) {
            logger.error("Une erreur s'est produite en servant une image", t);
            response.sendError(500, "An error has occured");
        }

    }

}
