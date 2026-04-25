package io.cooligc.scheduleit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Job
public class SampleTasks {
    private static final Logger logger = LoggerFactory.getLogger(SampleTasks.class);

    @ScheduledTask(fixedDelay = 1000) // every 5 seconds
    public void fixedDelayTask() {
        logger.info("Fixed delay task executed");
    }

    @ScheduledTask(fixedDelay = 10000) // every 10 seconds
    public void anotherTask() {
        logger.info("Another task executed");
    }
}