package com.softeng206.vidivox;

import com.softeng206.vidivox.concurrency.AdvancedVideoWorker;
import com.softeng206.vidivox.concurrency.VideoRenderWorker;
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

    @FXML public RadioButton overlayAtLocation;
    @FXML public Button exportButton;
    @FXML public RadioButton overlayVolume;
    @FXML public TextField locationBox;
    @FXML public TextField locationBox2;
    @FXML public TextField volumeBox;
    @FXML public ProgressBar overlayPB;
    @FXML public HBox textFields;
    @FXML public Button helpLabel;
    @FXML public TabPane tabPane;
    @FXML public Tab overlayTab;
    @FXML public Tab stripAudioTab;


    public void initialize(File audio, File video, ProgressBar pbar, MediaPlayer player){
        this.selectedAudio = audio;
        this.selectedVideo = video;
        this.progressbar = pbar;
        this.player = player;
        setupRadioButtons();
    }


    public void setupRadioButtons(){
        ToggleGroup group = new ToggleGroup();
        overlayVolume.setToggleGroup(group);
        overlayAtLocation.setToggleGroup(group);

    }

    public void activateLocationControls(){

        locationBox.setDisable(false);
        textFields.setDisable(true);

    }

    public void activateVolumeControls(){
        textFields.setDisable(false);
        locationBox.setDisable(true);

    }

    public void helpDialog(){
        String helpMessage = "Please enter the exact duration at which you want to insert the selected audio file." +
                " Make sure your input is in the format of \"mm:ss\". E.g. \"01:20\".\n\nAny input not given in this format will" +
                " not be accepted. If you want to decrease the volume of the original video, enter something like \"-10dB\". This " +
                "will decrease the original audio by 10 Decibels.\n\nThe format of the input is critical.";
        Controller.showAlert(Alert.AlertType.INFORMATION, "Help", helpMessage);
    }

    public void director(){
        if (overlayTab.isSelected()){
            processOverlayVideo();
        } else if (stripAudioTab.isSelected()){
            processStripAudio();
        }
    }
    public void processStripAudio() {
        if (selectedAudio != null && selectedVideo != null) {
            fc.setTitle("Choose video save location");
            File destination = fc.showSaveDialog(locationBox.getScene().getWindow());

            if (destination == null) {
                Controller.showAlert(Alert.AlertType.WARNING, "Error", "Please select a valid destination file.");
                return;
            }

            VideoRenderWorker worker = new VideoRenderWorker(selectedVideo, selectedAudio, destination,
                    player.getMedia().getDuration().toMillis());
            overlayPB.progressProperty().bind(worker.progressProperty());
            worker.setOnSucceeded(
                    event -> {
                        overlayPB.progressProperty().unbind();
                        overlayPB.setProgress(0);
                        tabPane.setDisable(false);
                        exportButton.setDisable(false);
                        Controller.showAlert(Alert.AlertType.INFORMATION, "Done!", "Your video was saved successfully.");
                    }
            );
            worker.runTask();
            tabPane.setDisable(true);
            exportButton.setDisable(true);
        } else {
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "You must select a video file and an audio file to combine.");
        }
    }

    public void processOverlayVideo() {
        String location;
        String volumeReduction = "";
        if (overlayAtLocation.isSelected()) {
            location = locationBox.getText();
        } else if (overlayVolume.isSelected()) {
            location = locationBox2.getText();
            volumeReduction = volumeBox.getText();
        } else {
            location = null;
        }

        if (!errorChecking(location,volumeReduction)){
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
                        tabPane.setDisable(false);
                        exportButton.setDisable(false);
                        Controller.showAlert(Alert.AlertType.INFORMATION, "Done!", "Your video was saved successfully.");
                    });
            if (destination == null) {
                Controller.showAlert(Alert.AlertType.WARNING, "Error", "Please select a valid destination file.");
                return;
            }
            worker.runTask();
            tabPane.setDisable(true);
            exportButton.setDisable(true);
        }
        if (overlayVolume.isSelected() && location != null) {
            fc.setTitle("Choose video export location");
            File destination = fc.showSaveDialog(locationBox.getScene().getWindow());
            AdvancedVideoWorker worker = new AdvancedVideoWorker(selectedAudio, selectedVideo, destination,
                    locationBox2.getText(), volumeBox.getText(), 2, player.getMedia().getDuration().toMillis());
            overlayPB.progressProperty().bind(worker.progressProperty());
            worker.setOnSucceeded(
                    event -> {
                        overlayPB.progressProperty().unbind();
                        overlayPB.setProgress(0);
                        tabPane.setDisable(false);
                        exportButton.setDisable(false);
                        Controller.showAlert(Alert.AlertType.INFORMATION, "Done!", "Your video was saved successfully.");
                    });
            if (destination == null) {
                Controller.showAlert(Alert.AlertType.WARNING, "Error", "Please select a valid destination file.");
                return;
            }
            worker.runTask();
            tabPane.setDisable(true);
            exportButton.setDisable(true);

        }


    }

    public boolean errorChecking(String location, String volumeReduction){
        if (!overlayVolume.isSelected() && !overlayAtLocation.isSelected()) {
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "Please select from one of the options.");
            return false;
        }
        if (location == null || location.isEmpty()) {
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "Please specify a location.");
            return false;
        }
        if (overlayVolume.isSelected() && volumeReduction.isEmpty()) {
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "Please specify a reduction in volume.");
            return false;
        }

        if (!isCorrectFormat(location)) {
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "Incorrect input format.");
            return false;
        }
        return true;

    }

    public boolean isCorrectFormat(String text){
        if (text == null || text.isEmpty()){
            return false;
        }
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
