package com.springbatch.monitor.ui.panels;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.springbatch.monitor.model.DataSourceConfig;
import com.springbatch.monitor.models.JobExecution;
import com.springbatch.monitor.models.StepExecution;
import com.springbatch.monitor.services.DataSourceConfigService;
import com.springbatch.monitor.services.DatabaseService;
import com.springbatch.monitor.ui.SpringBatchMonitorToolWindow;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作业详情面板 - Spring Batch Monitor
 *
 * @author jackssybin
 * @version 1.0.0
 */
public class JobDetailPanel extends JBPanel<JobDetailPanel> implements DataSourceConfigService.DataSourceConfigListener {

    private final SpringBatchMonitorToolWindow toolWindow;
    private final DataSourceConfigService configService;
    private final DatabaseService databaseService;

    private JobExecution currentJob;
    private List<StepExecution> currentSteps = new ArrayList<>();
    private Map<String, String> currentParameters = new HashMap<>();
    private String currentDataSourceId;

    private JComboBox<DataSourceConfig> dataSourceComboBox;
    private JBLabel jobIdLabel;
    private JBLabel jobInstanceIdLabel;
    private JBLabel jobNameLabel;
    private JBLabel statusLabel;
    private JBLabel startTimeLabel;
    private JBLabel endTimeLabel;
    private JBLabel durationLabel;
    private JBLabel exitCodeLabel;
    private JTextArea exitMessageArea;

    private JBTable stepTable;
    private StepTableModel stepTableModel;
    private JBTable paramTable;
    private ParameterTableModel paramTableModel;

    private JButton backButton;
    private JButton refreshButton;

    public JobDetailPanel(SpringBatchMonitorToolWindow toolWindow) {
        super(new BorderLayout());
        this.toolWindow = toolWindow;
        this.configService = DataSourceConfigService.getInstance();
        this.databaseService = DatabaseService.getInstance();

        // 注册数据源配置变更监听器
        configService.addListener(this);

        initializeUI();
        loadDataSources();
    }

    private void initializeUI() {
        // Top panel with navigation and data source selection
        JPanel topPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));

        backButton = new JButton("< 返回作业列表");
        backButton.addActionListener(e -> toolWindow.showJobList());
        topPanel.add(backButton);

        topPanel.add(new JBLabel("数据源:"));
        dataSourceComboBox = new JComboBox<>();
        dataSourceComboBox.addActionListener(e -> {
            DataSourceConfig selected = (DataSourceConfig) dataSourceComboBox.getSelectedItem();
            if (selected != null) {
                currentDataSourceId = selected.getId();
                if (currentJob != null) {
                    loadJobDetails(currentJob.getId(), currentDataSourceId);
                }
            }
        });
        topPanel.add(dataSourceComboBox);

        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> {
            if (currentJob != null && currentDataSourceId != null) {
                loadJobDetails(currentJob.getId(), currentDataSourceId);
            }
        });
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Main content with tabs
        JBTabbedPane tabbedPane = new JBTabbedPane();

        // Job Info tab
        tabbedPane.addTab("作业信息", createJobInfoPanel());

        // Steps tab
        tabbedPane.addTab("步骤执行", createStepsPanel());

        // Parameters tab
        tabbedPane.addTab("作业参数", createParametersPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createJobInfoPanel() {
        JPanel panel = new JBPanel<>(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Job basic information
        int row = 0;
        
        // Job Execution ID
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JBLabel("作业执行ID:"), gbc);
        gbc.gridx = 1;
        jobIdLabel = new JBLabel("-");
        jobIdLabel.setFont(jobIdLabel.getFont().deriveFont(Font.BOLD));
        panel.add(jobIdLabel, gbc);

        // Job Instance ID
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JBLabel("作业实例ID:"), gbc);
        gbc.gridx = 1;
        jobInstanceIdLabel = new JBLabel("-");
        panel.add(jobInstanceIdLabel, gbc);

        // Job Name
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JBLabel("作业名称:"), gbc);
        gbc.gridx = 1;
        jobNameLabel = new JBLabel("-");
        jobNameLabel.setFont(jobNameLabel.getFont().deriveFont(Font.BOLD));
        panel.add(jobNameLabel, gbc);
        
        // Status
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JBLabel("执行状态:"), gbc);
        gbc.gridx = 1;
        statusLabel = new JBLabel("-");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        panel.add(statusLabel, gbc);

        // Start Time
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JBLabel("开始时间:"), gbc);
        gbc.gridx = 1;
        startTimeLabel = new JBLabel("-");
        panel.add(startTimeLabel, gbc);

        // End Time
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JBLabel("结束时间:"), gbc);
        gbc.gridx = 1;
        endTimeLabel = new JBLabel("-");
        panel.add(endTimeLabel, gbc);

        // Duration
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JBLabel("执行时长:"), gbc);
        gbc.gridx = 1;
        durationLabel = new JBLabel("-");
        panel.add(durationLabel, gbc);

        // Exit Code
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JBLabel("退出代码:"), gbc);
        gbc.gridx = 1;
        exitCodeLabel = new JBLabel("-");
        panel.add(exitCodeLabel, gbc);

        // Exit Message
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JBLabel("退出消息:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        exitMessageArea = new JTextArea(5, 40);
        exitMessageArea.setEditable(false);
        exitMessageArea.setLineWrap(true);
        exitMessageArea.setWrapStyleWord(true);
        JBScrollPane exitMessageScrollPane = new JBScrollPane(exitMessageArea);
        panel.add(exitMessageScrollPane, gbc);

        return panel;
    }

    private JPanel createStepsPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        
        stepTableModel = new StepTableModel();
        stepTable = new JBTable(stepTableModel);
        stepTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set column widths
        stepTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 步骤ID
        stepTable.getColumnModel().getColumn(1).setPreferredWidth(200); // 步骤名称
        stepTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 状态
        stepTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 读取数量
        stepTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 写入数量
        stepTable.getColumnModel().getColumn(5).setPreferredWidth(100); // 执行时长
        
        // Custom cell renderer for status column
        stepTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        
        JBScrollPane scrollPane = new JBScrollPane(stepTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createParametersPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        
        paramTableModel = new ParameterTableModel();
        paramTable = new JBTable(paramTableModel);
        paramTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set column widths
        paramTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 参数名称
        paramTable.getColumnModel().getColumn(1).setPreferredWidth(400); // 值
        
        JBScrollPane scrollPane = new JBScrollPane(paramTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    public void loadJobDetails(Long jobExecutionId, String dataSourceId) {
        if (jobExecutionId == null || dataSourceId == null) {
            clearJobDetails();
            return;
        }

        toolWindow.updateStatus("正在加载作业详情...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private JobExecution jobExecution;
            private List<StepExecution> stepExecutions = new ArrayList<>();
            private Map<String, String> jobParameters = new HashMap<>();
            private Exception error;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    DataSourceConfig config = configService.getConfiguration(dataSourceId);
                    if (config == null) {
                        throw new RuntimeException("数据源配置未找到: " + dataSourceId);
                    }

                    try (Connection conn = getConnection(config)) {
                        // 加载作业执行信息
                        jobExecution = loadJobExecution(conn, jobExecutionId);
                        if (jobExecution == null) {
                            throw new RuntimeException("作业执行未找到: " + jobExecutionId);
                        }

                        // 加载步骤执行信息
                        stepExecutions = loadStepExecutions(conn, jobExecutionId);

                        // 加载作业参数
                        jobParameters = loadJobParameters(conn, jobExecutionId);
                    }
                } catch (Exception e) {
                    error = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (error != null) {
                    toolWindow.updateStatus("加载作业详情失败: " + error.getMessage());
                    JOptionPane.showMessageDialog(JobDetailPanel.this,
                            "加载作业详情失败: " + error.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                    clearJobDetails();
                } else {
                    currentJob = jobExecution;
                    currentSteps = stepExecutions;
                    currentParameters = jobParameters;
                    updateJobDetails();
                    toolWindow.updateStatus("作业详情加载成功");
                }
            }
        };
        worker.execute();
    }

    private Connection getConnection(DataSourceConfig config) throws SQLException {
        try {
            Class.forName(config.getDriverClassName());
            return java.sql.DriverManager.getConnection(
                config.getUrl(), config.getUsername(), config.getPassword());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + config.getDriverClassName(), e);
        }
    }

    private JobExecution loadJobExecution(Connection conn, Long jobExecutionId) throws SQLException {
        String sql = "SELECT je.JOB_EXECUTION_ID, je.JOB_INSTANCE_ID, ji.JOB_NAME, " +
                     "je.START_TIME, je.END_TIME, je.STATUS, je.EXIT_CODE, je.EXIT_MESSAGE, " +
                     "je.CREATE_TIME, je.LAST_UPDATED, je.VERSION " +
                     "FROM BATCH_JOB_EXECUTION je " +
                     "JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID " +
                     "WHERE je.JOB_EXECUTION_ID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, jobExecutionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JobExecution job = new JobExecution();
                    job.setId(rs.getLong("JOB_EXECUTION_ID"));
                    job.setJobInstanceId(rs.getLong("JOB_INSTANCE_ID"));
                    job.setJobName(rs.getString("JOB_NAME"));
                    job.setStartTime(rs.getTimestamp("START_TIME"));
                    job.setEndTime(rs.getTimestamp("END_TIME"));
                    job.setStatus(rs.getString("STATUS"));
                    job.setExitCode(rs.getString("EXIT_CODE"));
                    job.setExitMessage(rs.getString("EXIT_MESSAGE"));
                    return job;
                }
            }
        }
        return null;
    }

    private List<StepExecution> loadStepExecutions(Connection conn, Long jobExecutionId) throws SQLException {
        List<StepExecution> steps = new ArrayList<>();
        String sql = "SELECT STEP_EXECUTION_ID, STEP_NAME, START_TIME, END_TIME, STATUS, " +
                     "COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, " +
                     "WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT, ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE " +
                     "FROM BATCH_STEP_EXECUTION " +
                     "WHERE JOB_EXECUTION_ID = ? " +
                     "ORDER BY STEP_EXECUTION_ID";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, jobExecutionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StepExecution step = new StepExecution();
                    step.setId(rs.getLong("STEP_EXECUTION_ID"));
                    step.setStepName(rs.getString("STEP_NAME"));
                    step.setStartTime(rs.getTimestamp("START_TIME"));
                    step.setEndTime(rs.getTimestamp("END_TIME"));
                    step.setStatus(rs.getString("STATUS"));
                    step.setCommitCount(rs.getInt("COMMIT_COUNT"));
                    step.setReadCount(rs.getInt("READ_COUNT"));
                    step.setFilterCount(rs.getInt("FILTER_COUNT"));
                    step.setWriteCount(rs.getInt("WRITE_COUNT"));
                    step.setReadSkipCount(rs.getInt("READ_SKIP_COUNT"));
                    step.setWriteSkipCount(rs.getInt("WRITE_SKIP_COUNT"));
                    step.setProcessSkipCount(rs.getInt("PROCESS_SKIP_COUNT"));
                    step.setRollbackCount(rs.getInt("ROLLBACK_COUNT"));
                    step.setExitCode(rs.getString("EXIT_CODE"));
                    step.setExitMessage(rs.getString("EXIT_MESSAGE"));
                    steps.add(step);
                }
            }
        }
        return steps;
    }

    private Map<String, String> loadJobParameters(Connection conn, Long jobExecutionId) throws SQLException {
        Map<String, String> parameters = new HashMap<>();
        String sql = "SELECT KEY_NAME, TYPE_CD, STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, IDENTIFYING " +
                     "FROM BATCH_JOB_EXECUTION_PARAMS " +
                     "WHERE JOB_EXECUTION_ID = ? " +
                     "ORDER BY KEY_NAME";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, jobExecutionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("KEY_NAME");
                    String type = rs.getString("TYPE_CD");
                    String value = "";

                    switch (type) {
                        case "STRING":
                            value = rs.getString("STRING_VAL");
                            break;
                        case "DATE":
                            value = rs.getTimestamp("DATE_VAL") != null ?
                                   rs.getTimestamp("DATE_VAL").toString() : "";
                            break;
                        case "LONG":
                            value = String.valueOf(rs.getLong("LONG_VAL"));
                            break;
                        case "DOUBLE":
                            value = String.valueOf(rs.getDouble("DOUBLE_VAL"));
                            break;
                    }

                    String identifying = rs.getString("IDENTIFYING");
                    parameters.put(key, value + " (" + type + ")" + ("Y".equals(identifying) ? " [标识]" : ""));
                }
            }
        }
        return parameters;
    }

    private void updateJobDetails() {
        SwingUtilities.invokeLater(() -> {
            if (currentJob != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                jobIdLabel.setText(currentJob.getId().toString());
                jobInstanceIdLabel.setText(currentJob.getJobInstanceId().toString());
                jobNameLabel.setText(currentJob.getJobName());
                statusLabel.setText(currentJob.getStatus());

                // Set status color
                String status = currentJob.getStatus();
                if ("COMPLETED".equals(status)) {
                    statusLabel.setForeground(new Color(0, 128, 0)); // Green
                } else if ("FAILED".equals(status)) {
                    statusLabel.setForeground(Color.RED);
                } else if ("STARTED".equals(status) || "STARTING".equals(status)) {
                    statusLabel.setForeground(Color.BLUE);
                } else {
                    statusLabel.setForeground(Color.BLACK);
                }

                startTimeLabel.setText(currentJob.getStartTime() != null ?
                                     currentJob.getStartTime().toLocalDateTime().format(formatter) : "N/A");
                endTimeLabel.setText(currentJob.getEndTime() != null ?
                                   currentJob.getEndTime().toLocalDateTime().format(formatter) : "N/A");

                // Calculate duration
                String duration = "N/A";
                if (currentJob.getStartTime() != null) {
                    if (currentJob.getEndTime() != null) {
                        long seconds = java.time.Duration.between(
                            currentJob.getStartTime().toLocalDateTime(),
                            currentJob.getEndTime().toLocalDateTime()).getSeconds();
                        duration = formatDuration(seconds);
                    } else if ("STARTED".equals(status) || "STARTING".equals(status)) {
                        long seconds = java.time.Duration.between(
                            currentJob.getStartTime().toLocalDateTime(),
                            java.time.LocalDateTime.now()).getSeconds();
                        duration = formatDuration(seconds) + " (运行中)";
                    }
                }
                durationLabel.setText(duration);

                exitCodeLabel.setText(currentJob.getExitCode() != null ? currentJob.getExitCode() : "N/A");
                exitMessageArea.setText(currentJob.getExitMessage() != null ? currentJob.getExitMessage() : "");

                // Update tables
                stepTableModel.fireTableDataChanged();
                paramTableModel.fireTableDataChanged();
            } else {
                clearJobDetails();
            }
        });
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d小时%d分钟%d秒", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, secs);
        } else {
            return String.format("%d秒", secs);
        }
    }

    private void clearJobDetails() {
        SwingUtilities.invokeLater(() -> {
            currentJob = null;
            currentSteps.clear();
            currentParameters.clear();

            jobIdLabel.setText("-");
            jobInstanceIdLabel.setText("-");
            jobNameLabel.setText("-");
            statusLabel.setText("-");
            statusLabel.setForeground(Color.BLACK);
            startTimeLabel.setText("-");
            endTimeLabel.setText("-");
            durationLabel.setText("-");
            exitCodeLabel.setText("-");
            exitMessageArea.setText("");

            stepTableModel.fireTableDataChanged();
            paramTableModel.fireTableDataChanged();
        });
    }

    /**
     * 步骤执行表格模型
     */
    private class StepTableModel extends AbstractTableModel {
        private final String[] columnNames = {
                "步骤ID", "步骤名称", "状态", "读取数量", "写入数量", "执行时长"
        };

        @Override
        public int getRowCount() {
            return currentSteps.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= currentSteps.size()) {
                return "";
            }

            StepExecution step = currentSteps.get(rowIndex);

            switch (columnIndex) {
                case 0: return step.getId();
                case 1: return step.getStepName();
                case 2: return step.getStatus();
                case 3: return step.getReadCount();
                case 4: return step.getWriteCount();
                case 5: {
                    if (step.getStartTime() != null) {
                        if (step.getEndTime() != null) {
                            long seconds = java.time.Duration.between(
                                step.getStartTime().toLocalDateTime(),
                                step.getEndTime().toLocalDateTime()).getSeconds();
                            return formatDuration(seconds);
                        } else if ("STARTED".equals(step.getStatus()) || "STARTING".equals(step.getStatus())) {
                            long seconds = java.time.Duration.between(
                                step.getStartTime().toLocalDateTime(),
                                java.time.LocalDateTime.now()).getSeconds();
                            return formatDuration(seconds) + " (运行中)";
                        }
                    }
                    return "N/A";
                }
                default: return "";
            }
        }
    }

    /**
     * 作业参数表格模型
     */
    private class ParameterTableModel extends AbstractTableModel {
        private final String[] columnNames = {
                "参数名称", "值"
        };
        private final List<Map.Entry<String, String>> parameterList = new ArrayList<>();

        @Override
        public int getRowCount() {
            parameterList.clear();
            parameterList.addAll(currentParameters.entrySet());
            return parameterList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= parameterList.size()) {
                return "";
            }

            Map.Entry<String, String> entry = parameterList.get(rowIndex);

            switch (columnIndex) {
                case 0: return entry.getKey();
                case 1: return entry.getValue();
                default: return "";
            }
        }
    }

    /**
     * 状态列自定义渲染器
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected && row < currentSteps.size()) {
                StepExecution step = currentSteps.get(row);
                String status = step.getStatus();

                if ("COMPLETED".equals(status)) {
                    c.setForeground(new Color(0, 128, 0)); // Green
                } else if ("FAILED".equals(status)) {
                    c.setForeground(Color.RED);
                } else if ("STARTED".equals(status) || "STARTING".equals(status)) {
                    c.setForeground(Color.BLUE);
                } else {
                    c.setForeground(Color.BLACK);
                }
            }

            return c;
        }
    }

    // 数据源配置监听器方法
    @Override
    public void onConfigChanged(List<DataSourceConfig> configurations) {
        loadDataSources();
    }

    private void loadDataSources() {
        SwingUtilities.invokeLater(() -> {
            dataSourceComboBox.removeAllItems();
            List<DataSourceConfig> configs = configService.getAllConfigurations();
            for (DataSourceConfig config : configs) {
                dataSourceComboBox.addItem(config);
            }

            if (!configs.isEmpty() && currentDataSourceId == null) {
                currentDataSourceId = configs.get(0).getId();
            }
        });
    }

    // 公共方法，供外部调用
    public void loadJobDetails(Long jobExecutionId) {
        if (currentDataSourceId != null) {
            loadJobDetails(jobExecutionId, currentDataSourceId);
        } else {
            JOptionPane.showMessageDialog(this, "请先选择数据源", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }
}
