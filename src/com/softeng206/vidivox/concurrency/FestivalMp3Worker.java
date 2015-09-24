package com.softeng206.vidivox.concurrency;

import java.io.File;

/**
 * @author Jay Pandya
 */
public class FestivalMp3Worker extends BashWorker {
    private File destination;
    private String message;

    public FestivalMp3Worker(String message, File destination) {
        this.destination = destination;
        this.message = message;
    }

    protected String getBashCommand() {
        // If we get here, the overwrite option in the file chooser was accepted
        if (destination.exists()) {
            destination.delete();
        }

        // FFMPEG command from Nasser's followup to https://piazza.com/class/icl94md3xuv6n9?cid=92
        // Makes temporary wav with text2wave, then converts to MP3 using ffmpeg
        return "rm -f /tmp/vidivoxout.wav && echo \"" + escapeChars(message) + "\" | text2wave -o /tmp/vidivoxout.wav && " +
                "ffmpeg -i /tmp/vidivoxout.wav -f mp3 \"" + escapeChars(destination.getAbsolutePath()) +
                "\" && rm /tmp/vidivoxout.wav";
    }

    protected int getKillPID(int mainPid) {
        // TODO: analyse text2wave / ffmpeg process tree, if we want to have cancel functionality
        return mainPid;
    }
}
