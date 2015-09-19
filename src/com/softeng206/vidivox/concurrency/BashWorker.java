package com.softeng206.vidivox.concurrency;

import javafx.concurrent.Task;

import java.lang.reflect.Field;

/**
 * @author Harsh Chokshi
 */
abstract public class BashWorker extends Task<Integer> {
    private int processId;

    public void runTask() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    protected Integer call() throws Exception {
        // Bash is not available on windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return 0;
        }

        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", getBashCommand());

        try {
            Process mainProcess = builder.start();

            Field pidField = mainProcess.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            processId = (int)pidField.get(mainProcess);

            while (mainProcess.isAlive()) {
                if (isCancelled()) {
                    try {
                        Runtime.getRuntime().exec("kill -INT " + getKillPID(processId));
                    } catch (Exception f) {}
                }
            }

            return mainProcess.exitValue();
        } catch (Exception e) {
            System.out.printf("%s: %s\n", e.getClass(), e.getMessage());
            return 1;
        }
    }

    protected abstract String getBashCommand();
    protected abstract int getKillPID(int mainPid);
}
