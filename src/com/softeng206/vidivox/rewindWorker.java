package com.softeng206.vidivox;

import javafx.concurrent.Task;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;


/**
 * Created by jay on 19/09/15.
 */
public class rewindWorker extends Task<Void> {

    private MediaView mediaView;

    public rewindWorker(MediaView mview){
        this.mediaView = mview;
    }

    public void runTask() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected Void call() throws Exception {
        MediaPlayer mplayer = mediaView.getMediaPlayer();

        while (!isCancelled()) {
            mplayer.setMute(true);
            mplayer.seek(mplayer.getCurrentTime().subtract(Duration.seconds(1.5)));
            Thread.sleep(200);
        }


        return null;
    }
}
