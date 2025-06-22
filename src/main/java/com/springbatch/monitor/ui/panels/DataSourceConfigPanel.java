package com.springbatch.monitor.ui.panels;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.springbatch.monitor.model.DataSourceConfig;
import com.springbatch.monitor.services.DataSourceConfigService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 数据源配置面板 - Spring Batch Monitor
 */
public class DataSourceConfigPanel extends JBPanel<DataSourceConfigPanel> implements DataSourceConfigService.DataSourceConfigListener {

    private final DataSourceConfigService configService;
    private JBTable configTable;
    private ConfigTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton testButton;

    public DataSourceConfigPanel() {
        super(new BorderLayout());
        this.configService = DataSourceConfigService.getInstance();
        
        // 注册监听器
        configService.addListener(this);
        
        initializeUI();
        loadConfigurations();
    }

    private void initializeUI() {
        // 顶部工具栏
        JPanel toolbarPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        
        addButton = new JButton("添加数据源");
        addButton.addActionListener(e -> showAddDialog());
        toolbarPanel.add(addButton);
        
        editButton = new JButton("编辑");
        editButton.addActionListener(e -> showEditDialog());
        editButton.setEnabled(false);
        toolbarPanel.add(editButton);
        
        deleteButton = new JButton("删除");
        deleteButton.addActionListener(e -> deleteSelectedConfig());
        deleteButton.setEnabled(false);
        toolbarPanel.add(deleteButton);
        
        testButton = new JButton("测试连接");
        testButton.addActionListener(e -> testSelectedConnection());
        testButton.setEnabled(false);
        toolbarPanel.add(testButton);
        
        add(toolbarPanel, BorderLayout.NORTH);
        
        // 配置表格
        tableModel = new ConfigTableModel();
        configTable = new JBTable(tableModel);
        configTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // 设置列宽
        configTable.getColumnModel().getColumn(0).setPreferredWidth(150); // 名称
        configTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 类型
        configTable.getColumnModel().getColumn(2).setPreferredWidth(300); // URL
        configTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 用户名
        configTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 状态
        
        JBScrollPane scrollPane = new JBScrollPane(configTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // 底部说明
        JPanel bottomPanel = new JBPanel<>(new BorderLayout());
        JBLabel infoLabel = new JBLabel("<html><i>提示：配置数据源后，可以在其他标签页中选择对应的数据源进行查询</i></html>");
        bottomPanel.add(infoLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateButtonStates() {
        int selectedRow = configTable.getSelectedRow();
        boolean hasSelection = selectedRow >= 0;
        
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        testButton.setEnabled(hasSelection);
    }

    private void showAddDialog() {
        DataSourceConfigDialog dialog = new DataSourceConfigDialog(null, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            DataSourceConfig config = dialog.getDataSourceConfig();
            configService.addConfiguration(config);
        }
    }

    private void showEditDialog() {
        int selectedRow = configTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<DataSourceConfig> configs = configService.getAllConfigurations();
            DataSourceConfig config = configs.get(selectedRow);
            
            DataSourceConfigDialog dialog = new DataSourceConfigDialog(null, config);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                DataSourceConfig updatedConfig = dialog.getDataSourceConfig();
                configService.updateConfiguration(updatedConfig);
            }
        }
    }

    private void deleteSelectedConfig() {
        int selectedRow = configTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<DataSourceConfig> configs = configService.getAllConfigurations();
            DataSourceConfig config = configs.get(selectedRow);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除数据源 \"" + config.getName() + "\" 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                configService.removeConfiguration(config.getId());
            }
        }
    }

    private void testSelectedConnection() {
        int selectedRow = configTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<DataSourceConfig> configs = configService.getAllConfigurations();
            DataSourceConfig config = configs.get(selectedRow);
            
            // 在后台线程中测试连接
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    try {
                        Class.forName(config.getDriverClassName());
                        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                                config.getUrl(), config.getUsername(), config.getPassword())) {
                            return conn.isValid(5); // 5秒超时
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        String message = success ? 
                            "连接测试成功！" : 
                            "连接测试失败，请检查配置信息。";
                        int messageType = success ? 
                            JOptionPane.INFORMATION_MESSAGE : 
                            JOptionPane.ERROR_MESSAGE;
                        
                        JOptionPane.showMessageDialog(
                            DataSourceConfigPanel.this,
                            message,
                            "连接测试",
                            messageType
                        );
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                            DataSourceConfigPanel.this,
                            "连接测试失败：" + e.getMessage(),
                            "连接测试",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private void loadConfigurations() {
        SwingUtilities.invokeLater(() -> {
            tableModel.fireTableDataChanged();
            updateButtonStates();
        });
    }

    // 数据源配置监听器实现
    @Override
    public void onConfigChanged(List<DataSourceConfig> configurations) {
        loadConfigurations();
    }

    /**
     * 配置表格模型
     */
    private class ConfigTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "名称", "数据库类型", "连接URL", "用户名", "状态"
        };

        @Override
        public int getRowCount() {
            return configService.getAllConfigurations().size();
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
            List<DataSourceConfig> configs = configService.getAllConfigurations();
            if (rowIndex >= configs.size()) {
                return "";
            }
            
            DataSourceConfig config = configs.get(rowIndex);
            
            switch (columnIndex) {
                case 0: return config.getName();
                case 1: return config.getDatabaseType() != null ? config.getDatabaseType().getDisplayName() : "";
                case 2: return config.getUrl();
                case 3: return config.getUsername();
                case 4: return "未测试"; // 可以扩展为实际的连接状态
                default: return "";
            }
        }
    }
}
