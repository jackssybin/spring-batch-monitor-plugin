# Security Policy

Spring Batch Monitor Plugin connects directly from IntelliJ IDEA to user-configured Spring Batch metadata databases. The project treats credential handling, database access, and metadata exposure as core security concerns.

## Supported Versions

| Version | Supported |
| --- | --- |
| 1.0.x | Yes |

## Reporting a Vulnerability

Please do not disclose security vulnerabilities in public issues before they have been reviewed.

To report a vulnerability:

1. Open a private report through GitHub security reporting if available.
2. If private reporting is not available, contact the maintainer through the email listed on the GitHub profile or repository metadata.
3. Include a concise description, affected versions, reproduction steps, and potential impact.

The maintainer will review reports as soon as practical and coordinate a fix or mitigation when the report is valid.

## Security Expectations

- Use read-only database credentials whenever possible.
- Limit database permissions to Spring Batch metadata tables.
- Use SSL/TLS for remote database connections when supported.
- Do not commit connection strings, credentials, logs, or sensitive metadata.
- Rotate credentials if a local development environment may have been exposed.

## Out of Scope

- Vulnerabilities caused by intentionally sharing database credentials.
- Issues in third-party database servers, JDBC drivers, or IntelliJ IDEA itself unless the plugin uses them unsafely.
- Reports without enough detail to reproduce or reason about the issue.
