# Contributing to Schedule It

Thank you for your interest in contributing to Schedule It! This document provides guidelines for contributing to the project.

## Code of Conduct

This project follows a code of conduct to ensure a welcoming environment for all contributors.

## How to Contribute

### Reporting Issues

- Use the GitHub issue tracker
- Provide detailed steps to reproduce
- Include environment information (Java version, OS, etc.)

### Submitting Changes

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass: `mvn test`
6. Commit your changes: `git commit -am 'Add my feature'`
7. Push to the branch: `git push origin feature/my-feature`
8. Submit a pull request

### Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Keep methods small and focused
- Use SLF4J for logging

### Testing

- Write unit tests for new features
- Ensure existing tests still pass
- Test edge cases and error conditions
- Consider integration tests for complex features

## Development Setup

See [DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md) for detailed setup instructions.

## Commit Messages

Use clear, descriptive commit messages:

```
feat: add cron expression support
fix: resolve deadlock in lock manager
docs: update README with new features
```

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT).</content>
<parameter name="filePath">/Users/cooligc/Documents/codes/scheduler-ha/CONTRIBUTING.md