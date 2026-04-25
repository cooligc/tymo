# Developer Guide

## Overview

Schedule It is a lightweight Java scheduling framework that provides annotation-based task scheduling with high availability support.

## Architecture

### Core Components

- **SchedulerService**: The main component that manages scheduled tasks, thread pools, and job discovery.
- **LockManager**: Handles distributed locking to ensure single execution in HA environments.
- **Annotations**: `@Job` and `@ScheduledTask` for declarative task configuration.

### Thread Pools

The framework uses `ScheduledThreadPoolExecutor` for task execution. Tasks can specify a `poolName` to use different thread pools:

```java
@ScheduledTask(fixedDelay = 1000, poolName = "fast-tasks")
public void fastTask() {
    // Uses a dedicated thread pool
}
```

## Development Setup

### Prerequisites

- Java 21+
- Maven 3.6+

### Building

```bash
mvn clean compile
```

### Testing

```bash
mvn test
```

### Running

```bash
mvn exec:java
```

## Adding New Features

### Custom Annotations

To add new scheduling annotations:

1. Create the annotation interface
2. Update `SchedulerService.registerJobs()` to handle the new annotation
3. Add processing logic in the scheduling method

### Database Backends

To support different databases:

1. Implement a new `LockManager` or extend the existing one
2. Update database schema creation
3. Ensure thread-safe locking operations

## Code Quality

### Standards

- Use Java 21 features appropriately
- Follow standard Java naming conventions
- Add comprehensive logging
- Write unit tests for new features

### Testing

- Unit tests for core logic
- Integration tests for HA scenarios
- Performance tests for high-throughput scenarios

## Deployment

### JAR Packaging

The project uses Maven Shade plugin to create an executable JAR with all dependencies.

### Configuration

Environment-specific configuration can be added via:

- System properties
- Configuration files
- Environment variables

## Troubleshooting

### Common Issues

1. **Tasks not executing**: Check classpath scanning and annotation presence
2. **HA not working**: Verify database connectivity and lock table creation
3. **Performance issues**: Monitor thread pool usage and adjust pool sizes

### Debugging

Enable debug logging:

```xml
<logger name="io.cooligc.scheduleit" level="DEBUG"/>
```

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for detailed contribution guidelines.</content>
<parameter name="filePath">/Users/cooligc/Documents/codes/scheduler-ha/docs/DEVELOPER_GUIDE.md