# Spring Batch Monitor Plugin

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2022.*+-blue.svg)](https://www.jetbrains.com/idea/)
[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://openjdk.java.net/)

A comprehensive Spring Batch monitoring plugin for IntelliJ IDEA that provides real-time job execution monitoring and management capabilities directly within your IDE.

English | [中文](README_CHINESE.md)

## ✨ Features

### 🎯 Core Functionality
- **Direct Database Connection** - No additional backend services required
- **Multi-Database Support** - MySQL, PostgreSQL, Oracle, SQL Server, H2, SQLite
- **Real-time Monitoring** - Live job and step execution tracking
- **Chinese Interface** - Fully localized Chinese user interface

### 📊 Monitoring Capabilities
- **Job Execution History** - Complete execution records with detailed information
- **Step-Level Analysis** - Individual step execution statistics and performance metrics
- **Advanced Search & Filtering** - Search by job name, status, time range, and execution ID
- **Statistical Reports** - Success rates, execution trends, and performance analytics

### 🔧 Management Features
- **Dynamic Data Source Configuration** - Add, edit, and test database connections
- **Real-time Configuration Updates** - Instant updates across all panels
- **Connection Testing** - Built-in database connectivity validation
- **Multi-Environment Support** - Manage multiple Spring Batch environments

## 🚀 Quick Start

### Installation
1. Download the plugin from JetBrains Marketplace
2. Install via IntelliJ IDEA: `Settings` → `Plugins` → `Marketplace` → Search "Spring Batch Monitor"
3. Restart IntelliJ IDEA

### Configuration
1. Open the Spring Batch Monitor tool window (View → Tool Windows → Spring Batch Monitor)
2. Navigate to the "数据源配置" (Data Source Configuration) tab
3. Click "添加数据源" (Add Data Source) to configure your database connection
4. Test the connection and save

### Usage
1. **Job Monitoring**: Switch to "作业列表" (Job List) tab to view job executions
2. **Step Analysis**: Use "步骤列表" (Step List) tab for detailed step execution analysis
3. **Statistics**: Check "统计分析" (Statistics) tab for comprehensive reports
4. **Search & Filter**: Use the search fields to find specific jobs or steps

## 📋 Requirements

- **IntelliJ IDEA**: 2022.1 or later (Community/Ultimate Edition) - Supports up to 2025.1.2
- **Java**: 11 or later
- **Database Access**: Connection to Spring Batch metadata tables
- **Required Tables**: `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION`, `BATCH_JOB_INSTANCE`

## 🗄️ Supported Databases

| Database | Driver | Default Port |
|----------|--------|--------------|
| MySQL | `com.mysql.cj.jdbc.Driver` | 3306 |
| PostgreSQL | `org.postgresql.Driver` | 5432 |
| Oracle | `oracle.jdbc.OracleDriver` | 1521 |
| SQL Server | `com.microsoft.sqlserver.jdbc.SQLServerDriver` | 1433 |
| H2 | `org.h2.Driver` | - |
| SQLite | `org.sqlite.JDBC` | - |

## 🎨 User Interface

### Main Panels
- **欢迎页面** (Welcome) - Plugin overview and quick access
- **数据源配置** (Data Source Config) - Database connection management
- **作业列表** (Job List) - Job execution monitoring with advanced filtering
- **步骤列表** (Step List) - Step execution analysis and statistics
- **作业详情** (Job Details) - Detailed job execution information
- **统计分析** (Statistics) - Comprehensive reporting and analytics

### Key Features
- **Theme Support** - Full compatibility with IntelliJ IDEA light and dark themes
- **Real-time Validation** - Instant input validation with visual feedback
- **Responsive Design** - Optimized layout for different screen sizes
- **Intuitive Navigation** - Easy switching between different monitoring views

## 🔍 Advanced Search

### Job List Filtering
- **Job Name**: Filter by exact or partial job name
- **Status**: Filter by execution status (COMPLETED, FAILED, STARTED, etc.)
- **Time Range**: Filter by start/end time with flexible date formats
- **Date Formats**: Supports `2020-06-20` and `2020-06-20 14:30:00` formats

### Step List Filtering
- **Step Name**: Filter by step name
- **Job Execution ID**: Filter by parent job execution
- **Status**: Filter by step execution status
- **Time Range**: Flexible date/time filtering

## 📈 Statistics & Analytics

### Available Metrics
- **Job Execution Statistics**: Total, successful, failed, and running jobs
- **Step Execution Statistics**: Step-level success rates and performance
- **Data Processing Metrics**: Read/write counts, skip counts, and processing statistics
- **Trend Analysis**: Historical execution patterns and performance trends

## 🛠️ Development

### Building from Source
```bash
git clone https://github.com/jackssybin/spring-batch-monitor-plugin.git
cd spring-batch-monitor-plugin
gradle clean buildPlugin
```

The built plugin will be available at `build/distributions/spring-batch-monitor-plugin-1.0.0.zip`

### Project Structure
```
spring-batch-monitor-plugin/
├── src/main/java/com/springbatch/monitor/
│   ├── model/          # Data models and configurations
│   ├── services/       # Business logic and database services
│   └── ui/             # User interface components
├── src/main/resources/
│   ├── META-INF/       # Plugin configuration
│   └── icons/          # UI icons and resources
└── build.gradle        # Build configuration
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/jackssybin/spring-batch-monitor-plugin/issues)
- **Documentation**: [Plugin Documentation](https://github.com/jackssybin/spring-batch-monitor-plugin/wiki)

## 🏷️ Version History

### v1.0.0 (Initial Release)
- Complete Spring Batch monitoring solution
- Multi-database support with connection testing
- Advanced search and filtering capabilities
- Real-time job and step execution monitoring
- Comprehensive statistical reporting
- Fully localized Chinese interface
- Direct database connection (no backend required)

---

**Made with ❤️ for the Spring Batch community**
