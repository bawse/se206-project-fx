package com.softeng206.vidivox.concurrency;

import java.io.File;

/**
 * @author Harsh Chokshi
 */
public class VideoRenderWorker extends BashWorker {
    private File audio, destination, video;

    public VideoRenderWorker(File video, File audio, File destination) {
        this.audio = audio;
        this.destination = destination;
        this.video = video;
    }

    protected String getBashCommand() {
        return "ffmpeg -i \"" + video.getAbsolutePath() + "\" -i \"" + audio.getAbsolutePath() + "\" -acodec aac " +
                "-vcodec libx264 -filter_complex \" [1:0] apad \" -f mp4 -strict -2 -shortest \"" +
                destination.getAbsolutePath() + "\"";
    }

    protected int getKillPID(int mainPid) {
        return mainPid;
    }
}
