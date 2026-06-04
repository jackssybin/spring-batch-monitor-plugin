# Contributing

Thanks for your interest in Spring Batch Monitor Plugin. Contributions are welcome across code, documentation, testing, issue reports, and compatibility feedback.

## Development Setup

Requirements:

- JDK 17 or later.
- IntelliJ IDEA 2022.1 or later.
- Windows users can build with `gradlew.bat`.

Build and check the project:

```powershell
.\gradlew.bat clean check buildPlugin
```

The plugin ZIP is generated under `build/distributions/`.

## Contribution Areas

- IntelliJ IDEA plugin UI and workflow improvements.
- Spring Batch metadata query correctness.
- Multi-database compatibility for MySQL, PostgreSQL, Oracle, SQL Server, H2, and SQLite.
- Date/time parsing and filtering behavior.
- Documentation and bilingual content.
- Security hardening for credential handling and database access.

## Pull Request Guidelines

- Keep changes focused and explain the user-facing impact.
- Include tests when changing parsing, query, or service behavior.
- Avoid committing credentials, database URLs, local IDE files, or build artifacts.
- Update `README.md`, `README_CHINESE.md`, or `CHANGELOG.md` when behavior changes.
- Run `.\gradlew.bat clean check buildPlugin` before opening a pull request.

## Issue Reports

When reporting a bug, please include:

- IntelliJ IDEA version.
- Plugin version or commit.
- Operating system.
- Database type and version.
- Spring Batch version if known.
- Steps to reproduce and expected behavior.

Please do not include database passwords, API keys, private hostnames, or sensitive batch data in public issues.
