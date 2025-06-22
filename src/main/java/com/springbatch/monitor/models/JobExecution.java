package com.springbatch.monitor.models;

import java.sql.Timestamp;

/**
 * 作业执行模型
 */
public class JobExecution {
    private Long id;
    private Long jobInstanceId;
    private String jobName;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;
    private String exitCode;
    private String exitMessage;

    public JobExecution() {
    }

    public JobExecution(Long id, Long jobInstanceId, String jobName, Timestamp startTime, 
                       Timestamp endTime, String status, String exitCode, String exitMessage) {
        this.id = id;
        this.jobInstanceId = jobInstanceId;
        this.jobName = jobName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.exitCode = exitCode;
        this.exitMessage = exitMessage;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    // 兼容性方法
    public Long getJobExecutionId() {
        return id;
    }

    public String getFormattedDuration() {
        if (startTime == null) {
            return "";
        }
        if (endTime == null) {
            return "运行中";
        }

        long duration = endTime.getTime() - startTime.getTime();
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    @Override
    public String toString() {
        return "JobExecution{" +
                "id=" + id +
                ", jobInstanceId=" + jobInstanceId +
                ", jobName='" + jobName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                ", exitCode='" + exitCode + '\'' +
                ", exitMessage='" + exitMessage + '\'' +
                '}';
    }
}
