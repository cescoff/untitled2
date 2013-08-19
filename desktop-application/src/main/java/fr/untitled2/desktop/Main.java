package fr.untitled2.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(new PathMatchingResourcePatternResolver().getResource("classpath:sample.fxml").getURL());
        primaryStage.setTitle("MyPictureLog");
        primaryStage.setScene(new Scene(root, 300, 200));


        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
