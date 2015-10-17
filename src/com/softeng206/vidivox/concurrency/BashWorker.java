package com.softeng206.vidivox.concurrency;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.lang.reflect.Field;

/**
 * @author Jay Pandya
 */
abstract public class BashWorker extends Task<Void> {
    private int processId;

    public void runTask() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void setOnFinished(EventHandler<WorkerStateEvent> value) {
        setOnCancelled(value);
        setOnFailed(value);
        setOnSucceeded(value);
    }

    protected Void call() throws Exception {
        // Bash is not available on windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return null;
        }

        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", getBashCommand());

        try {
            Process mainProcess = builder.start();

            // Reflection "hack" as seen in lectures to get access of process ID.
            Field pidField = mainProcess.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            processId = (int)pidField.get(mainProcess);

            while (mainProcess.isAlive()) {
                if (isCancelled()) {
                    try {
                        // if the cancel command is detected, then the main process will get cancelled.
                        Runtime.getRuntime().exec("kill -INT " + getKillPID(processId));
                    } catch (Exception f) {}
                }
            }

            return null;
        } catch (Exception e) {
            System.out.printf("%s: %s\n", e.getClass(), e.getMessage());
            return null;
        }
    }

    /**
     * Escape bash special characters " and $
     */
    public static String escapeChars(String text) {
        /* Prevents the festival functionality being broken if the user decides to use
        special characters in their input.
          */
        return text.replaceAll("\"","\\\\\"").replaceAll("\\$", "\\\\\\$");
    }

    protected abstract String getBashCommand();
    protected abstract int getKillPID(int mainPid);
}
