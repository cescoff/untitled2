package fr.untitled2.mvc;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.*;
import fr.untitled2.entities.Log;
import fr.untitled2.servlet.process.ReadEmailsServlet;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.PersistenceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.nio.channels.Channels;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/logs/add")
public class LogUploadController extends AuthenticatedController {

    private static Logger logger = LoggerFactory.getLogger(ImageUploadController.class);

    @ModelAttribute
    public void ajaxAttribute(WebRequest request, Model model) {
        model.addAttribute("ajaxRequest", AjaxUtils.isAjaxRequest(request));
    }

    @RequestMapping(method= RequestMethod.GET)
    public String fileUploadForm() {
        return MVCConstants.log_add_view;
    }

    @RequestMapping(method= RequestMethod.POST)
    public String processUpload(@RequestParam("fileType") String fileType, @RequestParam MultipartFile file, Model model) throws Exception {
        String tripString = null;
        FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile appEngineFile = fileService.getBlobFile(new BlobKey(file.getOriginalFilename()));

        FileReadChannel fileReadChannel = fileService.openReadChannel(appEngineFile, true);

        tripString = IOUtils.toString(Channels.newInputStream(fileReadChannel));
        fileReadChannel.close();

        logger.info("Type de fichier selectionne : " + fileType);
        if (fileType != null) {
            if (StringUtils.isNotEmpty(tripString)) {
                Log log = null;
                if ("mpl".equals(fileType)) log = JSonUtils.readJson(Log.class, tripString);
                else if ("gpx".equals(fileType)) log = ReadEmailsServlet.GPX_TRAILS_TRANSFORMER.apply(getUser(), tripString);
                else if ("kml".equals(fileType)) throw new Exception("KML is not yet supported");
                log.setValidated(true);
                PersistenceUtils.persistTrip(log, getUser());
            } else logger.error("No trip string for type '" + fileType + "'");
        }
        model.addAttribute("message", "File uploaded successfully");
        return MVCConstants.log_add_view;
    }

}
