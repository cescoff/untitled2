package fr.untitled2.desktop;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;

public class Controller {

    @FXML
    private Label handleFileText;

    @FXML
    private ProgressBar imagePositionProgress;

    @FXML protected void handleAddPositionToMyImages(ActionEvent actionEvent) {
        imagePositionProgress.setVisible(true);
        imagePositionProgress.setProgress(0.5);
    }


}
