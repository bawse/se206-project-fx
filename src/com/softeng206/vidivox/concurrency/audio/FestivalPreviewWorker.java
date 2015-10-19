package com.softeng206.vidivox.concurrency.audio;

import com.softeng206.vidivox.concurrency.audio.BashWorker;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Jay Pandya
 */
public class FestivalPreviewWorker extends BashWorker {
    private String message;

    public FestivalPreviewWorker(String message) {
        this.message = message;
    }

    protected String getBashCommand() {
        // If the user enters input with special characters, then they must be escaped.
        // If this is not done, then there may not be any output from festival.
        return "echo \"" + escapeChars(message) + "\" | festival --tts";
    }

    protected int getKillPID(int mainPid) {
        try {
            ProcessBuilder cancelBuilder = new ProcessBuilder("/bin/bash", "-c", "pstree -p " + mainPid);
            Process process = cancelBuilder.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdout.readLine();

            // Extract number between play( ) giving PID of aplay / play
            line = line.split("play\\(")[1].split("\\)")[0];

            return Integer.parseInt(line);
        } catch (Exception f) {}

        throw new RuntimeException("Could not evaluate ID of festival player process.");
    }
}
