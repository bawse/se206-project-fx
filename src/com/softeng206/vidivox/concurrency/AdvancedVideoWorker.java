package com.softeng206.vidivox.concurrency;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jay on 1/10/15.
 */
public class AdvancedVideoWorker extends Task<Void> {
    private File selectedAudio;
    private File selectedVideo;
    private File destination;
    private String time;
    private int option;
    private double duration;
    private String volumeString;

    public AdvancedVideoWorker(File audio, File video, File destination, String time, int option, double duration) {
        this.selectedAudio = audio;
        this.selectedVideo = video;
        this.destination = destination;
        this.time = time;
        this.option = option;
        this.duration = duration;
    }
    public AdvancedVideoWorker(File audio, File video, File destination, String time, String volumeInput, int option, double duration) {
        this.selectedAudio = audio;
        this.selectedVideo = video;
        this.destination = destination;
        this.time = time;
        this.option = option;
        this.duration = duration;
        this.volumeString = volumeInput;
    }


    public void runTask() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected Void call() throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return null;
        }

        // If we get here, the overwrite option in the file chooser was accepted
        if (destination.exists()) {
            destination.delete();
        }

        String command = getCommand();
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
        try {
            Process mainProcess = builder.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(mainProcess.getErrorStream()));

            Field pidField = mainProcess.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            int processId = (int) pidField.get(mainProcess);

            while (mainProcess.isAlive()) {
                if (isCancelled()) {
                    try {
                        Runtime.getRuntime().exec("kill -INT " + processId);
                    } catch (Exception f) {
                    }
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
        }catch (Exception e) {
            System.out.printf("%s: %s\n", e.getClass(), e.getMessage());
            return null;
        }


        return null;
    }


    public String getCommand() {
        String command = "";

        long audioDelay = convertTimeToMilliseconds();

        if (option == 1) {

//        String command = "ffmpeg -i " + BashWorker.escapeChars(selectedVideo.getAbsolutePath()) + " -i " + BashWorker.escapeChars(selectedAudio.getAbsolutePath()) +
//                " -filter_complex \\\"[0:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=mono[aud1];[1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=mono[bud2];[bud2]adelay=5000[aud2];[aud1][aud2]amix=inputs=2\\\"  -map 0:v " + BashWorker.escapeChars(destination.getAbsolutePath());


            command = "ffmpeg -i \"" + BashWorker.escapeChars(selectedVideo.getAbsolutePath()) + "\" -i \"" +
                    BashWorker.escapeChars(selectedAudio.getAbsolutePath()) + "\" " + "-filter_complex \"[0:a]aformat=sample_" +
                    "fmts=fltp:sample_rates=44100:channel_layouts=mono[aud1];[1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=mono[bud2];" +
                    "[bud2]adelay=" + audioDelay + "[aud2];[aud1][aud2]amix=inputs=2\" " + "-map 0:v \"" + BashWorker.escapeChars(destination.getAbsolutePath()) + "\"";
        }
        else if (option == 2){
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date();
            String tempFileName = BashWorker.escapeChars(System.getProperty("user.home") + "/" + dateFormat.format(date) + ".mp4");

            volumeString = getCorrectFormat(volumeString);

            String tempCommand = "ffmpeg -i \"" + BashWorker.escapeChars(selectedVideo.getAbsolutePath()) + "\"" + " -vcodec copy -af \"volume=" + volumeString + "\" "
                    + "\"" + tempFileName + "\" " + "&& ";
            String removeTempString = " && rm " + tempFileName;

            command = tempCommand + "ffmpeg -i \"" + tempFileName + "\" -i \"" +
                    BashWorker.escapeChars(selectedAudio.getAbsolutePath()) + "\" " + "-filter_complex \"[0:a]aformat=sample_" +
                    "fmts=fltp:sample_rates=44100:channel_layouts=mono[aud1];[1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=mono[bud2];" +
                    "[bud2]adelay=" + audioDelay + "[aud2];[aud1][aud2]amix=inputs=2\" " + "-map 0:v \""
                    + BashWorker.escapeChars(destination.getAbsolutePath()) + "\"" + removeTempString;

        }
        return command;
    }

    private long convertTimeToMilliseconds() {
        String[] split = time.split(":");
        int minutes = Integer.parseInt(split[0]);
        int seconds = Integer.parseInt(split[1]);

        int totalSeconds = seconds + (minutes * 60);

        return totalSeconds*1000;
    }

    private String getCorrectFormat(String string){
        String[] substring = string.split("d");
        return substring[0] + "dB";
    }

}
