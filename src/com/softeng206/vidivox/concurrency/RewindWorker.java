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
            Thread.sleep(200);
        }

        return null;
    }
}
