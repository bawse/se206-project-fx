package com.softeng206.vidivox.concurrency;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 * @author Harsh Chokshi
 */
public class VideoRenderWorker extends Task<Void> {
    private File audio, destination, video;
    private double duration; // video duration in milliseconds

    public void runTask() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public VideoRenderWorker(File video, File audio, File destination, double duration) {
        this.audio = audio;
        this.destination = destination;
        this.duration = duration;
        this.video = video;
    }

    protected Void call() throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return null;
        }

        /*
         * apad filter setup from below link, to pad audio track with silence to match video length
         * http://superuser.com/questions/801547/ffmpeg-add-audio-but-keep-video-length-the-same-not-shortest
         */
        String command = "ffmpeg -i \"" + video.getAbsolutePath() + "\" -i \"" + audio.getAbsolutePath() + "\" -acodec aac " +
                "-vcodec libx264 -filter_complex \" [1:0] apad \" -f mp4 -strict -2 -shortest \"" +
                destination.getAbsolutePath() + "\"";
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);

        try {
            Process mainProcess = builder.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(mainProcess.getErrorStream()));

            Field pidField = mainProcess.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            int processId = (int)pidField.get(mainProcess);

            while (mainProcess.isAlive()) {
                if (isCancelled()) {
                    try {
                        Runtime.getRuntime().exec("kill -INT " + processId);
                    } catch (Exception f) {}
                }

                if (stdout.ready()) {
                    String line = stdout.readLine();
                    if (line.matches("(.*)time=(.*)")) {
                        // Line will have completed duration in format HH:MM:SS.MS
                        line = line.split("time=")[1].split(" bitrate=")[0];

                        // pieces[0] = hours, pieces[1] = minutes, pieces[2] = seconds.milliseconds
                        String[] pieces = line.split(":");
                        // seconds[0] = seconds, seconds[1] = milliseconds
                        String[] seconds = pieces[2].split("\\.");
                        double progress = 0;

                        // Hours and minutes progress, in seconds
                        progress += 3600 * Integer.parseInt(pieces[0]) + 60 * Integer.parseInt(pieces[1]);
                        progress += Integer.parseInt(seconds[0]); // Add seconds
                        progress = progress * 1000 + Integer.parseInt(seconds[1]); // Convert to ms and add ms

                        updateProgress(progress, duration);
                    }
                }
            }

            return null;
        } catch (Exception e) {
            System.out.printf("%s: %s\n", e.getClass(), e.getMessage());
            return null;
        }
    }
}
