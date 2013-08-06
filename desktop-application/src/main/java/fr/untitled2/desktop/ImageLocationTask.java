package fr.untitled2.desktop;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.LocalisationUtils;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.JAXBUtils;
import javafx.concurrent.Task;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
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

public class ImageLocationTask extends Task<Double> {

    private static Collection<String> datePrefixes = Sets.newHashSet("Create Date: '"); /* , "Date Time Original: '" */

    public static DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy:MM:dd HH:mm:ss");

    public static String[] image_file_extensions = new String[]{"jpg", "JPG", "jpeg", "JPEG", "tiff", "TIFF", "nef", "NEF"};

    private File imageDir;

    private Map<File, LocalDateTime> imageDates = Maps.newHashMap();

    private double maxProgress;

    public ImageLocationTask(double maxProgress) {
        this.maxProgress = maxProgress;
    }

    @Override
    protected Double call() throws Exception {
        double progress = 0.1;

        updateProgress(progress / maxProgress, maxProgress);
        updateMessage("Starting...");

        if (!imageDir.isDirectory()) {
            return 0.0;
        }

        try {
            process(getConfiguration(), progress);
        } catch (OAuthExpectationFailedException e) {
            e.printStackTrace();
        } catch (OAuthCommunicationException e) {
            e.printStackTrace();
        } catch (OAuthMessageSignerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageReadException e) {
            e.printStackTrace();
        }
        return 1.0;
    }

    public void process(Configuration configuration, double progress) throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException, ImageReadException {
        updateMessage("Authenticating on remote server");

        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(configuration.getoAuthAccessKey(), configuration.getoAuthAccessSecret());

        updateMessage("Loading user preferences");

        UserPreferences userPreferences = appEngineOAuthClient.getUserPreferences();
        progress += 0.1;

        updateProgress(progress / maxProgress, maxProgress);

        if (userPreferences == null) {
            updateMessage("User preferences are not set up yet. Please connect first to http://application.mypicturelog.com/ihm/");
        }

        Pair<LocalDateTime, LocalDateTime> startEnd = getStartAndEndDates(imageDir, userPreferences);
        if (startEnd != null) updateMessage("Min image date '" + startEnd.getValue0() + "', max image date '" + startEnd.getValue1() + "'");

        updateMessage("Loading log recording corresponding to your photos");

        Collection<LogRecording> logRecordings = appEngineOAuthClient.getMatchingLogRecordings(startEnd.getValue0(), startEnd.getValue1());
        if (CollectionUtils.isNotEmpty(logRecordings)) updateMessage("Found " + logRecordings.size() + " matching log recordings");
        else {
            updateMessage("No log recording are matching your photos");
            return;
        }

        Collection<File> imageFiles = imageDates.keySet();

        double progressForImage = (maxProgress - progress) / imageFiles.size();
        for (File imageFile : imageFiles) {
            Triplet<Double, Double, String> localisations = LocalisationUtils.getImagePosition(imageDates.get(imageFile), logRecordings);
            if (localisations != null) {
                updateMessage("File " + imageFile.getName() + " is located");

                try {
                    addGPSInfosToExif(imageFile, localisations.getValue0(), localisations.getValue1());
                } catch (Throwable t) {
                    updateMessage("An error has occured while updating exif meta data on file '" + imageFile + "'");
                    updateMessage(Throwables.getStackTraceAsString(t));
                }
            } else {
                updateMessage("File " + imageFile.getName() + " is not located");
            }
            progress+=progressForImage;

            updateProgress(progress / maxProgress, maxProgress);
            if (maxProgress <= 1.0) updateMessage("Finished !!");
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

        if (readMetadata instanceof JpegImageMetadata) {
            TiffOutputSet writeExif = null;
            if (readMetadata instanceof JpegImageMetadata) {
                TiffImageMetadata readExif = ((JpegImageMetadata) readMetadata).getExif();
                writeExif = readExif.getOutputSet();
            } else if (readMetadata instanceof TiffImageMetadata) {
                writeExif = ((TiffImageMetadata) readMetadata).getOutputSet();
            }

            writeExif.setGPSInDegrees(longitude, latitude);
            File tempOutputFile = File.createTempFile("mpl", "temp");
            FileOutputStream fos = new FileOutputStream(tempOutputFile);

            ExifRewriter exifRewriter = new ExifRewriter();
            exifRewriter.updateExifMetadataLossy(file, fos, writeExif);
            fos.close();
            FileUtils.deleteQuietly(file);
            FileUtils.moveFile(tempOutputFile, file);
        } else if (readMetadata instanceof TiffImageMetadata) {
            if (file.getName().endsWith("NEF") || file.getName().endsWith("nef")) {
                if (SystemUtils.IS_OS_MAC_OSX) {

                    File tempFile = new File(file.getParentFile(), "temp.nef");
                    FileUtils.moveFile(file, tempFile);
                    String commandLine = "/usr/bin/exiftool";

                    CommandLine cmdLine = CommandLine.parse(commandLine);
                    cmdLine.addArgument("-exif:gpslatitude=" + latitude);
                    cmdLine.addArgument("-exif:gpslongitude=" + longitude);
                    cmdLine.addArgument("-xmp:gpslatitude=" + latitude);
                    cmdLine.addArgument("-xmp:gpslongitude=" + longitude);
                    cmdLine.addArgument(StringUtils.replace(tempFile.getPath(), " ", "\\ "), true);

                    DefaultExecutor executor = new DefaultExecutor();
                    int exitValue = executor.execute(cmdLine);
                    FileUtils.moveFile(tempFile, file);
                    if (exitValue != 0) throw new IOException("Error updating exif meta data for file '" + file + "'");
                    File originalFile = new File(file.getParentFile(), "temp.nef_original") ;
                    FileUtils.deleteQuietly(originalFile);
                }
            }
        }
    }

    public File getImageDir() {
        return imageDir;
    }

    public void setImageDir(File imageDir) {
        this.imageDir = imageDir;
    }

    protected Configuration getConfiguration() {
        File configFile = new File(SystemUtils.USER_HOME, Main.configuration_file_name);
        if (!configFile.exists()) {
            return null;
        }
        try {
            return JAXBUtils.unmarshal(Configuration.class, configFile);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
