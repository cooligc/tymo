# Tymo

<p align="center">
  <img src="logo.png" alt="Tymo Logo" width="120" height="120">
</p>

<p align="center">
  <strong>A lightweight Java scheduling framework with custom annotations and high availability support</strong>
</p>

<p align="center">
  <a href="https://github.com/cooligc/tymo/actions/workflows/ci.yml">
    <img src="https://github.com/cooligc/tymo/actions/workflows/ci.yml/badge.svg" alt="Build Status">
  </a>
  <a href="https://github.com/cooligc/tymo/actions/workflows/gh-pages.yml">
    <img src="https://github.com/cooligc/tymo/actions/workflows/gh-pages.yml/badge.svg" alt="GitHub Pages">
  </a>
  <a href="https://search.maven.org/artifact/io.cooligc/tymo">
    <img src="https://img.shields.io/badge/Maven%20Central-1.0.0-blue.svg" alt="Maven Central">
  </a>
  <a href="https://opensource.org/licenses/MIT">
    <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT">
  </a>
</p>

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

### Add to your project

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.cooligc</groupId>
    <artifactId>tymo</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Create a scheduled task

```java
package com.example;

import io.cooligc.scheduleit.Job;
import io.cooligc.scheduleit.ScheduledTask;
import io.cooligc.scheduleit.SchedulerService;

@Job
public class MyTasks {
    @ScheduledTask(fixedDelay = 5000)
    public void myTask() {
        System.out.println("Task executed!");
    }
}
```

### Run the scheduler

```java
SchedulerService scheduler = new SchedulerService();
scheduler.start();
scheduler.registerJobs();
```

### Building from source

```bash
mvn clean package
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

## Publishing to Maven Central

To release a new version to Maven Central:

1. Update version in `pom.xml` to a non-SNAPSHOT version
2. Create a GPG key and distribute to keyservers
3. Configure Maven settings with OSSRH credentials
4. Run: `mvn clean deploy -P release`
5. Login to OSSRH and release the staged artifacts

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

For production deployment, configure the database URL in LockManager to point to a shared database accessible by all hosts.