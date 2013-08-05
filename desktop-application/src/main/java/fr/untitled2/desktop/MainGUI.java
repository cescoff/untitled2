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
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
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
import org.apache.sanselan.formats.tiff.write.TiffImageWriterLossy;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class MainGUI extends JPanel implements ActionListener {

    private static final String configuration_file_name = ".myPictureLog";

    private static Collection<String> datePrefixes = Sets.newHashSet("Create Date: '"); /* , "Date Time Original: '" */

    private static DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy:MM:dd HH:mm:ss");

    private static String[] image_file_extensions = new String[]{"jpg", "JPG", "jpeg", "JPEG", "tiff", "TIFF", "nef", "NEF"};

    private Map<File, LocalDateTime> imageDates = Maps.newHashMap();

    private Configuration configuration;

    private File imageDir;

    JLabel welcomeMessage;
    JButton fileChooseButton;
    JFileChooser fileChooser;
    JTextArea log;


    public MainGUI() {
        super(new BorderLayout());
        welcomeMessage = new JLabel("Before you start using this little utility software be sure you are registred on the following site : http://application.mypicturelog.com");


        log = new JTextArea(30,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setLineWrap(true);
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        //Display the window.
        fileChooser = new JFileChooser(new File(SystemUtils.USER_HOME));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        fileChooseButton = new JButton("Choose your photo dir...");
        fileChooseButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(fileChooseButton);

        add(welcomeMessage, BorderLayout.PAGE_START);
        add(buttonPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.PAGE_END);
    }

    private void handleConfigurationFile() {
        File configFile = new File(SystemUtils.USER_HOME, configuration_file_name);
        if (!configFile.exists()) {
            try {
                handleConnexion(configFile);
            } catch (OAuthNotAuthorizedException e) {
                logMessage(Throwables.getStackTraceAsString(e));
            } catch (OAuthExpectationFailedException e) {
                logMessage(Throwables.getStackTraceAsString(e));
            } catch (OAuthCommunicationException e) {
                logMessage(Throwables.getStackTraceAsString(e));
            } catch (OAuthMessageSignerException e) {
                logMessage(Throwables.getStackTraceAsString(e));
            } catch (IOException e) {
                logMessage(Throwables.getStackTraceAsString(e));
            } catch (JAXBException e) {
                logMessage(Throwables.getStackTraceAsString(e));
            }
        } else {
            try {
                this.configuration = JAXBUtils.unmarshal(Configuration.class, configFile);
            } catch (JAXBException e) {
                logMessage(Throwables.getStackTraceAsString(e));
            } catch (IOException e) {
                logMessage(Throwables.getStackTraceAsString(e));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        int returnVal = fileChooser.showOpenDialog(MainGUI.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    runLocalisation();
                    logMessage("Operation finished");
                    return null;
                }
            }.execute();
        }
        log.setCaretPosition(log.getDocument().getLength());
    }

    private void runLocalisation() {
        setImageDir(fileChooser.getSelectedFile());
        if (!imageDir.isDirectory()) {
            logMessage("Image dir is not a directory");
        }
        try {
            process();
        } catch (OAuthExpectationFailedException e) {
            logMessage(Throwables.getStackTraceAsString(e));
        } catch (OAuthCommunicationException e) {
            logMessage(Throwables.getStackTraceAsString(e));
        } catch (OAuthMessageSignerException e) {
            logMessage(Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            logMessage(Throwables.getStackTraceAsString(e));
        } catch (ImageReadException e) {
            logMessage(Throwables.getStackTraceAsString(e));
        }
    }

    public void handleConnexion(final File configFile) throws OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException, JAXBException {
        logMessage("Configuration file does not exist. Wait until it is been built");

        final AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient();
        String tokenValidationUrl = appEngineOAuthClient.getTokenValidationUrl();

        final JFrame connexionPopup = new JFrame("Connexion");

        JPanel jPanel = new JPanel(new BorderLayout());
        JTextArea linkTextArea = new JTextArea(30,50);
        linkTextArea.setMargin(new Insets(5,5,5,5));
        linkTextArea.setText("Copy the following link and paste it in a web browser : \n" + tokenValidationUrl);
        linkTextArea.setLineWrap(true);
        linkTextArea.setEditable(false);

        final JTextArea validationCodeArea = new JTextArea("Validation code here");
        validationCodeArea.setMargin(new Insets(5,5,5,5));

        JButton validationButton = new JButton("Validate code");

        validationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    appEngineOAuthClient.validateTokens(validationCodeArea.getText());
                } catch (OAuthCommunicationException e) {
                    logMessage("An error has occured : \n" + Throwables.getStackTraceAsString(e));
                } catch (OAuthExpectationFailedException e) {
                    logMessage("An error has occured : \n" + Throwables.getStackTraceAsString(e));
                } catch (OAuthNotAuthorizedException e) {
                    logMessage("An error has occured : \n" + Throwables.getStackTraceAsString(e));
                } catch (OAuthMessageSignerException e) {
                    logMessage("An error has occured : \n" + Throwables.getStackTraceAsString(e));
                }

                configuration = new Configuration();
                configuration.setoAuthAccessKey(appEngineOAuthClient.getAccessToken());
                configuration.setoAuthAccessSecret(appEngineOAuthClient.getTokenSecret());

                try {
                    JAXBUtils.marshal(configuration, configFile);
                } catch (JAXBException e) {
                    logMessage("An error has occured : \n" + Throwables.getStackTraceAsString(e));
                } catch (IOException e) {
                    logMessage("An error has occured : \n" + Throwables.getStackTraceAsString(e));
                }
                connexionPopup.setVisible(false);
            }
        });

        jPanel.add(linkTextArea, BorderLayout.PAGE_START);
        jPanel.add(validationCodeArea, BorderLayout.CENTER);
        jPanel.add(validationButton, BorderLayout.PAGE_END);

        logMessage("Copy the following link and paste it in a web browser : " + tokenValidationUrl + " (don't close your browser page now)");
        logMessage("Click on the grant button and then please fill in the validation code that is displayed previous web page");

        connexionPopup.add(jPanel);
        connexionPopup.pack();
        connexionPopup.setVisible(true);
    }

    public void process() throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException, ImageReadException {
        logMessage("Authenticating on remote server");
        AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient(configuration.getoAuthAccessKey(), configuration.getoAuthAccessSecret());

        logMessage("Loading user preferences");
        UserPreferences userPreferences = appEngineOAuthClient.getUserPreferences();

        if (userPreferences == null) {
            logMessage("User preferences are not set up yet. Please connect first to http://application.mypicturelog.com/ihm/");
        }

        Pair<LocalDateTime, LocalDateTime> startEnd = getStartAndEndDates(imageDir, userPreferences);
        if (startEnd != null) logMessage("Min image date '" + startEnd.getValue0() + "', max image date '" + startEnd.getValue1() + "'");

        log.append("Loading log recording corresponding to your photos\n");
        Collection<LogRecording> logRecordings = appEngineOAuthClient.getMatchingLogRecordings(startEnd.getValue0(), startEnd.getValue1());
        if (CollectionUtils.isNotEmpty(logRecordings)) logMessage("Found " + logRecordings.size() + " matching log recordings");
        else {
            logMessage("No log recording are matching your photos");
            return;
        }

        Collection<File> imageFiles = imageDates.keySet();

        for (File imageFile : imageFiles) {
            Triplet<Double, Double, String> localisations = LocalisationUtils.getImagePosition(imageDates.get(imageFile), logRecordings);
            if (localisations != null) {
                logMessage("File '" + imageFile + "' with date " + imageDates.get(imageFile) + " is located at (" + localisations.getValue0() + ", " + localisations.getValue1() + ", " + localisations.getValue2() + ")");
                try {
                    addGPSInfosToExif(imageFile, localisations.getValue0(), localisations.getValue1());
                } catch (Throwable t) {
                    logMessage("An error has occured while updating exif meta data on file '" + imageFile + "'");
                    logMessage(Throwables.getStackTraceAsString(t));
                }
            } else {
                logMessage("File '" + imageFile + "' with date " + imageDates.get(imageFile) + " is not located");
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

    public void setImageDir(File imageDir) {
        this.imageDir = imageDir;
    }

    private void logMessage(final String message) {
        log.append(message + "\n");
        log.setCaretPosition(log.getDocument().getLength());
//        System.out.println(message);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("MyPictureLog");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                MainGUI mainGUI = new MainGUI();
                frame.add(mainGUI);
                frame.pack();
                frame.setVisible(true);
                mainGUI.handleConfigurationFile();
            }
        });
    }

}
