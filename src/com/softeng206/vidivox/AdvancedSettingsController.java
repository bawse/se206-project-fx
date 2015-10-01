package com.softeng206.vidivox;

import com.softeng206.vidivox.concurrency.AdvancedVideoWorker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Created by jay on 1/10/15.
 */
public class AdvancedSettingsController {

    private File selectedAudio;
    private File selectedVideo;
    public ProgressBar progressbar;
    private FileChooser fc = new FileChooser();
    private MediaPlayer player;




    @FXML public CheckBox overlayAtLocation;
    @FXML public CheckBox overlayTrim;
    @FXML public TextField locationBox;
    @FXML public ProgressBar overlayPB;


    public void initialize(File audio, File video, ProgressBar pbar, MediaPlayer player){
        this.selectedAudio = audio;
        this.selectedVideo = video;
        this.progressbar = pbar;
        this.player = player;
    }


    public void overlayLocationCheckBox(){
        if (overlayTrim.isSelected()){
            locationBox.setDisable(false);
            overlayTrim.setSelected(false);
        }
        else if (!overlayAtLocation.isSelected()){
            locationBox.setDisable(true);
        }
        else if (locationBox.isDisabled()){
            locationBox.setDisable(false);
        }

    }
    public void overlayAndTrimCheckBox(){
        if (overlayAtLocation.isSelected()){
            overlayAtLocation.setSelected(false);
            locationBox.setDisable(true);
        }
    }

    public void processVideo(){
            String location = locationBox.getText();
            if (overlayAtLocation.isSelected() && location != null) {
                fc.setTitle("Save rendered video to file");
                File destination = fc.showSaveDialog(locationBox.getScene().getWindow());
                AdvancedVideoWorker worker = new AdvancedVideoWorker(selectedAudio, selectedVideo, destination,
                        locationBox.getText(), 1, player.getMedia().getDuration().toMillis());
                overlayPB.progressProperty().bind(worker.progressProperty());
                worker.setOnSucceeded(
                        event -> {
                            overlayPB.progressProperty().unbind();
                            overlayPB.setProgress(0);
                            Controller.showAlert(Alert.AlertType.INFORMATION, "Done!", "Your video was saved successfully.");
                        });
                if (destination == null) {
                    Controller.showAlert(Alert.AlertType.WARNING, "Error", "Please select a valid destination file.");
                    return;
                }
                worker.runTask();
            }

    }

}
