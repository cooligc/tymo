# Schedule It

A lightweight Java scheduling framework with custom annotations and high availability support using ScheduledThreadPoolExecutor and database locking.

## Features

- Custom `@ScheduledTask` annotation supporting fixed delay scheduling
- High availability: Only one host executes the task in a distributed environment using database locks
- Thread pool integration via ScheduledThreadPoolExecutor
- Automatic job discovery via classpath scanning
- Executable JAR support

## Requirements

- Java 21 or higher
- Maven 3.6+

## Quick Start

### Building

```bash
mvn clean package
```

### Running

```bash
java -jar target/schedule-it-1.0-SNAPSHOT.jar
```

Or for development:

```bash
mvn exec:java -Dexec.mainClass="io.cooligc.scheduleit.Main"
```

## Usage

### Creating Scheduled Tasks

Create a class annotated with `@Job` and add methods annotated with `@ScheduledTask`:

```java
package io.cooligc.scheduleit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Job
public class MyTasks {
    private static final Logger logger = LoggerFactory.getLogger(MyTasks.class);

    @ScheduledTask(fixedDelay = 5000) // Run every 5 seconds
    public void myTask() {
        logger.info("Task executed at {}", System.currentTimeMillis());
    }

    @ScheduledTask(fixedDelay = 10000, poolName = "custom-pool") // Custom thread pool
    public void anotherTask() {
        logger.info("Another task executed");
    }
}
```

The framework automatically discovers and registers jobs from the classpath.

### High Availability

The application uses database locking to ensure that scheduled tasks are executed by only one instance in a cluster. Multiple instances can run simultaneously, but the lock mechanism prevents duplicate executions.

## Configuration

### Database

By default, uses H2 in-memory database. For production, configure a shared database.

### Thread Pools

Tasks can specify a `poolName` to use different thread pools. Default pool is "default".

## Architecture

- **SchedulerService**: Main scheduler managing tasks and thread pools
- **LockManager**: Handles distributed locking using database
- **@ScheduledTask**: Annotation for method-level scheduling
- **@Job**: Annotation for class-level job marking

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

For production deployment, configure the database URL in LockManager to point to a shared database accessible by all hosts.