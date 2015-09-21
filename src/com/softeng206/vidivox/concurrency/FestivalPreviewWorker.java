package com.softeng206.vidivox.concurrency;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Harsh Chokshi
 */
public class FestivalPreviewWorker extends BashWorker {
    private String message;

    public FestivalPreviewWorker(String message) {
        this.message = message;
    }

    protected String getBashCommand() {
        // TODO: escape message for characters like "
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
