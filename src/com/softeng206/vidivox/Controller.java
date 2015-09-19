package com.softeng206.vidivox;

import com.softeng206.vidivox.concurrency.RewindWorker;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;

public class Controller {
    MediaPlayer player;
    RewindWorker rewindBackground;

    @FXML public Button playPauseButton;
    @FXML public Button rewindButton;
    @FXML public MediaView mediaView;
    @FXML public Pane mediaPane;
    @FXML public Button browseVideoButton;
    @FXML public Button stopVideoButton;

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

}
