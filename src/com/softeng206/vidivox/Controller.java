package com.softeng206.vidivox;

import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    public void playMedia(){
        Media media = new Media("file:///home/jay/Downloads/beenon.mp4");
        MediaPlayer player = new MediaPlayer(media);

        if (mediaView.getMediaPlayer() != null) {
            mediaView.getMediaPlayer().dispose();
        }


        mediaView.setMediaPlayer(player);

        player.setAutoPlay(true);

        mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
        mediaView.fitWidthProperty().bind(mediaPane.widthProperty());

        mediaPane.setVisible(true);


        player.play();

        //System.out.println(mediaPane.boundsInLocalProperty().toString());


    }

    public void browseVideo() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(browseVideoButton.getScene().getWindow());
    }
}
