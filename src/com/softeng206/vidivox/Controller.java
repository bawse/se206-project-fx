package com.softeng206.vidivox;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

import java.io.File;

public class Controller {
    @FXML
    public MediaView mediaView;

    @FXML
    public Pane mediaPane;

    @FXML
    public Button browseVideoButton;

    private void playMedia(File video) {
        MediaPlayer player = new MediaPlayer(new Media(video.toURI().toString()));
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

    public void browseVideo() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(browseVideoButton.getScene().getWindow());
        playMedia(file);
    }
}
