package fr.untitled2.desktop;

import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.utils.JAXBUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class Main extends Application {

    public static final String configuration_file_name = ".myPictureLog";

    private Label handleFileTextField;

    private ProgressBar imagePositionProgressBar;

    @Override
    public void start(final Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(new PathMatchingResourcePatternResolver().getResource("classpath:main.fxml").getURL());

        primaryStage.setTitle("MyPictureLog");
        primaryStage.setScene(new Scene(root, 300, 200));

        primaryStage.show();

        Button imageLocationBrowseButton = (Button) root.lookup("#imageLocationBrowseButton");

        handleFileTextField = (Label) root.lookup("#handleFileText");
        imagePositionProgressBar = (ProgressBar) root.lookup("#imagePositionProgress");


        final ImageLocationTask imageLocationTask = new ImageLocationTask(1.0);
        if (imageLocationBrowseButton != null) {
            imageLocationBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    final DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setInitialDirectory(SystemUtils.getUserDir());

                    imageLocationTask.setImageDir(directoryChooser.showDialog(primaryStage));
                }
            });
        }

        Button handleAddPositionToMyImagesButton = (Button) root.lookup("#handleAddPositionToMyImagesButton");
        final Main main = this;
        handleAddPositionToMyImagesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                if (imageLocationTask.getImageDir() == null) return;
                imagePositionProgressBar.setVisible(true);
                imagePositionProgressBar.progressProperty().bind(imageLocationTask.progressProperty());
                handleFileTextField.setVisible(true);
                handleFileTextField.textProperty().bind(imageLocationTask.messageProperty());
                try {
                    new Thread(imageLocationTask).start();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });


        final File configFile = new File(SystemUtils.USER_HOME, configuration_file_name);
        if (!configFile.exists()) {
            Parent connectPopup = FXMLLoader.load(new PathMatchingResourcePatternResolver().getResource("classpath:connectPopup.fxml").getURL());
            final Popup popup = new Popup();
            popup.getContent().add(connectPopup);
            popup.show(primaryStage);

            final AppEngineOAuthClient appEngineOAuthClient = new AppEngineOAuthClient();
            String tokenValidationUrl = appEngineOAuthClient.getTokenValidationUrl();

            if (StringUtils.isNotEmpty(System.getProperty("http.proxyUser")) && StringUtils.isNotEmpty(System.getProperty("http.proxyPassword"))) {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(System.getProperty("http.proxyUser"), System.getProperty("http.proxyPassword").toCharArray());
                    }
                });
            }

            WebView connectWebView = (WebView) connectPopup.lookup("#connectWebView");

            final TextField textField = (TextField) connectPopup.lookup("#codeTextField");
            Button codeValidate = (Button) connectPopup.lookup("#validateCodeButton");

            codeValidate.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    try {
                        appEngineOAuthClient.validateTokens(textField.getText());
                    } catch (OAuthCommunicationException e) {
                        e.printStackTrace();
                    } catch (OAuthExpectationFailedException e) {
                        e.printStackTrace();
                    } catch (OAuthNotAuthorizedException e) {
                        e.printStackTrace();
                    } catch (OAuthMessageSignerException e) {
                        e.printStackTrace();
                    }

                    Configuration configuration = new Configuration();
                    configuration.setoAuthAccessKey(appEngineOAuthClient.getAccessToken());
                    configuration.setoAuthAccessSecret(appEngineOAuthClient.getTokenSecret());
                    try {
                        JAXBUtils.marshal(configuration, configFile, true);
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    popup.hide();
                }
            });

            connectWebView.getEngine().load(tokenValidationUrl);
        }

    }



    public static void main(String[] args) {
        launch(args);
    }
}
