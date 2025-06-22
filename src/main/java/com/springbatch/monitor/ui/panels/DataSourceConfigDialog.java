package com.springbatch.monitor.ui.panels;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.springbatch.monitor.model.DataSourceConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

/**
 * 数据源配置对话框 - Spring Batch Monitor
 */
public class DataSourceConfigDialog extends JDialog {

    private final DataSourceConfig originalConfig;
    private boolean confirmed = false;
    
    private JBTextField nameField;
    private JComboBox<DataSourceConfig.DatabaseType> databaseTypeComboBox;
    private JBTextField urlField;
    private JBTextField usernameField;
    private JPasswordField passwordField;
    private JBTextField driverField;
    
    private JButton okButton;
    private JButton cancelButton;
    private JButton testButton;

    public DataSourceConfigDialog(Window parent, DataSourceConfig config) {
        super(parent, config == null ? "添加数据源" : "编辑数据源", ModalityType.APPLICATION_MODAL);
        this.originalConfig = config;
        
        initializeUI();
        if (config != null) {
            populateFields(config);
        }
        
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // 名称
        gbc.gridx = 0; gbc.gridy = row;
        mainPanel.add(new JBLabel("名称:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JBTextField(20);
        mainPanel.add(nameField, gbc);
        
        // 数据库类型
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JBLabel("数据库类型:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        databaseTypeComboBox = new JComboBox<>(DataSourceConfig.DatabaseType.values());
        databaseTypeComboBox.addActionListener(e -> updateDriverField());
        mainPanel.add(databaseTypeComboBox, gbc);
        
        // 连接URL
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JBLabel("连接URL:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        urlField = new JBTextField(30);
        urlField.setToolTipText("例如: jdbc:mysql://localhost:3306/batch_db");
        mainPanel.add(urlField, gbc);
        
        // 用户名
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JBLabel("用户名:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        usernameField = new JBTextField(20);
        mainPanel.add(usernameField, gbc);
        
        // 密码
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JBLabel("密码:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);
        
        // 驱动类名
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JBLabel("驱动类名:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        driverField = new JBTextField(30);
        mainPanel.add(driverField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        testButton = new JButton("测试连接");
        testButton.addActionListener(e -> testConnection());
        buttonPanel.add(testButton);
        
        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        buttonPanel.add(cancelButton);
        
        okButton = new JButton("确定");
        okButton.addActionListener(e -> {
            if (validateFields()) {
                confirmed = true;
                dispose();
            }
        });
        buttonPanel.add(okButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置默认按钮
        getRootPane().setDefaultButton(okButton);
        
        // 初始化驱动字段
        updateDriverField();
    }

    private void updateDriverField() {
        DataSourceConfig.DatabaseType dbType = (DataSourceConfig.DatabaseType) databaseTypeComboBox.getSelectedItem();
        if (dbType != null) {
            driverField.setText(dbType.getDriverClassName());
            if (urlField.getText().isEmpty()) {
                urlField.setText(dbType.getDefaultUrl());
            }
        }
    }

    private void populateFields(DataSourceConfig config) {
        nameField.setText(config.getName());
        databaseTypeComboBox.setSelectedItem(config.getDatabaseType());
        urlField.setText(config.getUrl());
        usernameField.setText(config.getUsername());
        passwordField.setText(config.getPassword());
        driverField.setText(config.getDriverClassName());
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入数据源名称", "验证错误", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        if (urlField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入连接URL", "验证错误", JOptionPane.ERROR_MESSAGE);
            urlField.requestFocus();
            return false;
        }
        
        if (usernameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名", "验证错误", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return false;
        }
        
        if (driverField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入驱动类名", "验证错误", JOptionPane.ERROR_MESSAGE);
            driverField.requestFocus();
            return false;
        }
        
        return true;
    }

    private void testConnection() {
        if (!validateFields()) {
            return;
        }
        
        testButton.setEnabled(false);
        testButton.setText("测试中...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String errorMessage;

            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    Class.forName(driverField.getText().trim());
                    try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                            urlField.getText().trim(), 
                            usernameField.getText().trim(), 
                            new String(passwordField.getPassword()))) {
                        return conn.isValid(5);
                    }
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                testButton.setEnabled(true);
                testButton.setText("测试连接");
                
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(
                            DataSourceConfigDialog.this,
                            "连接测试成功！",
                            "测试结果",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            DataSourceConfigDialog.this,
                            "连接测试失败：" + (errorMessage != null ? errorMessage : "未知错误"),
                            "测试结果",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        DataSourceConfigDialog.this,
                        "连接测试失败：" + e.getMessage(),
                        "测试结果",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public DataSourceConfig getDataSourceConfig() {
        String id = originalConfig != null ? originalConfig.getId() : UUID.randomUUID().toString();
        DataSourceConfig.DatabaseType dbType = (DataSourceConfig.DatabaseType) databaseTypeComboBox.getSelectedItem();

        return new DataSourceConfig(
            id,
            nameField.getText().trim(),
            dbType,
            urlField.getText().trim(),
            usernameField.getText().trim(),
            new String(passwordField.getPassword())
        );
    }
}
