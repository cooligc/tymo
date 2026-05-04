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

If the artifact is published to Maven Central, no extra repository configuration is required.

If you are consuming a snapshot build before it is released, add the Sonatype OSSRH snapshots repository:

```xml
<repositories>
  <repository>
    <id>ossrh-snapshots</id>
    <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

### Create a scheduled task

```java
package com.example;

import io.cooligc.tymo.Job;
import io.cooligc.tymo.ScheduledTask;

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

### Example application

An example app is available in the `example/` directory.

```bash
cd example
mvn -DskipTests exec:java
```

### Building from source

```bash
mvn clean package
```

## Usage

### Creating Scheduled Tasks

Create a class annotated with `@Job` and add methods annotated with `@ScheduledTask`:

```java
package io.cooligc.tymo;

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

To release a new version to Maven Central using the configured release plugins:

1. Update the version in `pom.xml` to a non-SNAPSHOT version.
2. Create and configure a GPG key for artifact signing.
3. Ensure your Maven `settings.xml` contains OSSRH credentials and GPG configuration.
4. Run the release goals:

```bash
mvn release:prepare release:perform -P release
```

5. If you need to set a specific version first, use:

```bash
mvn versions:set -DnewVersion=1.0.1
```

6. Login to OSSRH and release the staged artifacts if the process does not auto-release.

### Repository details

The published JAR is intended for Maven Central. Consumers can import it with the standard Maven Central repository settings:

```xml
<repository>
  <id>central</id>
  <name>Maven Central</name>
  <url>https://repo1.maven.org/maven2/</url>
</repository>
```

In most Maven setups, this repository is configured by default, so only the dependency block is required.

## Creating an issue

If you find a bug, want a feature, or need improvement in the scheduler, open an issue using the templates in `.github/ISSUE_TEMPLATE/`.

- Use **Bug report** for unexpected behavior or defects.
- Use **Feature request** for enhancements or new capabilities.

Provide:

- a clear summary
- steps to reproduce (for bugs)
- expected vs actual behavior
- any relevant logs, configuration, or environment details

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

For production deployment, configure the database URL in LockManager to point to a shared database accessible by all hosts.