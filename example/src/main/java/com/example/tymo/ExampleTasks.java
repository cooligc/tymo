package com.example.tymo;

import io.cooligc.tymo.Job;
import io.cooligc.tymo.ScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Job
public class ExampleTasks {
    private static final Logger logger = LoggerFactory.getLogger(ExampleTasks.class);

    @ScheduledTask(fixedDelay = 5000)
    public void fixedDelayTask() {
        logger.info("Fixed delay task executed");
    }

    @ScheduledTask(fixedDelay = 10000)
    public void anotherTask() {
        logger.info("Another task executed");
    }
}
