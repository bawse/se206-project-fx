package com.softeng206.vidivox;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;

public class Controller {
    MediaPlayer player;
    rewindWorker rewindBackground;

    @FXML
    public Button playPauseButton;

    @FXML
    public Button rewindButton;

    @FXML
    public MediaView mediaView;

    @FXML
    public Pane mediaPane;

    @FXML
    public Button browseVideoButton;

    @FXML
    public Button stopVideoButton;

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
        playMedia(file);
    }

    public void fastForwardVideo(){
        if (mediaView.getMediaPlayer() != null){
            player.setMute(true);
            player.setRate(8.0);
        }
    }

    public void rewindVideo() {
        rewindBackground = new rewindWorker(mediaView);
        rewindBackground.runTask();

    }

    public void playButtonPressed(){

        if (rewindBackground != null && rewindBackground.isRunning()){
            Duration playtime = mediaView.getMediaPlayer().getCurrentTime();
            rewindBackground.cancel(true);
            mediaView.getMediaPlayer().setMute(false);
            mediaView.getMediaPlayer().seek(playtime);
        }
        
         // player.getStatus().equals(MediaPlayer.Status.PLAYING) can be used to check if
        // the player is already playing. Useful for pause functionality for the project.
        if (mediaView.getMediaPlayer() != null && (mediaView.getMediaPlayer().getRate() != 1.0)){
            mediaView.getMediaPlayer().setMute(false);
            mediaView.getMediaPlayer().setRate(1.0);

        }
    }

}
