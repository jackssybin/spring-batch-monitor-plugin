# Spring Batch Monitor Plugin

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2022.3%2B-blue.svg)](https://www.jetbrains.com/idea/)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)

Spring Batch Monitor Plugin is an open-source IntelliJ IDEA plugin for inspecting Spring Batch job and step executions directly from metadata databases. It helps developers understand batch status, failures, throughput, and execution history from inside the IDE without running a separate monitoring backend.

English | [Chinese](README_CHINESE.md)

## Why This Exists

Spring Batch stores rich execution state in metadata tables, but developers often need to leave the IDE, query databases manually, or build one-off dashboards to answer common questions:

- Which jobs failed recently?
- Which step is slow or repeatedly failing?
- How many records were read, written, filtered, or skipped?
- Are different batch environments producing different execution patterns?

This plugin keeps that workflow close to the code and makes batch operations easier to inspect during development, debugging, and support.

## Features

- Direct database connection with no additional backend service.
- Job execution history with status, timing, and execution details.
- Step-level analysis with read/write/skip metrics.
- Search and filtering by job name, status, execution ID, and time range.
- Multi-environment data source configuration.
- Connection testing before saving data sources.
- Statistics views for success rates, execution trends, and performance signals.
- Chinese user interface and bilingual documentation.

## Supported Databases

| Database | Driver | Default port |
| --- | --- | --- |
| MySQL | `com.mysql.cj.jdbc.Driver` | 3306 |
| PostgreSQL | `org.postgresql.Driver` | 5432 |
| Oracle | `oracle.jdbc.OracleDriver` | 1521 |
| SQL Server | `com.microsoft.sqlserver.jdbc.SQLServerDriver` | 1433 |
| H2 | `org.h2.Driver` | N/A |
| SQLite | `org.sqlite.JDBC` | N/A |

The plugin expects access to standard Spring Batch metadata tables, including `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, and `BATCH_STEP_EXECUTION`.

## Use Cases

- Debug Spring Batch failures from within IntelliJ IDEA.
- Compare job behavior across development, staging, and production-like environments.
- Inspect step-level throughput and skip counts during performance tuning.
- Support operations teams with a lightweight IDE-based view of batch metadata.
- Review historical execution state while changing job or step implementation code.

## Quick Start

### Install

1. Build the plugin from source or install a published release package.
2. In IntelliJ IDEA, open `Settings` -> `Plugins` -> `Install Plugin from Disk`.
3. Select the generated plugin ZIP.
4. Restart IntelliJ IDEA.

### Configure

1. Open `View` -> `Tool Windows` -> `Spring Batch Monitor`.
2. Open the data source configuration panel.
3. Add a Spring Batch metadata database connection.
4. Test the connection and save it.
5. Open the job, step, detail, or statistics views.

## Security Model

The plugin connects directly from the IDE to databases you configure. It does not require a hosted backend and does not intentionally transmit metadata to third-party services.

Recommended usage:

- Use read-only database users whenever possible.
- Scope access to Spring Batch metadata tables.
- Prefer SSL/TLS database connections for remote databases.
- Avoid committing credentials or connection strings.
- Rotate credentials if a local development machine is compromised.

Security issues should be reported through the process in [SECURITY.md](SECURITY.md).

## Development

Requirements:

- JDK 17 or later.
- IntelliJ IDEA 2022.3 or later.
- Gradle wrapper on Windows via `gradlew.bat`.

Build from source:

```powershell
git clone https://github.com/jackssybin/spring-batch-monitor-plugin.git
cd spring-batch-monitor-plugin
.\gradlew.bat clean buildPlugin
```

The plugin ZIP is generated under `build/distributions/`.

Run checks:

```powershell
.\gradlew.bat clean check buildPlugin
```

## Project Structure

```text
spring-batch-monitor-plugin/
├── src/main/java/com/springbatch/monitor/
│   ├── actions/
│   ├── model/
│   ├── models/
│   ├── services/
│   ├── ui/
│   └── utils/
├── src/main/resources/
│   ├── META-INF/plugin.xml
│   └── icons/
├── build.gradle
└── settings.gradle
```

## Roadmap

- Improve automated tests around date parsing and database query behavior.
- Add sample Spring Batch metadata fixtures for local compatibility testing.
- Expand compatibility coverage across supported databases.
- Improve English and Chinese documentation.
- Add clearer troubleshooting guidance for common connection and schema issues.
- Prepare repeatable signed release and JetBrains Marketplace publishing workflows.

## Codex for OSS Readiness

This project is maintained as an independent open-source developer tool. Codex support would be used to improve:

- Automated code review for IntelliJ plugin and database access code.
- Test generation for query, filtering, date parsing, and UI workflow logic.
- Multi-database compatibility scenarios.
- Security review for credential handling and database connection behavior.
- Bilingual documentation, issue triage, and release QA.

The goal is to make a practical Spring Batch developer tool more reliable, safer to maintain, and easier for the community to adopt.

Application notes and copy-ready field drafts are kept in [docs/CODEX_FOR_OSS.md](docs/CODEX_FOR_OSS.md).

## Contributing

Contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) for development workflow and pull request guidance.

## License

This project is released under the [MIT License](LICENSE).
