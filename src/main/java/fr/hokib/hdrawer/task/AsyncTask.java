package fr.hokib.hdrawer.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AsyncTask implements Runnable {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledFuture<?> scheduledTask;

    public AsyncTask(final long period) {
        this.scheduledTask = EXECUTOR_SERVICE.scheduleAtFixedRate(this, period, period, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        this.scheduledTask.cancel(true);
    }

}
