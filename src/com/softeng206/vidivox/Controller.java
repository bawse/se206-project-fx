package com.softeng206.vidivox;

import com.softeng206.vidivox.concurrency.FestivalMp3Worker;
import com.softeng206.vidivox.concurrency.FestivalPreviewWorker;
import com.softeng206.vidivox.concurrency.RewindWorker;
import com.softeng206.vidivox.concurrency.VideoRenderWorker;
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
    @FXML public MenuItem advancedButton;
    @FXML public TitledPane commentaryPanel;
    @FXML public Slider volumeSlider;

    private void playMedia(File video) {
        player = new MediaPlayer(new Media(video.toURI().toString()));
        player.setAutoPlay(true);

        if (mediaView.getMediaPlayer() != null) {
            mediaView.getMediaPlayer().dispose();
        }

        mediaView.setMediaPlayer(player);
        mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
        mediaView.fitWidthProperty().bind(mediaPane.widthProperty());
        timeSlider.setDisable(false);
        mediaPane.setVisible(true);

        commentaryPanel.setDisable(false);
        commentaryPanel.setExpanded(true);

        player.play();

        setListeners();

    }
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

    public void setListeners(){

        volumeSlider.valueProperty().bindBidirectional(player.volumeProperty());
        primaryStage = (Stage) volumeSlider.getScene().getWindow();
        commentaryPanel.expandedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue){
                    primaryStage.setHeight(primaryStage.getHeight() - 196);
                }
                else{
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

        player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                if (!timeSlider.isValueChanging()) {
                    timeSlider.setValue(newValue.toSeconds());
                }
            }
        });

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
    public void update() {
        if (timeLabel != null && timeSlider != null) {
            Duration currentTime = player.getCurrentTime();
            totalTime = player.getMedia().getDuration();
            timeLabel.setText(formatDuration(player.getCurrentTime(), player.getMedia().getDuration()));
        }

    }

    public String formatDuration(Duration current, Duration total) {

        int totalSeconds = (int) current.toSeconds();
        int totalMinutes = totalSeconds / 60;
        int currentSeconds = totalSeconds - (totalMinutes * 60);

        int totalSec = (int) total.toSeconds();
        int totalMin = totalSec / 60;
        int sec = totalSec - (totalMin * 60);

        return String.format("%02d:%02d / %02d:%02d", totalMinutes, currentSeconds, totalMin, sec);

    }

    public void stopVideo() {
        if (mediaView.getMediaPlayer() != null) {
            player.stop();
        }
    }

    public void browseVideo() {
        configFileChooser();
        fc.setTitle("Browse for video");
        selectedVideo = fc.showOpenDialog(browseVideoButton.getScene().getWindow());


        if (selectedVideo != null) {
            playMedia(selectedVideo);
        } else {
            showAlert(Alert.AlertType.WARNING, "Error", "Please select a video file to preview.");
        }
    }

    public void fastForwardVideo() {
        if (mediaView.getMediaPlayer() != null) {
            player.setMute(true);
            player.setRate(8.0);
        }
    }

    public void rewindVideo() {
        if (mediaView.getMediaPlayer() != null) {
            rewindBackground = new RewindWorker(player);
            rewindBackground.runTask();
        }
    }

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

    private boolean checkTextSuitability() {
        if (ttsPreviewText.getText().split(" ").length > 35) {
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
        selectedAudio = fc.showOpenDialog(browseVideoButton.getScene().getWindow());

        if (selectedAudio == null) {
            currentAudio.setText("(none)");
            showAlert(Alert.AlertType.WARNING, "Error", "Please select an audio file to use.");
            return;
        }

        currentAudio.setText(selectedAudio.getName());
    }

    public void renderVideo() {
        if (selectedAudio != null && selectedVideo != null) {
            configFileChooser();
            fc.setTitle("Save rendered video to file");
            File destination = fc.showSaveDialog(browseVideoButton.getScene().getWindow());

            if (destination == null) {
                showAlert(Alert.AlertType.WARNING, "Error", "Please select a valid destination file.");
                return;
            }

            VideoRenderWorker worker = new VideoRenderWorker(selectedVideo, selectedAudio, destination,
                    mediaView.getMediaPlayer().getMedia().getDuration().toMillis());
            progressBar.progressProperty().bind(worker.progressProperty());
            worker.setOnSucceeded(
                    event -> {
                        progressBar.progressProperty().unbind();
                        progressBar.setProgress(0);
                        showAlert(Alert.AlertType.INFORMATION, "Done!", "Your video was saved successfully.");
                    }
            );
            worker.runTask();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "You must select a video file and an audio file to combine.");
        }
    }

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setResizable(true);
        if (message.length() > 50){
            alert.getDialogPane().setPrefSize(300,300);
        }
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void advancedSettings() throws IOException {
        if (selectedAudio != null && selectedVideo != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("advancedsettings.fxml"));
            Parent root1 = fxmlLoader.load();

            AdvancedSettingsController asc = fxmlLoader.getController();
            asc.initialize(selectedAudio,selectedVideo,progressBar, player);

            Stage stage = new Stage();
            stage.setTitle("Advanced A/V settings");
            stage.setScene(new Scene(root1, 350, 350));
            stage.setResizable(false);
            stage.showAndWait();
       }
        else{
            showAlert(Alert.AlertType.ERROR, "Error", "Please select an audio and video file first.");
       }
    }

}
