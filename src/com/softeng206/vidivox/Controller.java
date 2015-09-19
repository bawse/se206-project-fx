package com.softeng206.vidivox;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class Controller {

    @FXML
    public MediaView mediaView;

    @FXML
    public Pane mediaPane;

    @FXML
    public Button browseVideoButton;

    @FXML
    public void playMedia(){

        Media media = new Media("file:///home/jay/Downloads/bigbuckbunny206_2.mp4");
        MediaPlayer player = new MediaPlayer(media);
        mediaView.setMediaPlayer(player);
        player.setAutoPlay(true);
        mediaPane.setVisible(true);
        mediaPane.getChildren().add(mediaView);


        player.play();


    }

    public void browseVideo() {
        FileChooser fc = new FileChooser();

        File file = fc.showOpenDialog(browseVideoButton.getScene().getWindow());


    }


}
