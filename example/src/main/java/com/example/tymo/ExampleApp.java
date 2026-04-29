package com.example.tymo;

import io.cooligc.tymo.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleApp {
    private static final Logger logger = LoggerFactory.getLogger(ExampleApp.class);

    public static void main(String[] args) {
        SchedulerService schedulerService = new SchedulerService();

        try {
            schedulerService.start();
            schedulerService.registerJobs();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                schedulerService.stop();
            }));

            logger.info("Tymo example is running. Press Ctrl+C to stop.");
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error running Tymo example", e);
        }
    }
}
