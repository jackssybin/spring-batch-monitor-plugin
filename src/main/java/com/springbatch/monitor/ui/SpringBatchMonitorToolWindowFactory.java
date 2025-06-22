package com.springbatch.monitor.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating Spring Batch Monitor Tool Window
 */
public class SpringBatchMonitorToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SpringBatchMonitorToolWindow monitorToolWindow = new SpringBatchMonitorToolWindow(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(monitorToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
