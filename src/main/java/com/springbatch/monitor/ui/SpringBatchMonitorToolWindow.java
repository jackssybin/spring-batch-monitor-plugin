package com.springbatch.monitor.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.springbatch.monitor.ui.panels.WelcomePanel;
import com.springbatch.monitor.ui.panels.JobListPanel;
import com.springbatch.monitor.ui.panels.JobDetailPanel;
import com.springbatch.monitor.ui.panels.StepListPanel;
import com.springbatch.monitor.ui.panels.DataSourceConfigPanel;
import com.springbatch.monitor.ui.panels.StatisticsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Spring Batch Monitor 主工具窗口
 */
public class SpringBatchMonitorToolWindow {

    private final Project project;
    private JPanel contentPanel;
    private JBTabbedPane tabbedPane;
    private WelcomePanel welcomePanel;
    private JobListPanel jobListPanel;
    private JobDetailPanel jobDetailPanel;
    private StepListPanel stepListPanel;
    private JBLabel statusLabel;

    public SpringBatchMonitorToolWindow(Project project) {
        this.project = project;
        initializeUI();
    }

    private void initializeUI() {
        contentPanel = new JBPanel<>(new BorderLayout());

        // Create tabbed pane
        tabbedPane = new JBTabbedPane();

        // Create panels
        DataSourceConfigPanel dataSourcePanel = new DataSourceConfigPanel();
        jobListPanel = new JobListPanel(this);
        stepListPanel = new StepListPanel(this);
        jobDetailPanel = new JobDetailPanel(this);
        StatisticsPanel statisticsPanel = new StatisticsPanel();
        welcomePanel = new WelcomePanel(this);

        // Add tabs
        tabbedPane.addTab("数据源配置", dataSourcePanel);
        tabbedPane.addTab("作业列表", jobListPanel);
        tabbedPane.addTab("步骤列表", stepListPanel);
        tabbedPane.addTab("作业详情", jobDetailPanel);
        tabbedPane.addTab("统计分析", statisticsPanel);
        tabbedPane.addTab("关于", welcomePanel);

        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JBLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        contentPanel.add(statusLabel, BorderLayout.SOUTH);
    }

    public JComponent getContent() {
        return contentPanel;
    }

    /**
     * Switch to job list tab
     */
    public void showJobList() {
        tabbedPane.setSelectedIndex(1);
        jobListPanel.refreshData();
    }

    /**
     * Switch to step list tab
     */
    public void showStepList() {
        tabbedPane.setSelectedIndex(2);
        stepListPanel.refreshData();
    }

    /**
     * Switch to job detail tab and show specific job
     */
    public void showJobDetail(Long jobExecutionId) {
        tabbedPane.setSelectedIndex(3);
        jobDetailPanel.loadJobDetails(jobExecutionId);
    }

    /**
     * Update status bar message
     */
    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    /**
     * Get the project instance
     */
    public Project getProject() {
        return project;
    }
}
