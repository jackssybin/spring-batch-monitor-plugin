# Spring Batch Monitor Plugin

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2022.3%2B-blue.svg)](https://www.jetbrains.com/idea/)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)

Spring Batch Monitor Plugin 是一个开源 IntelliJ IDEA 插件，用于直接从 Spring Batch 元数据数据库中查看 job 和 step 的执行情况。它可以帮助开发者在 IDE 内理解批处理状态、失败原因、吞吐指标和历史执行记录，不需要额外部署监控后端。

[English](README.md) | 中文

## 项目价值

Spring Batch 会在元数据表中保存丰富的执行状态，但开发者通常需要离开 IDE，手写 SQL，或临时搭建监控页面才能回答一些常见问题：

- 最近哪些 job 失败了？
- 哪个 step 执行慢，或者反复失败？
- 读取、写入、过滤、跳过的数据量是多少？
- 不同批处理环境之间的执行表现是否一致？

这个插件把这些信息放回 IDE 内，方便在开发、调试和支持场景中快速检查批处理执行状态。

## 功能特性

- 直接连接数据库，不需要额外后端服务。
- 查看 job 执行历史、状态、时间和详细信息。
- 查看 step 级别的读写、跳过和性能指标。
- 按 job 名称、状态、执行 ID 和时间范围搜索过滤。
- 支持多环境数据源配置。
- 保存数据源前可以测试连接。
- 提供成功率、执行趋势和性能信号等统计视图。
- 中文界面和中英文双语文档。

## 支持的数据库

| 数据库 | 驱动 | 默认端口 |
| --- | --- | --- |
| MySQL | `com.mysql.cj.jdbc.Driver` | 3306 |
| PostgreSQL | `org.postgresql.Driver` | 5432 |
| Oracle | `oracle.jdbc.OracleDriver` | 1521 |
| SQL Server | `com.microsoft.sqlserver.jdbc.SQLServerDriver` | 1433 |
| H2 | `org.h2.Driver` | 不适用 |
| SQLite | `org.sqlite.JDBC` | 不适用 |

插件需要访问标准 Spring Batch 元数据表，包括 `BATCH_JOB_INSTANCE`、`BATCH_JOB_EXECUTION` 和 `BATCH_STEP_EXECUTION`。

## 典型使用场景

- 在 IntelliJ IDEA 内调试 Spring Batch 失败任务。
- 对比开发、测试和类生产环境中的 job 执行表现。
- 在性能调优时查看 step 级别吞吐和跳过数据量。
- 为运维和支持场景提供轻量级 IDE 内元数据视图。
- 修改 job 或 step 代码时同步查看历史执行状态。

## 快速开始

### 安装

1. 从源码构建插件，或使用已发布的插件包。
2. 在 IntelliJ IDEA 中打开 `Settings` -> `Plugins` -> `Install Plugin from Disk`。
3. 选择生成的插件 ZIP 文件。
4. 重启 IntelliJ IDEA。

### 配置

1. 打开 `View` -> `Tool Windows` -> `Spring Batch Monitor`。
2. 进入数据源配置面板。
3. 添加 Spring Batch 元数据数据库连接。
4. 测试连接并保存。
5. 查看 job、step、详情或统计视图。

## 安全模型

插件从 IDE 直接连接你配置的数据库。它不需要托管后端，也不会有意把元数据发送给第三方服务。

推荐用法：

- 尽量使用只读数据库用户。
- 将权限限制在 Spring Batch 元数据表范围内。
- 远程数据库连接优先使用 SSL/TLS。
- 不要提交数据库账号、密码或连接串。
- 如果本地开发机器存在泄露风险，请及时轮换凭据。

安全问题请按照 [SECURITY.md](SECURITY.md) 中的流程报告。

## 本地开发

环境要求：

- JDK 17 或更高版本。
- IntelliJ IDEA 2022.3 或更高版本。
- Windows 下可使用 `gradlew.bat`。

从源码构建：

```powershell
git clone https://github.com/jackssybin/spring-batch-monitor-plugin.git
cd spring-batch-monitor-plugin
.\gradlew.bat clean buildPlugin
```

插件 ZIP 会生成在 `build/distributions/` 目录下。

运行检查：

```powershell
.\gradlew.bat clean check buildPlugin
```

## 项目结构

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

- 补充日期解析和数据库查询行为的自动化测试。
- 增加 Spring Batch 元数据示例，用于本地兼容性测试。
- 扩展多数据库兼容性验证。
- 持续完善中英文文档。
- 补充常见连接和 schema 问题排查指南。
- 准备可重复的签名发布和 JetBrains Marketplace 发布流程。

## Codex for OSS 准备情况

这个项目作为独立开源开发者工具维护。Codex 支持将用于改进：

- IntelliJ 插件代码和数据库访问代码的自动化审查。
- 查询、过滤、日期解析和 UI 工作流逻辑的测试生成。
- 多数据库兼容性场景验证。
- 凭据处理和数据库连接行为的安全审查。
- 中英文文档、issue 分析和发布 QA。

目标是让这个 Spring Batch 开发者工具更可靠、更安全、更容易维护，也更容易被社区采用。

申请说明和可复制的表单草稿见 [docs/CODEX_FOR_OSS.md](docs/CODEX_FOR_OSS.md)。

## 参与贡献

欢迎贡献代码、文档和问题反馈。开发流程和 PR 要求见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## License

本项目基于 [MIT License](LICENSE) 发布。
