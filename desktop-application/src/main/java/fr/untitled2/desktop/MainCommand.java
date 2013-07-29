package fr.untitled2.desktop;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.LocalisationUtils;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.JAXBUtils;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 6/26/13
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainCommand {

    private static final String configuration_file_name = ".myPictureLog";

    private static Collection<String> datePrefixes = Sets.newHashSet("Create Date: '"); /* , "Date Time Original: '" */

    private static DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy:MM:dd HH:mm:ss");

    private static String[] image_file_extensions = new String[]{"jpg", "JPG", "jpeg", "JPEG", "tiff", "TIFF", "nef", "NEF"};

    private Map<File, LocalDateTime> imageDates = Maps.newHashMap();

    private Configuration configuration;

    private File imageDir;

    public static void main(String[] args) throws OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException, JAXBException, ImageReadException {
        File configFile = new File(SystemUtils.USER_HOME, configuration_file_name);
        if (!configFile.exists()) {
            System.out.println("Condfiguration file does not exist. Wait until it is been built");
            AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient();
            String tokenValidationUrl = appEngineOAuthClient.getTokenValidationUrl();
            System.out.println("Copy the following link and paste it in a web browser : " + tokenValidationUrl + " (don't close your browser page now)");
            System.out.println("Click on the grant button and then please fill in the validation code that is displayed previous web page");
            byte[] key = new byte[256];
            System.in.read(key);
            String verificationCode = new String(key).trim();

            appEngineOAuthClient.validateTokens(verificationCode);

            Configuration configuration = new Configuration();
            configuration.setoAuthAccessKey(appEngineOAuthClient.getAccessToken());
            configuration.setoAuthAccessSecret(appEngineOAuthClient.getTokenSecret());

            JAXBUtils.marshal(configuration, configFile);
        }
        if (args.length < 1) {
            System.out.println("Usage MainCommand <PHOTO_DIR>");
            System.exit(-1);
        }

        File imageDir = new File(args[0]);

        if (!imageDir.isDirectory()) {
            System.err.println("Image dir is not a directory");
            System.exit(-1);
        }

        Configuration configuration = JAXBUtils.unmarshal(Configuration.class, configFile);

        MainCommand main = new MainCommand(configuration, imageDir);
        main.process();
    }

    public MainCommand(Configuration configuration, File imageDir) {
        this.configuration = configuration;
        this.imageDir = imageDir;
    }

    public void process() throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException, ImageReadException {
        System.out.println("Authenticating on remote server");
        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(configuration.getoAuthAccessKey(), configuration.getoAuthAccessSecret());

        System.out.println("Loading user preferences");
        UserPreferences userPreferences = appEngineOAuthClient.getUserPreferences();

        if (userPreferences == null) {
            System.err.println("User preferences are not set up yet. Please connect first to http://application.mypicturelog.com/ihm/");
            System.exit(-1);
        }

        Pair<LocalDateTime, LocalDateTime> startEnd = getStartAndEndDates(imageDir, userPreferences);
        if (startEnd != null) System.out.println("Min image date '" + startEnd.getValue0() + "', max image date '" + startEnd.getValue1() + "'");

        System.out.println("Loading log recording corresponding to your photos");
        Collection<LogRecording> logRecordings = appEngineOAuthClient.getMatchingLogRecordings(startEnd.getValue0(), startEnd.getValue1());
        if (CollectionUtils.isNotEmpty(logRecordings)) System.out.println("Found " + logRecordings.size() + " matching log recordings");
        else {
            System.out.println("No log recording are matching your photos");
            return;
        }

        Collection<File> imageFiles = imageDates.keySet();

        for (File imageFile : imageFiles) {
            Triplet<Double, Double, String> localisations = LocalisationUtils.getImagePosition(imageDates.get(imageFile), logRecordings);
            if (localisations != null) {
                System.out.println("File '" + imageFile + "' with date " + imageDates.get(imageFile) + " is located at (" + localisations.getValue0() + ", " + localisations.getValue1() + ", " + localisations.getValue2() + ")");
                try {
                    addGPSInfosToExif(imageFile, localisations.getValue0(), localisations.getValue1());
                } catch (Throwable t) {
                    System.err.println("An error has occured while updating exif meta data on file '" + imageFile + "'");
                    t.printStackTrace();
                }
            } else {
                System.out.println("File '" + imageFile + "' with date " + imageDates.get(imageFile) + " is not located");
            }
        }
    }

    private Pair<LocalDateTime, LocalDateTime> getStartAndEndDates(File imageDir, UserPreferences userPreferences) throws IOException, ImageReadException {
        Collection<File> imageFiles = FileUtils.listFiles(imageDir, image_file_extensions, true);
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = null;
        for (File imageFile : imageFiles) {
            IImageMetadata metdata = Sanselan.getMetadata(imageFile);
            if (metdata != null) {
                Collection<IImageMetadata.IImageMetadataItem> items = metdata.getItems();
                for (IImageMetadata.IImageMetadataItem item : items) {


                    String itemString = item.toString();
                    for (String datePrefix : datePrefixes) {
                        if (StringUtils.startsWith(itemString, datePrefix)) {
                            LocalDateTime localDateTime = dateTimeFormat.parseLocalDateTime(StringUtils.remove(StringUtils.remove(itemString, datePrefix), "'"));
                            LocalDateTime date = localDateTime.toDateTime(DateTimeZone.forID(userPreferences.getCameraDateTimeZone())).toDateTime(DateTimeZone.UTC).toLocalDateTime();
                            imageDates.put(imageFile, date);
                            if (date.isBefore(start)) start = date;
                            if (end == null || date.isAfter(end)) end = date;
                        }
                    }
                }
            }
        }
        return new Pair<LocalDateTime, LocalDateTime>(start, end);
    }

    private void addGPSInfosToExif(File file, double latitude, double longitude) throws IOException, ImageReadException, ImageWriteException {
        IImageMetadata readMetadata = Sanselan.getMetadata(file);
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) readMetadata;

        TiffImageMetadata readExif = jpegMetadata.getExif();
        TiffOutputSet writeExif = readExif.getOutputSet();

        writeExif.setGPSInDegrees(longitude, latitude);
        File tempOutputFile = File.createTempFile("mpl", "temp");
        FileOutputStream fos = new FileOutputStream(tempOutputFile);
        ExifRewriter exifRewriter = new ExifRewriter();
        exifRewriter.updateExifMetadataLossy(file, fos, writeExif);
        fos.close();
        FileUtils.deleteQuietly(file);
        FileUtils.moveFile(tempOutputFile, file);
    }

}
