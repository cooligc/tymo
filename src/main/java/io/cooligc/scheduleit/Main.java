package io.cooligc.scheduleit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SchedulerService schedulerService = new SchedulerService();
        try {
            schedulerService.start();
            schedulerService.registerJobs();

            // Keep the application running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                schedulerService.stop();
            }));

            // Wait indefinitely
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error starting application", e);
        }
    }
}