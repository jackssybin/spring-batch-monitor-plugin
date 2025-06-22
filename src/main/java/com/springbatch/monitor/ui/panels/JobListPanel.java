package com.springbatch.monitor.ui.panels;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.springbatch.monitor.model.DataSourceConfig;
import com.springbatch.monitor.models.JobExecution;
import com.springbatch.monitor.services.DataSourceConfigService;
import com.springbatch.monitor.services.DatabaseService;
import com.springbatch.monitor.ui.SpringBatchMonitorToolWindow;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.intellij.ui.JBColor;
import com.springbatch.monitor.utils.DateTimeUtils;

/**
 * 作业列表面板 - Spring Batch Monitor
 */
public class JobListPanel extends JBPanel<JobListPanel> implements DataSourceConfigService.DataSourceConfigListener {

    private final SpringBatchMonitorToolWindow toolWindow;
    private final DataSourceConfigService configService;
    private final DatabaseService databaseService;
    private JBTable jobTable;
    private JobTableModel tableModel;
    private JTextField searchField;
    private JTextField jobNameField;
    private JComboBox<String> statusFilter;
    private JComboBox<DataSourceConfig> dataSourceComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton refreshButton;
    private JButton searchButton;
    private JBLabel totalCountLabel;

    private List<JobExecution> jobExecutions = new ArrayList<>();
    private List<JobExecution> dbJobExecutions = new ArrayList<>();
    private int currentPage = 0;
    private final int pageSize = 20;

    public JobListPanel(SpringBatchMonitorToolWindow toolWindow) {
        super(new BorderLayout());
        this.toolWindow = toolWindow;
        this.configService = DataSourceConfigService.getInstance();
        this.databaseService = DatabaseService.getInstance();

        // 注册数据源配置变更监听器
        configService.addListener(this);

        initializeUI();
        loadDataSources();
        loadJobExecutions();
    }

    private void initializeUI() {
        // Top panel with search and filters - use GridBagLayout for better control
        JPanel topPanel = new JBPanel<>(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // First row
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JBLabel("数据源:"), gbc);
        gbc.gridx = 1;
        dataSourceComboBox = new JComboBox<>();
        dataSourceComboBox.addActionListener(e -> loadJobExecutions());
        topPanel.add(dataSourceComboBox, gbc);

        gbc.gridx = 2;
        topPanel.add(new JBLabel("作业名称:"), gbc);
        gbc.gridx = 3;
        jobNameField = new JTextField(15);
        topPanel.add(jobNameField, gbc);

        gbc.gridx = 4;
        topPanel.add(new JBLabel("状态:"), gbc);
        gbc.gridx = 5;
        statusFilter = new JComboBox<>(new String[]{"全部", "COMPLETED", "FAILED", "STARTED", "STOPPED"});
        topPanel.add(statusFilter, gbc);

        // Second row
        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JBLabel("开始时间:"), gbc);
        gbc.gridx = 1;
        startDateField = new JTextField(12);
        startDateField.setToolTipText("支持格式: 2020-06-20 或 2020-06-20 14:30:00");
        addDateTimeValidation(startDateField);
        topPanel.add(startDateField, gbc);

        gbc.gridx = 2;
        topPanel.add(new JBLabel("结束时间:"), gbc);
        gbc.gridx = 3;
        endDateField = new JTextField(12);
        endDateField.setToolTipText("支持格式: 2020-06-20 或 2020-06-20 14:30:00");
        addDateTimeValidation(endDateField);
        topPanel.add(endDateField, gbc);

        gbc.gridx = 4;
        topPanel.add(new JBLabel("关键字:"), gbc);
        gbc.gridx = 5;
        searchField = new JTextField(15);
        topPanel.add(searchField, gbc);

        // Third row - buttons
        gbc.gridx = 0; gbc.gridy = 2;
        searchButton = new JButton("查询");
        searchButton.addActionListener(e -> performSearch());
        topPanel.add(searchButton, gbc);

        gbc.gridx = 1;
        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshData());
        topPanel.add(refreshButton, gbc);

        gbc.gridx = 2;
        JButton clearButton = new JButton("清空");
        clearButton.addActionListener(e -> clearSearchFields());
        topPanel.add(clearButton, gbc);

        // Total count label
        gbc.gridx = 5; gbc.anchor = GridBagConstraints.EAST;
        totalCountLabel = new JBLabel("总计: 0 个作业");
        topPanel.add(totalCountLabel, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new JobTableModel();
        jobTable = new JBTable(tableModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set column widths
        jobTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        jobTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Job Name
        jobTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Status
        jobTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Start Time
        jobTable.getColumnModel().getColumn(4).setPreferredWidth(150); // End Time
        jobTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Duration

        // Custom cell renderer for status column
        jobTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());

        // Double-click to view details
        jobTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = jobTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        Long jobExecutionId;
                        if (!dbJobExecutions.isEmpty()) {
                            JobExecution job = dbJobExecutions.get(selectedRow);
                            jobExecutionId = job.getId();
                        } else {
                            JobExecution job = jobExecutions.get(selectedRow);
                            jobExecutionId = job.getJobExecutionId();
                        }
                        toolWindow.showJobDetail(jobExecutionId);
                    }
                }
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(jobTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with pagination (placeholder for now)
        JPanel bottomPanel = new JBPanel<>(new FlowLayout(FlowLayout.CENTER));
        JBLabel paginationLabel = new JBLabel("Page 1 of 1");
        bottomPanel.add(paginationLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void clearSearchFields() {
        jobNameField.setText("");
        searchField.setText("");
        startDateField.setText("");
        endDateField.setText("");
        statusFilter.setSelectedIndex(0);
    }

    private void performSearch() {
        DataSourceConfig selectedDataSource = (DataSourceConfig) dataSourceComboBox.getSelectedItem();
        if (selectedDataSource == null) {
            JOptionPane.showMessageDialog(this, "请先选择数据源", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String jobName = jobNameField.getText().trim();
        String searchText = searchField.getText().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();

        toolWindow.updateStatus("正在查询作业执行信息...");

        SwingWorker<List<JobExecution>, Void> worker = new SwingWorker<List<JobExecution>, Void>() {
            @Override
            protected List<JobExecution> doInBackground() throws Exception {
                try {
                    databaseService.addDataSource(selectedDataSource);
                } catch (Exception e) {
                    // 数据源可能已存在，忽略错误
                }

                return databaseService.searchJobExecutions(
                    selectedDataSource.getId(),
                    jobName,
                    "全部".equals(selectedStatus) ? null : selectedStatus,
                    startDate.isEmpty() ? null : startDate,
                    endDate.isEmpty() ? null : endDate,
                    searchText.isEmpty() ? null : searchText
                );
            }

            @Override
            protected void done() {
                try {
                    List<JobExecution> results = get();
                    updateJobListFromDatabase(results);
                    toolWindow.updateStatus("查询完成，找到 " + results.size() + " 个作业执行记录。");
                } catch (Exception e) {
                    toolWindow.updateStatus("查询失败: " + e.getMessage());
                    JOptionPane.showMessageDialog(JobListPanel.this,
                            "查询作业执行信息失败: " + e.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * 为日期时间输入框添加实时验证
     */
    private void addDateTimeValidation(JTextField textField) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateDateTime();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateDateTime();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateDateTime();
            }

            private void validateDateTime() {
                SwingUtilities.invokeLater(() -> {
                    String text = textField.getText().trim();
                    if (text.isEmpty()) {
                        textField.setBackground(JBColor.background());
                        textField.setToolTipText("支持格式: 2020-06-20 或 2020-06-20 14:30:00");
                    } else {
                        DateTimeUtils.ValidationResult result = DateTimeUtils.validateDateTime(text);
                        if (result.isValid()) {
                            // 主题适配的淡绿色：亮色主题为淡绿，暗色主题为深绿
                            textField.setBackground(new JBColor(
                                new Color(230, 255, 230), // 亮色主题
                                new Color(45, 85, 45)      // 暗色主题
                            ));
                            textField.setToolTipText(result.getMessage());
                        } else {
                            // 主题适配的淡红色：亮色主题为淡红，暗色主题为深红
                            textField.setBackground(new JBColor(
                                new Color(255, 230, 230), // 亮色主题
                                new Color(85, 45, 45)      // 暗色主题
                            ));
                            textField.setToolTipText(result.getMessage());
                        }
                    }
                });
            }
        });
    }

    public void refreshData() {
        loadDataSources();
        loadJobExecutions();
    }

    private void loadDataSources() {
        SwingUtilities.invokeLater(() -> {
            dataSourceComboBox.removeAllItems();
            List<DataSourceConfig> configs = configService.getActiveConfigurations();
            for (DataSourceConfig config : configs) {
                dataSourceComboBox.addItem(config);
            }
        });
    }

    private void loadJobExecutions() {
        DataSourceConfig selectedDataSource = (DataSourceConfig) dataSourceComboBox.getSelectedItem();
        if (selectedDataSource == null) {
            updateJobList(new ArrayList<>());
            return;
        }

        toolWindow.updateStatus("Loading job executions from database...");

        SwingWorker<List<JobExecution>, Void> worker = new SwingWorker<List<JobExecution>, Void>() {
            @Override
            protected List<JobExecution> doInBackground() throws Exception {
                // 确保数据源已添加到数据库服务
                try {
                    databaseService.addDataSource(selectedDataSource);
                } catch (Exception e) {
                    // 数据源可能已存在，忽略错误
                }
                return databaseService.getJobExecutions(selectedDataSource.getId());
            }

            @Override
            protected void done() {
                try {
                    List<JobExecution> jobs = get();
                    updateJobListFromDatabase(jobs);
                    toolWindow.updateStatus("Loaded " + jobs.size() + " job executions from database.");
                } catch (Exception e) {
                    toolWindow.updateStatus("Failed to load jobs: " + e.getMessage());
                    JOptionPane.showMessageDialog(JobListPanel.this,
                            "Failed to load job executions: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateJobList(List<JobExecution> jobs) {
        SwingUtilities.invokeLater(() -> {
            this.jobExecutions = jobs != null ? jobs : new ArrayList<>();
            this.dbJobExecutions.clear();
            tableModel.fireTableDataChanged();
            totalCountLabel.setText("Total: " + jobExecutions.size() + " jobs");
        });
    }

    private void updateJobListFromDatabase(List<JobExecution> jobs) {
        SwingUtilities.invokeLater(() -> {
            this.dbJobExecutions = jobs != null ? jobs : new ArrayList<>();
            this.jobExecutions.clear();
            tableModel.fireTableDataChanged();
            totalCountLabel.setText("总计: " + dbJobExecutions.size() + " 个作业");
        });
    }

    @Override
    public void onConfigChanged(List<DataSourceConfig> configurations) {
        // 数据源配置变更时，更新下拉框
        SwingUtilities.invokeLater(() -> {
            DataSourceConfig selectedConfig = (DataSourceConfig) dataSourceComboBox.getSelectedItem();
            dataSourceComboBox.removeAllItems();

            List<DataSourceConfig> activeConfigs = configurations.stream()
                    .filter(DataSourceConfig::isActive)
                    .collect(java.util.stream.Collectors.toList());

            for (DataSourceConfig config : activeConfigs) {
                dataSourceComboBox.addItem(config);
            }

            // 尝试保持之前选中的数据源
            if (selectedConfig != null) {
                for (DataSourceConfig config : activeConfigs) {
                    if (config.getId().equals(selectedConfig.getId())) {
                        dataSourceComboBox.setSelectedItem(config);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Table model for job executions
     */
    private class JobTableModel extends AbstractTableModel {
        private final String[] columnNames = {
                "ID", "Job Name", "Status", "Start Time", "End Time", "Duration"
        };

        @Override
        public int getRowCount() {
            return !dbJobExecutions.isEmpty() ? dbJobExecutions.size() : jobExecutions.size();
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
            // 优先使用数据库数据
            if (!dbJobExecutions.isEmpty()) {
                if (rowIndex >= dbJobExecutions.size()) {
                    return "";
                }

                JobExecution job = dbJobExecutions.get(rowIndex);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                switch (columnIndex) {
                    case 0: return job.getId();
                    case 1: return job.getJobName();
                    case 2: return job.getStatus();
                    case 3: return job.getStartTime() != null ?
                        new java.sql.Timestamp(job.getStartTime().getTime()).toLocalDateTime().format(formatter) : "";
                    case 4: return job.getEndTime() != null ?
                        new java.sql.Timestamp(job.getEndTime().getTime()).toLocalDateTime().format(formatter) : "";
                    case 5: return calculateDuration(job.getStartTime(), job.getEndTime());
                    default: return "";
                }
            } else {
                if (rowIndex >= jobExecutions.size()) {
                    return "";
                }

                JobExecution job = jobExecutions.get(rowIndex);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                switch (columnIndex) {
                    case 0: return job.getJobExecutionId();
                    case 1: return job.getJobName();
                    case 2: return job.getStatus();
                    case 3: return job.getStartTime() != null ? formatter.format(job.getStartTime().toLocalDateTime()) : "";
                    case 4: return job.getEndTime() != null ? formatter.format(job.getEndTime().toLocalDateTime()) : "";
                    case 5: return job.getFormattedDuration();
                    default: return "";
                }
            }
        }

        private String calculateDuration(java.sql.Timestamp startTime, java.sql.Timestamp endTime) {
            if (startTime == null || endTime == null) {
                return "";
            }
            long duration = endTime.getTime() - startTime.getTime();
            long seconds = duration / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            if (hours > 0) {
                return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
            } else if (minutes > 0) {
                return String.format("%dm %ds", minutes, seconds % 60);
            } else {
                return String.format("%ds", seconds);
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

            if (!isSelected) {
                String status = null;

                // 优先使用数据库数据
                if (!dbJobExecutions.isEmpty() && row < dbJobExecutions.size()) {
                    JobExecution job = dbJobExecutions.get(row);
                    status = job.getStatus();
                } else if (!jobExecutions.isEmpty() && row < jobExecutions.size()) {
                    JobExecution job = jobExecutions.get(row);
                    status = job.getStatus();
                }

                if (status != null) {
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
            }

            return c;
        }
    }
}
