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
    @FXML public RadioButton addStartStrip;
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
        // Creating a group for all of the Radio Buttons, so that only one can be selected at a time.
        ToggleGroup group = new ToggleGroup();
        overlayVolume.setToggleGroup(group);
        overlayAtLocation.setToggleGroup(group);
        addStartStrip.setToggleGroup(group);

    }

    // Enable the text fields on the selection of a particular radio button.
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

    // A "director" method that will invoke the correct method based on what radio button is selected.
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
                return;
            }

            // Create a new instance of the worker that will handle the video processing if the user selects a valid destination file.
            VideoRenderWorker worker = new VideoRenderWorker(selectedVideo, selectedAudio, destination,
                    player.getMedia().getDuration().toMillis());

            // Bind the progressbar to the worker progress, so the worker updates automatically according to the progress of the worker.
            overlayPB.progressProperty().bind(worker.progressProperty());

            worker.setOnSucceeded(
                    event -> {
                        // Unbind and reset the progress bar, as well as inform the user that the video has been saved correctly.
                        overlayPB.progressProperty().unbind();
                        overlayPB.setProgress(0);
                        tabPane.setDisable(false);
                        exportButton.setDisable(false);
                        Controller.showAlert(Alert.AlertType.INFORMATION, "Done!", "Your video was saved successfully.");
                    }
            );

            // Start the task, and disable all other click-ables on the panel except the help button so that there is no
            // overlapping of workers.
            worker.runTask();
            tabPane.setDisable(true);
            exportButton.setDisable(true);

        // The user does not have BOTH audio and video files selected.
        } else {
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "You must select a video file and an audio file to combine.");
        }
    }

    public void processOverlayVideo() {
        String location;
        String volumeReduction = "";

        // Depending on which option has been selected, the location/volumeReduction strings are populated with user input.
        if (overlayAtLocation.isSelected()) {
            location = locationBox.getText();
        } else if (overlayVolume.isSelected()) {
            location = locationBox2.getText();
            volumeReduction = volumeBox.getText();
        } else {
            location = null;
        }

        // Check for common errors in the user input via the errorChecking method. If it returns true, then the user has entered
        // incorrect input.
        if (!errorChecking(location,volumeReduction)){
            return;
        }

        if (overlayAtLocation.isSelected() && location != null) {
            fc.setTitle("Choose video export location");

            File destination = fc.showSaveDialog(locationBox.getScene().getWindow());
            /*
            The constructor for the advanced video worker requires an input which lets the worker know what command to run.
            As seen in the declaration of this worker, "1" is passed in as one of the inputs. This suggests that the first radio
            button is selected, and hence only an overlay at location needs to be done.
             */
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

            // No destination specified.
            if (destination == null) {
                Controller.showAlert(Alert.AlertType.WARNING, "Error", "Please select a valid destination file.");
                return;
            }

            // Disabling other GUI components whilst worker is running.
            worker.runTask();
            tabPane.setDisable(true);
            exportButton.setDisable(true);
        }
        // This conditional block is called in the case where the second radio button is selected.
        if (overlayVolume.isSelected() && location != null) {
            fc.setTitle("Choose video export location");
            File destination = fc.showSaveDialog(locationBox.getScene().getWindow());

            // As mentioned above, this worker has "2" as one of the inputs to the constructor. Suggests that the second
            // radio button has been selected, and overlay at location as well as volume reduction needs to be done.
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

    // Method which will display error messages for common mistakes made by users such as empty inputs and incorrect format.
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
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "Incorrect location format.");
            return false;
        }
        if (overlayVolume.isSelected() && !isCorrectFormat(location, volumeReduction)){
            Controller.showAlert(Alert.AlertType.ERROR, "Error", "Incorrect volume reduction format.");
        }

        return true;

    }

    // Method which checks for correct format for just the overlay audio functionality.
    public boolean isCorrectFormat(String text){

        if (text == null || text.isEmpty()){
            return false;
        }

        // The expected format is mm:ss therefore the input is split using the : delimiter.
        String[] splitLocation = text.split(":");
        // If more than one instance of the delimiter is found, then the format is incorrect.
        if (splitLocation.length > 2){
            return false;
        }
        else {
            // Try and parse the split strings into integers. If they cannot be converted, then they aren't integers.
            // Hence, we cannot make sense of the input. So the input format is incorrect.
            try {
                int minute = Integer.parseInt(splitLocation[0]);
                int seconds = Integer.parseInt(splitLocation[1]);
            } catch (Exception e) { // exception will get thrown if string can't be converted to an integer
                return false;
            }
            // If none of the other return statements have been called, the input is assumed to be correct.
            return true;
        }
    }

    // Method which checks for correct formatting for the second radio button in the audio overlay tab.
    public boolean isCorrectFormat(String location, String volume){
        // The format expected is "x#dB" where x refers to the sign (+/-) and # refers to an integer value.
        // Splitting the string at the "d" allows us to get the integer value.
        String[] substring = volume.split("d");

        try{
            // If the string cannot be parsed into an integer, it is of incorrect format and hence will throw an exception.
            Integer.parseInt(substring[0]);
        } catch (Exception e){
            return false;
        }
        // If the end of the string does not equal "db" (case insensitive) then the format is incorrect.
        if (!volume.substring(volume.indexOf("d"), volume.length()).equalsIgnoreCase("db")){
            return false;
        }
        return true;

    }

}
