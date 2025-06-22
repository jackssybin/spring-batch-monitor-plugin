package com.springbatch.monitor.ui.panels;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.springbatch.monitor.ui.SpringBatchMonitorToolWindow;

import javax.swing.*;
import java.awt.*;

/**
 * 关于页面 - Spring Batch 监控插件
 */
public class WelcomePanel extends JBPanel<WelcomePanel> {

    private final SpringBatchMonitorToolWindow toolWindow;

    public WelcomePanel(SpringBatchMonitorToolWindow toolWindow) {
        super(new BorderLayout());
        this.toolWindow = toolWindow;
        initializeUI();
    }

    private void initializeUI() {
        // Header panel
        JPanel headerPanel = new JBPanel<>(new FlowLayout(FlowLayout.CENTER));
        JBLabel titleLabel = new JBLabel("Spring Batch 监控插件");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        headerPanel.add(titleLabel);

        JBLabel versionLabel = new JBLabel("版本 1.0.0");
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.ITALIC, 12f));
        headerPanel.add(versionLabel);

        // Main content panel
        JPanel mainPanel = new JBPanel<>(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        JBLabel descLabel = new JBLabel("<html><center>" +
                "在 IntelliJ IDEA 中直接监控和管理 Spring Batch 作业<br><br>" +
                "支持查看作业执行历史、状态和详细信息<br>" +
                "提供数据源配置、作业查询、步骤分析等功能" +
                "</center></html>");
        descLabel.setFont(descLabel.getFont().deriveFont(14f));
        mainPanel.add(descLabel, gbc);

        // Quick access buttons
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        JPanel buttonPanel = new JBPanel<>(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton jobListButton = new JButton("作业列表");
        jobListButton.setFont(jobListButton.getFont().deriveFont(Font.BOLD, 14f));
        jobListButton.addActionListener(e -> toolWindow.showJobList());
        buttonPanel.add(jobListButton);

        JButton stepListButton = new JButton("步骤列表");
        stepListButton.setFont(stepListButton.getFont().deriveFont(Font.BOLD, 14f));
        stepListButton.addActionListener(e -> toolWindow.showStepList());
        buttonPanel.add(stepListButton);

        mainPanel.add(buttonPanel, gbc);

        // Features panel
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel featuresPanel = createFeaturesPanel();
        mainPanel.add(featuresPanel, gbc);

        // Add panels to main layout
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JBPanel<>(new FlowLayout(FlowLayout.CENTER));
        JBLabel footerLabel = new JBLabel("Spring Batch 监控插件 for IntelliJ IDEA");
        footerLabel.setFont(footerLabel.getFont().deriveFont(Font.ITALIC, 10f));
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createFeaturesPanel() {
        JPanel panel = new JBPanel<>(new GridLayout(3, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("主要功能"));

        // Feature items
        String[] features = {
                "• 多数据源配置管理",
                "• 作业执行历史查询",
                "• 步骤执行详情分析",
                "• 实时状态监控",
                "• 高级搜索过滤",
                "• 统计信息展示"
        };

        for (String feature : features) {
            JBLabel featureLabel = new JBLabel(feature);
            featureLabel.setFont(featureLabel.getFont().deriveFont(12f));
            panel.add(featureLabel);
        }

        return panel;
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        // 关于页面不需要刷新操作
    }
}
