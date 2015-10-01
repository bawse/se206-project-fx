package com.softeng206.vidivox.concurrency;

import javafx.concurrent.Task;

/**
 * Created by jay on 1/10/15.
 */
public class AdvancedVideoWorker extends Task<Void> {

    public void runTask() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }



    @Override
    protected Void call() throws Exception {
        return null;
    }
}
