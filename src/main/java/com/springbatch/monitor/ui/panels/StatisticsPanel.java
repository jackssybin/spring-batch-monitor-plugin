package com.springbatch.monitor.ui.panels;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.springbatch.monitor.model.DataSourceConfig;
import com.springbatch.monitor.services.DataSourceConfigService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计信息面板 - Spring Batch Monitor
 */
public class StatisticsPanel extends JBPanel<StatisticsPanel> implements DataSourceConfigService.DataSourceConfigListener {

    private final DataSourceConfigService configService;
    private JComboBox<DataSourceConfig> dataSourceComboBox;
    private JBTable statisticsTable;
    private StatisticsTableModel tableModel;
    private JButton refreshButton;
    private JBLabel statusLabel;
    private String currentDataSourceId;

    public StatisticsPanel() {
        super(new BorderLayout());
        this.configService = DataSourceConfigService.getInstance();
        
        // 注册监听器
        configService.addListener(this);
        
        initializeUI();
        loadDataSources();
    }

    private void initializeUI() {
        // 顶部控制面板
        JPanel topPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        
        topPanel.add(new JBLabel("数据源:"));
        dataSourceComboBox = new JComboBox<>();
        dataSourceComboBox.addActionListener(e -> {
            DataSourceConfig selected = (DataSourceConfig) dataSourceComboBox.getSelectedItem();
            if (selected != null) {
                currentDataSourceId = selected.getId();
                loadStatistics();
            }
        });
        topPanel.add(dataSourceComboBox);
        
        refreshButton = new JButton("刷新统计");
        refreshButton.addActionListener(e -> loadStatistics());
        topPanel.add(refreshButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 统计表格
        tableModel = new StatisticsTableModel();
        statisticsTable = new JBTable(tableModel);
        statisticsTable.setDefaultRenderer(Object.class, new StatisticsCellRenderer());
        
        // 设置列宽
        statisticsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 统计项
        statisticsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 数值
        statisticsTable.getColumnModel().getColumn(2).setPreferredWidth(300); // 说明
        
        JBScrollPane scrollPane = new JBScrollPane(statisticsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // 底部状态栏
        JPanel bottomPanel = new JBPanel<>(new BorderLayout());
        statusLabel = new JBLabel("请选择数据源");
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
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
                loadStatistics();
            }
        });
    }

    private void loadStatistics() {
        if (currentDataSourceId == null) {
            statusLabel.setText("请选择数据源");
            return;
        }
        
        statusLabel.setText("正在加载统计信息...");
        refreshButton.setEnabled(false);
        
        SwingWorker<List<StatisticItem>, Void> worker = new SwingWorker<List<StatisticItem>, Void>() {
            private Exception error;

            @Override
            protected List<StatisticItem> doInBackground() throws Exception {
                try {
                    DataSourceConfig config = configService.getConfiguration(currentDataSourceId);
                    if (config == null) {
                        throw new RuntimeException("数据源配置未找到");
                    }

                    return collectStatistics(config);
                } catch (Exception e) {
                    error = e;
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                if (error != null) {
                    statusLabel.setText("加载失败: " + error.getMessage());
                } else {
                    try {
                        List<StatisticItem> statistics = get();
                        tableModel.setStatistics(statistics);
                        statusLabel.setText("统计信息已更新 (" + statistics.size() + " 项)");
                    } catch (Exception e) {
                        statusLabel.setText("处理统计数据失败: " + e.getMessage());
                    }
                }
            }
        };
        worker.execute();
    }

    private List<StatisticItem> collectStatistics(DataSourceConfig config) throws SQLException {
        List<StatisticItem> statistics = new ArrayList<>();
        
        try (Connection conn = getConnection(config)) {
            // 作业执行统计
            statistics.add(new StatisticItem("作业执行总数", 
                getCount(conn, "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION"), 
                "所有作业执行的总数量"));
            
            statistics.add(new StatisticItem("成功作业数", 
                getCount(conn, "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION WHERE STATUS = 'COMPLETED'"), 
                "状态为COMPLETED的作业数量"));
            
            statistics.add(new StatisticItem("失败作业数", 
                getCount(conn, "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION WHERE STATUS = 'FAILED'"), 
                "状态为FAILED的作业数量"));
            
            statistics.add(new StatisticItem("运行中作业数", 
                getCount(conn, "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION WHERE STATUS = 'STARTED'"), 
                "状态为STARTED的作业数量"));
            
            // 步骤执行统计
            statistics.add(new StatisticItem("步骤执行总数", 
                getCount(conn, "SELECT COUNT(*) FROM BATCH_STEP_EXECUTION"), 
                "所有步骤执行的总数量"));
            
            statistics.add(new StatisticItem("成功步骤数", 
                getCount(conn, "SELECT COUNT(*) FROM BATCH_STEP_EXECUTION WHERE STATUS = 'COMPLETED'"), 
                "状态为COMPLETED的步骤数量"));
            
            statistics.add(new StatisticItem("失败步骤数", 
                getCount(conn, "SELECT COUNT(*) FROM BATCH_STEP_EXECUTION WHERE STATUS = 'FAILED'"), 
                "状态为FAILED的步骤数量"));
            
            // 作业实例统计
            statistics.add(new StatisticItem("作业实例总数", 
                getCount(conn, "SELECT COUNT(*) FROM BATCH_JOB_INSTANCE"), 
                "所有作业实例的总数量"));
            
            statistics.add(new StatisticItem("不同作业类型数", 
                getCount(conn, "SELECT COUNT(DISTINCT JOB_NAME) FROM BATCH_JOB_INSTANCE"), 
                "系统中不同作业名称的数量"));
            
            // 数据处理统计
            statistics.add(new StatisticItem("总读取记录数", 
                getSum(conn, "SELECT SUM(READ_COUNT) FROM BATCH_STEP_EXECUTION"), 
                "所有步骤读取的记录总数"));
            
            statistics.add(new StatisticItem("总写入记录数", 
                getSum(conn, "SELECT SUM(WRITE_COUNT) FROM BATCH_STEP_EXECUTION"), 
                "所有步骤写入的记录总数"));
            
            statistics.add(new StatisticItem("总跳过记录数", 
                getSum(conn, "SELECT SUM(READ_SKIP_COUNT + WRITE_SKIP_COUNT + PROCESS_SKIP_COUNT) FROM BATCH_STEP_EXECUTION"), 
                "所有步骤跳过的记录总数"));
        }
        
        return statistics;
    }

    private Connection getConnection(DataSourceConfig config) throws SQLException {
        try {
            Class.forName(config.getDriverClassName());
            return java.sql.DriverManager.getConnection(
                config.getUrl(), config.getUsername(), config.getPassword());
        } catch (ClassNotFoundException e) {
            throw new SQLException("数据库驱动未找到: " + config.getDriverClassName(), e);
        }
    }

    private long getCount(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    private long getSum(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    // 数据源配置监听器实现
    @Override
    public void onConfigChanged(List<DataSourceConfig> configurations) {
        loadDataSources();
    }

    /**
     * 统计项数据类
     */
    private static class StatisticItem {
        private final String name;
        private final long value;
        private final String description;

        public StatisticItem(String name, long value, String description) {
            this.name = name;
            this.value = value;
            this.description = description;
        }

        public String getName() { return name; }
        public long getValue() { return value; }
        public String getDescription() { return description; }
    }

    /**
     * 统计表格模型
     */
    private class StatisticsTableModel extends AbstractTableModel {
        private final String[] columnNames = {"统计项", "数值", "说明"};
        private List<StatisticItem> statistics = new ArrayList<>();

        public void setStatistics(List<StatisticItem> statistics) {
            this.statistics = statistics;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return statistics.size();
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
            if (rowIndex >= statistics.size()) {
                return "";
            }
            
            StatisticItem item = statistics.get(rowIndex);
            
            switch (columnIndex) {
                case 0: return item.getName();
                case 1: return String.format("%,d", item.getValue());
                case 2: return item.getDescription();
                default: return "";
            }
        }
    }

    /**
     * 统计数据单元格渲染器
     */
    private class StatisticsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // 数值列右对齐
            if (column == 1) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            
            return c;
        }
    }
}
