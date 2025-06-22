package com.springbatch.monitor.services;

import com.springbatch.monitor.model.DataSourceConfig;
import com.springbatch.monitor.models.JobExecution;
import com.springbatch.monitor.models.StepExecution;
import com.springbatch.monitor.utils.DateTimeUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库服务类 - 直接连接数据库查询Spring Batch数据
 */
public class DatabaseService {
    private static final DatabaseService INSTANCE = new DatabaseService();
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    private DatabaseService() {
    }

    public static DatabaseService getInstance() {
        return INSTANCE;
    }

    /**
     * 添加数据源
     */
    public void addDataSource(DataSourceConfig config) {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName(config.getDriverClassName());
            hikariConfig.setJdbcUrl(config.getUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setMaximumPoolSize(5);
            hikariConfig.setMinimumIdle(1);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setIdleTimeout(600000);
            hikariConfig.setMaxLifetime(1800000);

            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            dataSources.put(config.getId(), dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create data source: " + e.getMessage(), e);
        }
    }

    /**
     * 移除数据源
     */
    public void removeDataSource(String dataSourceId) {
        DataSource dataSource = dataSources.remove(dataSourceId);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }

    /**
     * 测试数据源连接
     */
    public boolean testConnection(DataSourceConfig config) {
        try {
            Class.forName(config.getDriverClassName());
            try (Connection conn = DriverManager.getConnection(
                    config.getUrl(), config.getUsername(), config.getPassword())) {
                return conn.isValid(5);
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取数据库表信息
     */
    public List<String> getTableNames(String dataSourceId) {
        List<String> tables = new ArrayList<>();
        DataSource dataSource = dataSources.get(dataSourceId);
        if (dataSource == null) {
            return tables;
        }

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (tableName.toLowerCase().contains("batch")) {
                        tables.add(tableName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    /**
     * 获取作业执行列表
     */
    public List<JobExecution> getJobExecutions(String dataSourceId) {
        List<JobExecution> executions = new ArrayList<>();
        DataSource dataSource = dataSources.get(dataSourceId);
        if (dataSource == null) {
            return executions;
        }

        String sql = "SELECT je.JOB_EXECUTION_ID, je.JOB_INSTANCE_ID, ji.JOB_NAME, " +
                     "je.START_TIME, je.END_TIME, je.STATUS, je.EXIT_CODE, je.EXIT_MESSAGE " +
                     "FROM BATCH_JOB_EXECUTION je " +
                     "JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID " +
                     "ORDER BY je.START_TIME DESC " +
                     "LIMIT 100";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                JobExecution execution = new JobExecution();
                execution.setId(rs.getLong("JOB_EXECUTION_ID"));
                execution.setJobInstanceId(rs.getLong("JOB_INSTANCE_ID"));
                execution.setJobName(rs.getString("JOB_NAME"));
                execution.setStartTime(rs.getTimestamp("START_TIME"));
                execution.setEndTime(rs.getTimestamp("END_TIME"));
                execution.setStatus(rs.getString("STATUS"));
                execution.setExitCode(rs.getString("EXIT_CODE"));
                execution.setExitMessage(rs.getString("EXIT_MESSAGE"));
                executions.add(execution);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return executions;
    }

    /**
     * 搜索作业执行列表
     */
    public List<JobExecution> searchJobExecutions(String dataSourceId, String jobName, String status,
                                                  String startDate, String endDate, String keyword) {
        List<JobExecution> executions = new ArrayList<>();
        DataSource dataSource = dataSources.get(dataSourceId);
        if (dataSource == null) {
            return executions;
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT je.JOB_EXECUTION_ID, je.JOB_INSTANCE_ID, ji.JOB_NAME, ")
                  .append("je.START_TIME, je.END_TIME, je.STATUS, je.EXIT_CODE, je.EXIT_MESSAGE ")
                  .append("FROM BATCH_JOB_EXECUTION je ")
                  .append("JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID ")
                  .append("WHERE 1=1 ");

        List<Object> parameters = new ArrayList<>();

        if (jobName != null && !jobName.trim().isEmpty()) {
            sqlBuilder.append("AND ji.JOB_NAME LIKE ? ");
            parameters.add("%" + jobName.trim() + "%");
        }

        if (status != null && !status.trim().isEmpty()) {
            sqlBuilder.append("AND je.STATUS = ? ");
            parameters.add(status.trim());
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            java.sql.Timestamp startTimestamp = DateTimeUtils.parseStartDateTime(startDate.trim());
            if (startTimestamp != null) {
                sqlBuilder.append("AND je.START_TIME >= ? ");
                parameters.add(startTimestamp);
            }
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            java.sql.Timestamp endTimestamp = DateTimeUtils.parseEndDateTime(endDate.trim());
            if (endTimestamp != null) {
                sqlBuilder.append("AND je.END_TIME <= ? ");
                parameters.add(endTimestamp);
            }
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sqlBuilder.append("AND (ji.JOB_NAME LIKE ? OR je.EXIT_MESSAGE LIKE ?) ");
            String keywordPattern = "%" + keyword.trim() + "%";
            parameters.add(keywordPattern);
            parameters.add(keywordPattern);
        }

        sqlBuilder.append("ORDER BY je.START_TIME DESC LIMIT 500");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JobExecution execution = new JobExecution();
                    execution.setId(rs.getLong("JOB_EXECUTION_ID"));
                    execution.setJobInstanceId(rs.getLong("JOB_INSTANCE_ID"));
                    execution.setJobName(rs.getString("JOB_NAME"));
                    execution.setStartTime(rs.getTimestamp("START_TIME"));
                    execution.setEndTime(rs.getTimestamp("END_TIME"));
                    execution.setStatus(rs.getString("STATUS"));
                    execution.setExitCode(rs.getString("EXIT_CODE"));
                    execution.setExitMessage(rs.getString("EXIT_MESSAGE"));
                    executions.add(execution);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return executions;
    }

    /**
     * 获取步骤执行列表
     */
    public List<StepExecution> getStepExecutions(String dataSourceId, Long jobExecutionId) {
        List<StepExecution> executions = new ArrayList<>();
        DataSource dataSource = dataSources.get(dataSourceId);
        if (dataSource == null) {
            return executions;
        }

        String sql = "SELECT STEP_EXECUTION_ID, STEP_NAME, START_TIME, END_TIME, STATUS, " +
                     "EXIT_CODE, EXIT_MESSAGE, READ_COUNT, WRITE_COUNT, COMMIT_COUNT, " +
                     "ROLLBACK_COUNT, READ_SKIP_COUNT, PROCESS_SKIP_COUNT, WRITE_SKIP_COUNT, " +
                     "FILTER_COUNT " +
                     "FROM BATCH_STEP_EXECUTION " +
                     "WHERE JOB_EXECUTION_ID = ? " +
                     "ORDER BY START_TIME";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, jobExecutionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StepExecution execution = new StepExecution();
                    execution.setId(rs.getLong("STEP_EXECUTION_ID"));
                    execution.setStepName(rs.getString("STEP_NAME"));
                    execution.setStartTime(rs.getTimestamp("START_TIME"));
                    execution.setEndTime(rs.getTimestamp("END_TIME"));
                    execution.setStatus(rs.getString("STATUS"));
                    execution.setExitCode(rs.getString("EXIT_CODE"));
                    execution.setExitMessage(rs.getString("EXIT_MESSAGE"));
                    execution.setReadCount(rs.getInt("READ_COUNT"));
                    execution.setWriteCount(rs.getInt("WRITE_COUNT"));
                    execution.setCommitCount(rs.getInt("COMMIT_COUNT"));
                    execution.setRollbackCount(rs.getInt("ROLLBACK_COUNT"));
                    execution.setReadSkipCount(rs.getInt("READ_SKIP_COUNT"));
                    execution.setProcessSkipCount(rs.getInt("PROCESS_SKIP_COUNT"));
                    execution.setWriteSkipCount(rs.getInt("WRITE_SKIP_COUNT"));
                    execution.setFilterCount(rs.getInt("FILTER_COUNT"));
                    executions.add(execution);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return executions;
    }

    /**
     * 搜索步骤执行列表
     */
    public List<StepExecution> searchStepExecutions(String dataSourceId, String stepName, String status,
                                                    String startDate, String endDate, Long jobExecutionId) {
        List<StepExecution> executions = new ArrayList<>();
        DataSource dataSource = dataSources.get(dataSourceId);
        if (dataSource == null) {
            return executions;
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT se.STEP_EXECUTION_ID, se.STEP_NAME, se.START_TIME, se.END_TIME, se.STATUS, ")
                  .append("se.EXIT_CODE, se.EXIT_MESSAGE, se.READ_COUNT, se.WRITE_COUNT, se.COMMIT_COUNT, ")
                  .append("se.ROLLBACK_COUNT, se.READ_SKIP_COUNT, se.PROCESS_SKIP_COUNT, se.WRITE_SKIP_COUNT, ")
                  .append("se.FILTER_COUNT, se.JOB_EXECUTION_ID ")
                  .append("FROM BATCH_STEP_EXECUTION se ")
                  .append("WHERE 1=1 ");

        List<Object> parameters = new ArrayList<>();

        if (stepName != null && !stepName.trim().isEmpty()) {
            sqlBuilder.append("AND se.STEP_NAME LIKE ? ");
            parameters.add("%" + stepName.trim() + "%");
        }

        if (status != null && !status.trim().isEmpty()) {
            sqlBuilder.append("AND se.STATUS = ? ");
            parameters.add(status.trim());
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            java.sql.Timestamp startTimestamp = DateTimeUtils.parseStartDateTime(startDate.trim());
            if (startTimestamp != null) {
                sqlBuilder.append("AND se.START_TIME >= ? ");
                parameters.add(startTimestamp);
            }
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            java.sql.Timestamp endTimestamp = DateTimeUtils.parseEndDateTime(endDate.trim());
            if (endTimestamp != null) {
                sqlBuilder.append("AND se.END_TIME <= ? ");
                parameters.add(endTimestamp);
            }
        }

        if (jobExecutionId != null) {
            sqlBuilder.append("AND se.JOB_EXECUTION_ID = ? ");
            parameters.add(jobExecutionId);
        }

        sqlBuilder.append("ORDER BY se.START_TIME DESC LIMIT 500");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StepExecution execution = new StepExecution();
                    execution.setId(rs.getLong("STEP_EXECUTION_ID"));
                    execution.setStepName(rs.getString("STEP_NAME"));
                    execution.setStartTime(rs.getTimestamp("START_TIME"));
                    execution.setEndTime(rs.getTimestamp("END_TIME"));
                    execution.setStatus(rs.getString("STATUS"));
                    execution.setExitCode(rs.getString("EXIT_CODE"));
                    execution.setExitMessage(rs.getString("EXIT_MESSAGE"));
                    execution.setReadCount(rs.getInt("READ_COUNT"));
                    execution.setWriteCount(rs.getInt("WRITE_COUNT"));
                    execution.setCommitCount(rs.getInt("COMMIT_COUNT"));
                    execution.setRollbackCount(rs.getInt("ROLLBACK_COUNT"));
                    execution.setReadSkipCount(rs.getInt("READ_SKIP_COUNT"));
                    execution.setProcessSkipCount(rs.getInt("PROCESS_SKIP_COUNT"));
                    execution.setWriteSkipCount(rs.getInt("WRITE_SKIP_COUNT"));
                    execution.setFilterCount(rs.getInt("FILTER_COUNT"));
                    executions.add(execution);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return executions;
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics(String dataSourceId) {
        Map<String, Object> stats = new HashMap<>();
        DataSource dataSource = dataSources.get(dataSourceId);
        if (dataSource == null) {
            return stats;
        }

        try (Connection conn = dataSource.getConnection()) {
            // 作业统计
            String jobSql = "SELECT STATUS, COUNT(*) as count " +
                            "FROM BATCH_JOB_EXECUTION " +
                            "GROUP BY STATUS";
            
            Map<String, Integer> jobStats = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(jobSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    jobStats.put(rs.getString("STATUS"), rs.getInt("count"));
                }
            }
            stats.put("jobStats", jobStats);

            // 步骤统计
            String stepSql = "SELECT STATUS, COUNT(*) as count " +
                             "FROM BATCH_STEP_EXECUTION " +
                             "GROUP BY STATUS";
            
            Map<String, Integer> stepStats = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(stepSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stepStats.put(rs.getString("STATUS"), rs.getInt("count"));
                }
            }
            stats.put("stepStats", stepStats);

            // 总计数
            String totalSql = "SELECT COUNT(*) as total FROM BATCH_JOB_EXECUTION";
            try (PreparedStatement stmt = conn.prepareStatement(totalSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalJobs", rs.getInt("total"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * 关闭所有数据源
     */
    public void closeAllDataSources() {
        for (DataSource dataSource : dataSources.values()) {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        }
        dataSources.clear();
    }
}
