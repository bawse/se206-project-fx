package com.softeng206.vidivox;

import com.softeng206.vidivox.concurrency.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class Controller {
    private FileChooser fc = new FileChooser();
    private FestivalMp3Worker mp3Worker;
    private MediaPlayer player;
    private FestivalPreviewWorker previewWorker;
    private RewindWorker rewindBackground;
    private File selectedAudio, selectedVideo;
    Duration totalTime;
    Stage primaryStage;

    // All the GUI related components are declared here.

    @FXML public Button browseVideoButton;
    @FXML public Label currentAudio;
    @FXML public MediaView mediaView;
    @FXML public Pane mediaPane;
    @FXML public ProgressBar progressBar;
    @FXML public Button rewindButton;
    @FXML public Button stopVideoButton;
    @FXML public Button ttsCancelPreviewButton;
    @FXML public Button ttsPreviewButton;
    @FXML public TextArea ttsPreviewText;
    @FXML public Button playButton;
    @FXML public Button pauseButton;
    @FXML public Slider timeSlider;
    @FXML public Label timeLabel;
    @FXML public CheckBox toggleMute;
    @FXML public Button advancedButton;
    @FXML public TitledPane commentaryPanel;
    @FXML public Slider volumeSlider;




    public void browseVideo() {
        configFileChooser();
        fc.setTitle("Browse for video");

        //http://stackoverflow.com/questions/13634576/javafx-filechooser-how-to-set-file-filters
        // Setting an extension filter, so user can only select .mp4 files. Saves a lot of error handling.

        FileChooser.ExtensionFilter videoFilter = new FileChooser.ExtensionFilter("Video file (.mp4)", "*.mp4");
        fc.getExtensionFilters().remove(0, fc.getExtensionFilters().size());
        fc.getExtensionFilters().add(videoFilter);
        fc.setSelectedExtensionFilter(videoFilter);

        if (selectedVideo == null) {
            selectedVideo = fc.showOpenDialog(browseVideoButton.getScene().getWindow());

            // If a file was selected, play the video.
            if (selectedVideo != null) {
                playMedia(selectedVideo);
            }

        } else { // There is already a video selected. Therefore if the user specifies a new video, then this must be recognised.
            File newVideo = fc.showOpenDialog(browseVideoButton.getScene().getWindow());
            if (newVideo == null){
                return;
            } else{
                selectedVideo = newVideo;
                playMedia(selectedVideo);
            }
        }
    }

    private void playMedia(File video) {
        // Initialising a new MediaPlayer to play the file chosen by the user.
        player = new MediaPlayer(new Media(video.toURI().toString()));
        player.setAutoPlay(true);

        // If there is already a video playing, dispose of the existing
        // media player and replace it with the new.
        if (mediaView.getMediaPlayer() != null) {
            mediaView.getMediaPlayer().dispose();
        }

        mediaView.setMediaPlayer(player);

        // HeightProperty and WidthProperty binds so that MediaPlayer resizing works.
        // Bound to the Pane containing the mediaplayer. Changes to the Pane will be
        // translated to resizing of the video.
        mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
        mediaView.fitWidthProperty().bind(mediaPane.widthProperty());

        timeSlider.setDisable(false);
        mediaPane.setVisible(true);

        commentaryPanel.setDisable(false);
        commentaryPanel.setExpanded(true);

        player.play();

        // Method which will set all ChangeListeners. Used to update the time stamps as well
        // as the slider values for the player controls.
        setListeners();

    }

    public void setListeners(){

        // Bidirectional bind on the volume slider. Any changes to slider value results in
        // player volume being adjusted.
        volumeSlider.valueProperty().bindBidirectional(player.volumeProperty());
        primaryStage = (Stage) volumeSlider.getScene().getWindow();

        // The TTS panel is initially disabled. When a video starts playing, it is automatically enabled.
        // Enabling of the panel results in window dimensions changing, and hence will affect the video size.
        // Listener on the expandedProperty ensures that video remains the same size.
        commentaryPanel.expandedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    primaryStage.setHeight(primaryStage.getHeight() - 196); // 196 is the total height of the TTS panel.
                } else {
                    primaryStage.setHeight(primaryStage.getHeight() + 196);
                }
            }
        });

        // The total duration property will only change once for every video,
        // therefore adding a listener allows us to set the maximum value of the time slider
        // as opposed to any arbitrary value that doesn't mean anything.
        player.totalDurationProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                timeSlider.setMax(newValue.toSeconds());
            }
        });

        // Update the timeslider value to the current value of the player.
        player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                if (!timeSlider.isValueChanging()) {
                    timeSlider.setValue(newValue.toSeconds());
                }
            }
        });


        // Allows for the user to press anywhere on the slider to skip to a certain part of the video being played.
        timeSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    player.seek(Duration.seconds(timeSlider.getValue()));

                }
            }
        });

        //http://www.java2s.com/Tutorials/Java/JavaFX/0490__JavaFX_Slider.htm
        // Link has several examples of adding listeners to properties of a slider,
        // which helped me in developing this particular listener. Refer to journal for
        // additional notes on how this listener was designed.
        timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (!timeSlider.isValueChanging()) {
                    double currentTimeInSeconds = player.getCurrentTime().toSeconds();

                    // If the value change is not brought about by the changelistener implemented on the currentTimeProperty
                    // of the player, then this assumes that the timeslider was dragged to skip to a new section of the video.
                    // As the changeListener on the currentTimeProperty of the player is "activated" due to minute differences,
                    // a change value greater than 1.0 (arbitrary) denotes that the user has chosen to skip to a different portion of the video.
                    if (Math.abs(currentTimeInSeconds - newValue.doubleValue()) > 1.0) {
                        player.seek(Duration.seconds(newValue.doubleValue()));
                    }
                }
            }
        });


        player.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                update();
            }
        });


    }

    //http://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
    // Updates the time stamps on the mediaplayer controls with the correctly formatted time.
    public void update() {
        if (timeLabel != null && timeSlider != null) {
            Duration currentTime = player.getCurrentTime();
            totalTime = player.getMedia().getDuration();
            timeLabel.setText(formatDuration(player.getCurrentTime(), player.getMedia().getDuration()));
        }

    }

    // Simple calculations in order to create a String format, so that time stamps can be updated using
    // a consistent display format.
    public String formatDuration(Duration current, Duration total) {

        int totalSeconds = (int) current.toSeconds();
        int totalMinutes = totalSeconds / 60;
        int currentSeconds = totalSeconds - (totalMinutes * 60);

        int totalSec = (int) total.toSeconds();
        int totalMin = totalSec / 60;
        int sec = totalSec - (totalMin * 60);

        return String.format("%02d:%02d / %02d:%02d", totalMinutes, currentSeconds, totalMin, sec);

    }

    // Sets the initial directory of the file chooser to be the path to the users Desktop.
    public void configFileChooser(){
        String s = System.getenv("HOME") + "/Desktop/";
        File desktop = new File(s);
        fc.setInitialDirectory(desktop);
    }

    public void setToggleMute() {
        if (mediaView.getMediaPlayer() != null) {
            if (toggleMute.isSelected()) {
                player.setMute(true);
            } else {
                player.setMute(false);
            }
        }
    }

    public void stopVideo() {
        if (mediaView.getMediaPlayer() != null) {
            if (rewindBackground != null && rewindBackground.isRunning()){
                rewindBackground.cancel(true);
            }
            if (player.getRate() != 1){
                player.setRate(1);
                player.setMute(false);
            }
            player.stop();
        }
    }

    public void fastForwardVideo() {
        // Different approach to fast-forwarding a video. The playback rate is just increased to the maximum value of 8.
        // The player looks much more responsive with this method as opposed to skipping frames.
        if (mediaView.getMediaPlayer() != null) {
            // If the video is currently rewinding, then we need to cancel that process before attempting a fast-forward.
            if (rewindBackground != null && rewindBackground.isRunning()){
                rewindBackground.cancel(true);
            }
            player.setMute(true);
            player.setRate(8.0);
        }
    }


    // Unfortunately the same method as fast-forward couldn't be used, as the setRate method does
    // not accept negative rates as input.
    public void rewindVideo() {
        if (mediaView.getMediaPlayer() != null) {
            // If the video is being fast-forwarded, we need to restore it to default speed before we rewind.
            if (player.getRate() != 1){
                player.setRate(1);
                player.setMute(false);
                player.play();
            }
            // Only rewind if there is no current rewind worker running or if it is the first time rewind has been pressed
            // in which case rewindBackground will be null.
            if (rewindBackground == null || !rewindBackground.isRunning()) {
                rewindBackground = new RewindWorker(player);
                rewindBackground.runTask();
            }
        }
    }

    // Resume the video. If the video is being rewinded, then the video will continue playback at whichever point
    // the play button was pressed.
    public void playButtonPressed() {
        if (mediaView.getMediaPlayer() != null) {
            if (rewindBackground != null && rewindBackground.isRunning()) {
                Duration playtime = player.getCurrentTime();
                rewindBackground.cancel(true);
                player.setMute(false);
                player.seek(playtime);
            }


            player.setRate(1);
            player.setMute(false);
            player.play();


        }
    }

    public void pauseButtonPressed() {
        if (mediaView.getMediaPlayer() != null) {
            if (player.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                player.pause();
            }
        }

    }

    // Check if any word in the text to speech preview TextField is greater than 35 characters.
    // If so, then the word is too long and might cause festival to speak funny.
    private boolean checkTextSuitability() {
        if (ttsPreviewText.getText().split(" ").length > 34) {
            showAlert(Alert.AlertType.WARNING, "Warning",
                    "Please use less than 35 words in your text. Long phrases tend to sound unnatural.");
            return false;
        }

        return true;
    }

    public void ttsPreview() {
        if (!checkTextSuitability()) {
            return;
        }

        // Concurrent worker to execute the Festival commands on the linux command line.
        // Enabling and Disabling of the buttons is handled after the worker has finished executing.
        previewWorker = new FestivalPreviewWorker(ttsPreviewText.getText());
        previewWorker.setOnFinished(
                WorkerStateEvent -> {
                    ttsCancelPreviewButton.setDisable(true);
                    ttsPreviewButton.setDisable(false);
                }
        );
        ttsPreviewButton.setDisable(true);
        ttsCancelPreviewButton.setDisable(false);

        previewWorker.runTask();
    }


    public void ttsPreviewCancel() {
        if (previewWorker != null) {
            previewWorker.cancel(true);
        }
    }

    public void ttsMp3() {
        // If the text is not suitable, then the user cannot save to mp3.
        if (!checkTextSuitability()) {
            return;
        }

        if (ttsPreviewText.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning",
                    "Please enter some text before saving to MP3.");
        } else {
            configFileChooser();
            fc.setTitle("Save MP3");
            File target = fc.showSaveDialog(ttsPreviewButton.getScene().getWindow());

            if (target == null) {
                showAlert(Alert.AlertType.WARNING, "Error", "Please select a valid destination file.");
                return;
            }

            // Executing the save to mp3 commands on a worker thread so GUI remains responsive. The user
            // is notified once their file has been saved to the desired location.
            mp3Worker = new FestivalMp3Worker(ttsPreviewText.getText(), target);
            mp3Worker.setOnSucceeded(
                    event -> showAlert(Alert.AlertType.INFORMATION, "Done!", "Your MP3 was saved successfully.")
            );

            mp3Worker.setOnFailed(
                    event -> System.out.println(event.toString())
            );

            mp3Worker.runTask();
        }
    }

    public void browseAudio() {
        configFileChooser();
        fc.setTitle("Browse for audio file");

        // Restrict the user to only be able to choose from .mp3 or .wav files via an Extension Filter applied to
        // the FileChooser.
        FileChooser.ExtensionFilter audioFilter = new FileChooser.ExtensionFilter("Audio file (.mp3, .wav)", "*.mp3", "*.wav");
        fc.getExtensionFilters().remove(0, fc.getExtensionFilters().size());
        fc.getExtensionFilters().add(audioFilter);
        fc.setSelectedExtensionFilter(audioFilter);

        selectedAudio = fc.showOpenDialog(browseVideoButton.getScene().getWindow());

        if (selectedAudio == null) {
            currentAudio.setText("(none)");
            return;
        }


        currentAudio.setText(selectedAudio.getName()); // Label showing the current audio that is selected.
    }

    // Custom method which allows for minor customisation of the alerts shown to the user.
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setResizable(true);
        if (message.length() > 50){
            alert.getDialogPane().setPrefSize(500,300);
        }
        alert.setContentText(message);
        alert.showAndWait();
    }

    // If the user presses the add mp3 to video button, then a new Stage is created.
    // New stage consists of the Advanced settings panel, allowing user to choose what audio filters
    // to apply to their chosen video.
    public void advancedSettings() throws IOException {
        if (selectedAudio != null && selectedVideo != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("advancedsettings.fxml"));
            Parent root1 = fxmlLoader.load();

            AdvancedSettingsController asc = fxmlLoader.getController();
            asc.initialize(selectedAudio,selectedVideo,progressBar, player);

            Stage stage = new Stage();
            stage.setTitle("Advanced audio settings");
            stage.setScene(new Scene(root1, 350, 450));
            stage.setResizable(false);
            stage.showAndWait();
       }
        else{
            showAlert(Alert.AlertType.ERROR, "Error", "Please select an audio and video file first.");
       }
    }

}
