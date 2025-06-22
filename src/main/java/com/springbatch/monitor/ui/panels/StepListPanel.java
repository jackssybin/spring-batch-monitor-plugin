package com.springbatch.monitor.ui.panels;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.springbatch.monitor.model.DataSourceConfig;
import com.springbatch.monitor.models.StepExecution;
import com.springbatch.monitor.services.DataSourceConfigService;
import com.springbatch.monitor.services.DatabaseService;
import com.springbatch.monitor.ui.SpringBatchMonitorToolWindow;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.intellij.ui.JBColor;
import com.springbatch.monitor.utils.DateTimeUtils;

/**
 * 步骤执行列表面板
 */
public class StepListPanel extends JBPanel<StepListPanel> implements DataSourceConfigService.DataSourceConfigListener {

    private final SpringBatchMonitorToolWindow toolWindow;
    private final DataSourceConfigService configService;
    private final DatabaseService databaseService;
    private JBTable stepTable;
    private StepTableModel tableModel;
    private JTextField stepNameField;
    private JComboBox<String> statusFilter;
    private JComboBox<DataSourceConfig> dataSourceComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField jobExecutionIdField;
    private JButton refreshButton;
    private JButton searchButton;
    private JBLabel totalCountLabel;

    private List<StepExecution> stepExecutions = new ArrayList<>();

    public StepListPanel(SpringBatchMonitorToolWindow toolWindow) {
        super(new BorderLayout());
        this.toolWindow = toolWindow;
        this.configService = DataSourceConfigService.getInstance();
        this.databaseService = DatabaseService.getInstance();
        
        // 注册数据源配置变更监听器
        configService.addListener(this);
        
        initializeUI();
        loadDataSources();
        loadStepExecutions();
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
        dataSourceComboBox.addActionListener(e -> loadStepExecutions());
        topPanel.add(dataSourceComboBox, gbc);

        gbc.gridx = 2;
        topPanel.add(new JBLabel("步骤名称:"), gbc);
        gbc.gridx = 3;
        stepNameField = new JTextField(15);
        topPanel.add(stepNameField, gbc);

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
        topPanel.add(new JBLabel("作业执行ID:"), gbc);
        gbc.gridx = 5;
        jobExecutionIdField = new JTextField(12);
        topPanel.add(jobExecutionIdField, gbc);

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
        totalCountLabel = new JBLabel("总计: 0 个步骤");
        topPanel.add(totalCountLabel, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new StepTableModel();
        stepTable = new JBTable(tableModel);
        stepTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stepTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set column widths
        stepTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        stepTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Step Name
        stepTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Status
        stepTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Start Time
        stepTable.getColumnModel().getColumn(4).setPreferredWidth(120); // End Time
        stepTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Read Count
        stepTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Write Count
        stepTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Commit Count

        // Custom cell renderer for status column
        stepTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());

        // Double-click to view details
        stepTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = stepTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        StepExecution step = stepExecutions.get(selectedRow);
                        showStepDetails(step);
                    }
                }
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(stepTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with pagination (placeholder for now)
        JPanel bottomPanel = new JBPanel<>(new FlowLayout(FlowLayout.CENTER));
        JBLabel paginationLabel = new JBLabel("第 1 页，共 1 页");
        bottomPanel.add(paginationLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void clearSearchFields() {
        stepNameField.setText("");
        startDateField.setText("");
        endDateField.setText("");
        jobExecutionIdField.setText("");
        statusFilter.setSelectedIndex(0);
    }

    private void performSearch() {
        DataSourceConfig selectedDataSource = (DataSourceConfig) dataSourceComboBox.getSelectedItem();
        if (selectedDataSource == null) {
            JOptionPane.showMessageDialog(this, "请先选择数据源", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String stepName = stepNameField.getText().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();
        String jobExecutionIdText = jobExecutionIdField.getText().trim();
        
        Long jobExecutionId = null;
        if (!jobExecutionIdText.isEmpty()) {
            try {
                jobExecutionId = Long.parseLong(jobExecutionIdText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "作业执行ID必须是数字", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        toolWindow.updateStatus("正在查询步骤执行信息...");
        
        final Long finalJobExecutionId = jobExecutionId;
        SwingWorker<List<StepExecution>, Void> worker = new SwingWorker<List<StepExecution>, Void>() {
            @Override
            protected List<StepExecution> doInBackground() throws Exception {
                try {
                    databaseService.addDataSource(selectedDataSource);
                } catch (Exception e) {
                    // 数据源可能已存在，忽略错误
                }
                
                return databaseService.searchStepExecutions(
                    selectedDataSource.getId(), 
                    stepName.isEmpty() ? null : stepName, 
                    "全部".equals(selectedStatus) ? null : selectedStatus,
                    startDate.isEmpty() ? null : startDate,
                    endDate.isEmpty() ? null : endDate,
                    finalJobExecutionId
                );
            }

            @Override
            protected void done() {
                try {
                    List<StepExecution> results = get();
                    updateStepList(results);
                    toolWindow.updateStatus("查询完成，找到 " + results.size() + " 个步骤执行记录。");
                } catch (Exception e) {
                    toolWindow.updateStatus("查询失败: " + e.getMessage());
                    JOptionPane.showMessageDialog(StepListPanel.this, 
                            "查询步骤执行信息失败: " + e.getMessage(), 
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
        loadStepExecutions();
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

    private void loadStepExecutions() {
        DataSourceConfig selectedDataSource = (DataSourceConfig) dataSourceComboBox.getSelectedItem();
        if (selectedDataSource == null) {
            updateStepList(new ArrayList<>());
            return;
        }

        toolWindow.updateStatus("正在加载步骤执行信息...");

        SwingWorker<List<StepExecution>, Void> worker = new SwingWorker<List<StepExecution>, Void>() {
            @Override
            protected List<StepExecution> doInBackground() throws Exception {
                try {
                    databaseService.addDataSource(selectedDataSource);
                } catch (Exception e) {
                    // 数据源可能已存在，忽略错误
                }
                // 加载所有步骤执行记录
                return databaseService.searchStepExecutions(selectedDataSource.getId(), null, null, null, null, null);
            }

            @Override
            protected void done() {
                try {
                    List<StepExecution> steps = get();
                    updateStepList(steps);
                    toolWindow.updateStatus("加载完成，共 " + steps.size() + " 个步骤执行记录。");
                } catch (Exception e) {
                    toolWindow.updateStatus("加载失败: " + e.getMessage());
                    JOptionPane.showMessageDialog(StepListPanel.this,
                            "加载步骤执行信息失败: " + e.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateStepList(List<StepExecution> steps) {
        SwingUtilities.invokeLater(() -> {
            this.stepExecutions = steps != null ? steps : new ArrayList<>();
            tableModel.fireTableDataChanged();
            totalCountLabel.setText("总计: " + stepExecutions.size() + " 个步骤");
        });
    }

    private void showStepDetails(StepExecution step) {
        StringBuilder details = new StringBuilder();
        details.append("步骤执行详情\n\n");
        details.append("步骤ID: ").append(step.getId()).append("\n");
        details.append("步骤名称: ").append(step.getStepName()).append("\n");
        details.append("状态: ").append(step.getStatus()).append("\n");
        details.append("开始时间: ").append(step.getStartTime()).append("\n");
        details.append("结束时间: ").append(step.getEndTime()).append("\n");
        details.append("退出代码: ").append(step.getExitCode()).append("\n");
        details.append("退出消息: ").append(step.getExitMessage()).append("\n\n");
        details.append("统计信息:\n");
        details.append("读取数量: ").append(step.getReadCount()).append("\n");
        details.append("写入数量: ").append(step.getWriteCount()).append("\n");
        details.append("提交数量: ").append(step.getCommitCount()).append("\n");
        details.append("回滚数量: ").append(step.getRollbackCount()).append("\n");
        details.append("读取跳过: ").append(step.getReadSkipCount()).append("\n");
        details.append("处理跳过: ").append(step.getProcessSkipCount()).append("\n");
        details.append("写入跳过: ").append(step.getWriteSkipCount()).append("\n");
        details.append("过滤数量: ").append(step.getFilterCount()).append("\n");

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "步骤执行详情", JOptionPane.INFORMATION_MESSAGE);
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
     * Table model for step executions
     */
    private class StepTableModel extends AbstractTableModel {
        private final String[] columnNames = {
                "步骤ID", "步骤名称", "状态", "开始时间", "结束时间", "读取数", "写入数", "提交数"
        };

        @Override
        public int getRowCount() {
            return stepExecutions.size();
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
            if (rowIndex >= stepExecutions.size()) {
                return "";
            }

            StepExecution step = stepExecutions.get(rowIndex);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            switch (columnIndex) {
                case 0: return step.getId();
                case 1: return step.getStepName();
                case 2: return step.getStatus();
                case 3: return step.getStartTime() != null ?
                    new java.sql.Timestamp(step.getStartTime().getTime()).toLocalDateTime().format(formatter) : "";
                case 4: return step.getEndTime() != null ?
                    new java.sql.Timestamp(step.getEndTime().getTime()).toLocalDateTime().format(formatter) : "";
                case 5: return step.getReadCount();
                case 6: return step.getWriteCount();
                case 7: return step.getCommitCount();
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

            if (!isSelected && row < stepExecutions.size()) {
                StepExecution step = stepExecutions.get(row);
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
}
