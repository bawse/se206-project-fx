package com.softeng206.vidivox.concurrency;

import javafx.concurrent.Task;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Created by jay on 19/09/15.
 */
public class RewindWorker extends Task<Void> {
    private MediaPlayer player;

    public RewindWorker(MediaPlayer player) {
        this.player = player;
    }

    public void runTask() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected Void call() throws Exception {
        while (!isCancelled()) {
            player.setMute(true);
            player.seek(player.getCurrentTime().subtract(Duration.seconds(1.5)));
            // 200ms Thread sleep to give the effect of GUI responsiveness. Without this, the GUI seems to "freeze"
            // when in reality it is just rewinding really fast.
            Thread.sleep(200);

            // If the video is currently rewinding, and close to the start of the video, then assume that the
            // rewind has reached the start of the video. Hence, the video is played in this scenario.
            if (player.getCurrentTime().toSeconds() <= 1.0){
                player.play();
                break;
            }
        }

        return null;
    }
}
