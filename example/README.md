# Tymo Example

This example demonstrates how a Java developer can use the Tymo scheduling framework.

## Build and run

1. Install the library locally:

```bash
cd ..
mvn clean install -DskipTests
```

2. Run the example:

```bash
cd example
mvn -DskipTests exec:java
```

## What this example shows

- Creating a `@Job` class containing scheduled methods
- Annotating methods with `@ScheduledTask` and using `fixedDelay`
- Starting `SchedulerService`
- Registering jobs from the classpath
- Cleanly stopping the scheduler with a shutdown hook

## Example behavior

`ExampleTasks` logs a message every 5 seconds and another message every 10 seconds.
