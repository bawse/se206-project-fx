package com.softeng206.vidivox;

import com.softeng206.vidivox.concurrency.FestivalMp3Worker;
import com.softeng206.vidivox.concurrency.FestivalPreviewWorker;
import com.softeng206.vidivox.concurrency.RewindWorker;
import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.regex.Pattern;

public class Controller {
    private FileChooser fc = new FileChooser();
    private FestivalMp3Worker mp3Worker;
    private MediaPlayer player;
    private FestivalPreviewWorker previewWorker;
    private RewindWorker rewindBackground;

    @FXML public Button browseVideoButton;
    @FXML public MediaView mediaView;
    @FXML public Pane mediaPane;
    @FXML public Button playPauseButton;
    @FXML public ProgressBar progressBar;
    @FXML public Button rewindButton;
    @FXML public Button stopVideoButton;
    @FXML public Button ttsCancelPreviewButton;
    @FXML public Button ttsPreviewButton;
    @FXML public TextArea ttsPreviewText;

    private void playMedia(File video) {
        player = new MediaPlayer(new Media(video.toURI().toString()));
        player.setAutoPlay(true);

        if (mediaView.getMediaPlayer() != null) {
            mediaView.getMediaPlayer().dispose();
        }

        mediaView.setMediaPlayer(player);
        mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
        mediaView.fitWidthProperty().bind(mediaPane.widthProperty());

        mediaPane.setVisible(true);
        player.play();
    }

    public void stopVideo() {
        player.stop();
    }

    public void browseVideo() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(browseVideoButton.getScene().getWindow());

        if (file != null) {
            playMedia(file);
        }
    }

    public void fastForwardVideo() {
        if (mediaView.getMediaPlayer() != null){
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

    private boolean checkTextSuitability() {
        if (ttsPreviewText.getText().split(" ").length > 35) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please use less than 35 words in your text. Long phrases tend to sound unnatural.");
            alert.show();
            return false;
        }

        return true;
    }

    public void ttsPreview() {
        if (!checkTextSuitability()) {
            return;
        }

        String escapedText = ttsPreviewText.getText().replaceAll("\"","\\\\\"");
        escapedText = escapedText.replaceAll("\\$", "\\\\\\$");
        previewWorker = new FestivalPreviewWorker(escapedText);
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

        File target = fc.showSaveDialog(ttsPreviewButton.getScene().getWindow());

        if (target == null) {
            return;
        }

        mp3Worker = new FestivalMp3Worker(ttsPreviewText.getText(), target);
        mp3Worker.setOnSucceeded(
                event -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Done!");
                    alert.setHeaderText(null);
                    alert.setContentText("Your MP3 was saved successfully.");
                    alert.show();
                }
        );

        mp3Worker.setOnFailed(
                event -> System.out.println(event.toString())
        );

        mp3Worker.runTask();
    }
}
