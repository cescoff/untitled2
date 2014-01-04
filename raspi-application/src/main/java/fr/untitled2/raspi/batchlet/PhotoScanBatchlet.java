package fr.untitled2.raspi.batchlet;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.entities.raspi.FileRef;
import fr.untitled2.common.entities.raspi.ImageTransformationTask;
import fr.untitled2.common.entities.raspi.PhotoGallery;
import fr.untitled2.common.entities.raspi.ServerConfig;
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.GeoLocalisationUtils;
import fr.untitled2.raspi.api.BatchContext;
import fr.untitled2.raspi.api.MasterBatchlet;
import fr.untitled2.raspi.utils.CommandLineUtils;
import fr.untitled2.utils.CollectionUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhotoScanBatchlet extends MasterBatchlet {

    private static final Logger logger = LoggerFactory.getLogger(PhotoScanBatchlet.class);

    private static Collection<String> datePrefixes = Sets.newHashSet("Create Date: '"); /* , "Date Time Original: '" */

    public static DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy:MM:dd HH:mm:ss");

    private File removableDevicesDir;

    @Override
    public void init() throws Exception {
        if (SystemUtils.IS_OS_MAC_OSX) {
            removableDevicesDir = new File("/Volumes");
        } else if (SystemUtils.IS_OS_LINUX) {
            removableDevicesDir = new File("/media");
        }
    }

    @Override
    public boolean isThreadSafe() {
        return false;
    }

    @Override
    public void execute() throws Exception {
        UserPreferences userPreferences = getBatchContext().getUserPreferences();
        Collection<File> images = Lists.newArrayList();
        Map<File, String> fileDriveNames = Maps.newHashMap();
        if (removableDevicesDir != null) {
            File[] diskNames = removableDevicesDir.listFiles();
            if (diskNames != null) {
                for (int indexInDiskNames = 0; indexInDiskNames < diskNames.length;indexInDiskNames++) {
                    File diskName = diskNames[indexInDiskNames];
                    if (diskName != null) {
                        File[] diskRootDirs = diskName.listFiles();
                        if (diskRootDirs != null) {
                            for (int indexInDiskRoots = 0; indexInDiskRoots < diskRootDirs.length; indexInDiskRoots++) {
                                File diskRootDir = diskRootDirs[indexInDiskRoots];
                                if (diskRootDir != null) {
                                    if (diskRootDir.getName().equalsIgnoreCase("dcim")) {
                                        logger.info("Found image dir '" + diskRootDir + "'");
                                        for (File imageFile : FileUtils.listFiles(diskRootDir, new String[]{"JPG", "jpg", "jpeg", "JPEG", "NEF", "nef"}, true)) {
                                            fileDriveNames.put(imageFile, diskName.getName());
                                            images.add(imageFile);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(images)) {
            logger.info("No files to upload");
            return;
        }

        Pair<LocalDateTime, LocalDateTime> startEnd = getStartAndEndDates(images, userPreferences, getBatchContext());

        if (startEnd != null)
            logger.info("Min image date '" + startEnd.getValue0() + "', max image date '" + startEnd.getValue1() + "'");

        logger.info("Loading log recording corresponding to your photos");

        ServerConfig serverConfig = CommandLineUtils.getServerConfig();
        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(serverConfig.getAccessKey(), serverConfig.getAccessSecret());
        Collection<LogRecording> logRecordings = appEngineOAuthClient.getMatchingLogRecordings(startEnd.getValue0(), startEnd.getValue1());
        if (CollectionUtils.isNotEmpty(logRecordings))
            logger.info("Found " + logRecordings.size() + " matching log recordings");
        else {
            logger.info("No log recording are matching your photos");
        }

        Collection<File> errorFiles = Lists.newArrayList();

        File tempDir = getBatchContext().createTempDir();
        Collection<File> geolocalizedFiles = Lists.newArrayList();

        for (File imageFile : images) {
            try {
                Triplet<Double, Double, String> localisations = GeoLocalisationUtils.getImagePosition(getImageDate(imageFile, userPreferences), logRecordings);
                if (localisations != null) {
                    logger.info("File " + imageFile.getPath() + " is located");

                    try {
                        File driveTempDir = new File(tempDir, fileDriveNames.get(imageFile));
                        if (!driveTempDir.exists()) driveTempDir.mkdirs();
                        File geolocalizedFile = new File(driveTempDir, imageFile.getName());

                        FileUtils.copyFile(imageFile, geolocalizedFile);

                        addGPSInfosToExif(geolocalizedFile, localisations.getValue0(), localisations.getValue1());
                        geolocalizedFiles.add(geolocalizedFile);
                    } catch (Throwable t) {
                        logger.error("An error has occured while updating exif meta data on file '" + imageFile + "'", t);
                        errorFiles.add(imageFile);
                    }
                } else {
                    logger.info("File " + imageFile.getName() + " is not located");
                }
            } catch (Throwable t) {
                getBatchContext().logError("An unexpected error has occured", t);
                logger.error("An unexpected error has occured", t);
                errorFiles.add(imageFile);
            }
        }

        PhotoGallery photoGallery = new PhotoGallery();

        for (File image : geolocalizedFiles) {
            logger.info("Uploading file '" + image + "'");
            FileRef fileRef = getBatchContext().pushRemoteFile(image);
            photoGallery.getOriginalFiles().add(fileRef);
            logger.info("File '" + image + "' uploaded->'" + fileRef.getId() + "'");
            try {
                FileUtils.deleteQuietly(image);
            } catch (Throwable t) {
                logger.error("An error has occured while deleting file '" + image + "'");
            }
        }
        photoGallery = getBatchContext().executeRemoteCommand("pushPhotoGallery", photoGallery, PhotoGallery.class);

        for (FileRef fileRef : photoGallery.getOriginalFiles()) {
            ImageTransformationTask imageTransformationTask = new ImageTransformationTask();
            imageTransformationTask.setGalleryId(photoGallery.getId());
            imageTransformationTask.setOriginalFile(fileRef);
            getBatchContext().createNewBatchTask(imageTransformationTask, ImageTransformBatchlet.class);
        }

        for (File image : images) {
            if (!errorFiles.contains(image)) {
                try {
                    FileUtils.deleteQuietly(image);
                } catch (Throwable t) {
                    logger.error("An error has occured while deleting file '" + image + "'");
                }
            } else {
                logger.info("Skipping error file '" + image + "'");
            }
        }

    }

    private Pair<LocalDateTime, LocalDateTime> getStartAndEndDates(Collection<File> imageFiles, UserPreferences userPreferences, BatchContext batchContext) throws IOException, ImageReadException {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = null;
        for (File imageFile : imageFiles) {
            try {
                LocalDateTime date = getImageDate(imageFile, userPreferences);
                if (date != null) {
                    if (date.isBefore(start)) start = date;
                    if (end == null || date.isAfter(end)) end = date;
                }
            } catch (Throwable t) {
                batchContext.logError("An unexpected error has occured", t);
            }
        }
        return new Pair<LocalDateTime, LocalDateTime>(start, end);
    }

    private LocalDateTime getImageDate(File imageFile, UserPreferences userPreferences) throws IOException, ImageReadException {
        IImageMetadata metdata = Sanselan.getMetadata(imageFile);
        if (metdata != null) {
            Collection<IImageMetadata.IImageMetadataItem> items = metdata.getItems();
            for (IImageMetadata.IImageMetadataItem item : items) {
                String itemString = item.toString();
                for (String datePrefix : datePrefixes) {
                    if (StringUtils.startsWith(itemString, datePrefix)) {
                        LocalDateTime localDateTime = dateTimeFormat.parseLocalDateTime(StringUtils.remove(StringUtils.remove(itemString, datePrefix), "'"));
                        return localDateTime.toDateTime(DateTimeZone.forID(userPreferences.getCameraDateTimeZone())).toDateTime(DateTimeZone.UTC).toLocalDateTime();
                    }
                }
            }
        }
        return null;
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

                    File tempFile = new File(FileUtils.getTempDirectory(), file.getName().replaceAll("\\s", "_"));
                    if (tempFile.exists()) FileUtils.deleteQuietly(tempFile);
                    String commandLine = "/usr/bin/exiftool";
                    FileUtils.copyFile(file, tempFile);
                    CommandLine cmdLine = CommandLine.parse(commandLine);
                    if (latitude < 0.0) {
                        cmdLine.addArgument("-exif:gpslatitude=" + Math.abs(latitude));
                        cmdLine.addArgument("-exif:gpslatituderef=S");
                    } else {
                        cmdLine.addArgument("-exif:gpslatitude=" + latitude);
                        cmdLine.addArgument("-exif:gpslatituderef=N");
                    }
                    if (longitude < 0.0) {
                        cmdLine.addArgument("-exif:gpslongitude=" + Math.abs(longitude) + "W");
                        cmdLine.addArgument("-exif:gpslongituderef=W");
                    } else {
                        cmdLine.addArgument("-exif:gpslongitude=" + longitude);
                        cmdLine.addArgument("-exif:gpslongituderef=E");
                    }

                    if (latitude < 0.0) {
                        cmdLine.addArgument("-xmp:gpslatitude=" + latitude);
                    } else {
                        cmdLine.addArgument("-xmp:gpslatitude=" + latitude);
                    }
                    if (longitude < 0.0) {
                        cmdLine.addArgument("-xmp:gpslongitude=" + longitude);
                    } else {
                        cmdLine.addArgument("-xmp:gpslongitude=" + longitude);
                    }
                    cmdLine.addArgument(StringUtils.replace(tempFile.getPath(), " ", "\\ "), true);

                    DefaultExecutor executor = new DefaultExecutor();
                    int exitValue = executor.execute(cmdLine);
                    FileUtils.deleteQuietly(file);
                    FileUtils.moveFile(tempFile, file);
                    if (exitValue != 0) throw new IOException("Error updating exif meta data for file '" + file + "'");
                    File originalFile = new File(file.getParentFile(), "temp.nef_original") ;
                    FileUtils.deleteQuietly(originalFile);
                }
            }
        }
    }

}
