package com.softeng206.vidivox;

import com.softeng206.vidivox.concurrency.AdvancedVideoWorker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
    @FXML public CheckBox overlayVolume;
    @FXML public TextField locationBox;
    @FXML public TextField locationBox2;
    @FXML public ProgressBar overlayPB;
    @FXML public HBox textFields;
    @FXML public Button helpLabel;


    public void initialize(File audio, File video, ProgressBar pbar, MediaPlayer player){
        this.selectedAudio = audio;
        this.selectedVideo = video;
        this.progressbar = pbar;
        this.player = player;
    }


    public void overlayLocationCheckBox(){
        if (overlayVolume.isSelected()){
            locationBox.setDisable(false);
            overlayVolume.setSelected(false);
            textFields.setDisable(true);
        }
        else if (!overlayAtLocation.isSelected()){
            locationBox.setDisable(true);
        }
        else if (locationBox.isDisabled()){
            locationBox.setDisable(false);
        }

    }
    public void overlayVolumeCheckBox(){
        if (overlayAtLocation.isSelected()){
            overlayAtLocation.setSelected(false);
            locationBox.setDisable(true);
        }
        if (overlayVolume.isSelected()){
            textFields.setDisable(false);
        }
        else{
            textFields.setDisable(true);
        }


    }

    public void helpDialog(){
        String helpMessage = "Please enter the exact duration at which you want to insert the selected audio file." +
                " Make sure your input is in the format of \"mm:ss\". E.g. \"01:20\".\n\nAny input not given in this format will" +
                " not be accepted. If you want to decrease the volume of the original video, enter something like \"-10dB\". This " +
                "will decrease the original audio by 10 Decibels.\n\nThe format of the input is critical.";
        Controller.showAlert(Alert.AlertType.INFORMATION, "Help", helpMessage);
    }

    public void processVideo(){
        String location = locationBox.getText();
        if (location == null){
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "Please enter a time.");
        }
        if (!isCorrectFormat(location)){
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "Incorret input format.");
            return;
        }

            if (overlayAtLocation.isSelected() && location != null) {
                fc.setTitle("Choose video export location");
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

    public boolean isCorrectFormat(String text){
        String[] splitLocation = text.split(":");
        if (splitLocation.length > 2){
            return false;
        }else {
            try {
                int minute = Integer.parseInt(splitLocation[0]);
                int seconds = Integer.parseInt(splitLocation[1]);
            } catch (Exception e) {
                return false;
            }


            return true;
        }
    }

}
