package com.softeng206.vidivox;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by jay on 19/10/15.
 */
 public class Listeners {
    Slider volumeSlider;
    MediaPlayer player;
    Stage primaryStage;
    TitledPane commentaryPanel;
    Slider timeSlider;

    public Listeners(Slider volumeSlider, MediaPlayer player, Stage primaryStage, TitledPane commentaryPanel, Slider timeSlider){
        this.volumeSlider = volumeSlider;
        this.player = player;
        this.primaryStage = primaryStage;
        this.commentaryPanel = commentaryPanel;
        this.timeSlider = timeSlider;


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


    }

}
