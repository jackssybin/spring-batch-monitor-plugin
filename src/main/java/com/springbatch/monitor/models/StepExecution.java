package com.springbatch.monitor.models;

import java.sql.Timestamp;

/**
 * 步骤执行模型
 */
public class StepExecution {
    private Long id;
    private String stepName;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;
    private String exitCode;
    private String exitMessage;
    private int readCount;
    private int writeCount;
    private int commitCount;
    private int rollbackCount;
    private int readSkipCount;
    private int processSkipCount;
    private int writeSkipCount;
    private int filterCount;

    public StepExecution() {
    }

    public StepExecution(Long id, String stepName, Timestamp startTime, Timestamp endTime, 
                        String status, String exitCode, String exitMessage, int readCount, 
                        int writeCount, int commitCount, int rollbackCount, int readSkipCount, 
                        int processSkipCount, int writeSkipCount, int filterCount) {
        this.id = id;
        this.stepName = stepName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.exitCode = exitCode;
        this.exitMessage = exitMessage;
        this.readCount = readCount;
        this.writeCount = writeCount;
        this.commitCount = commitCount;
        this.rollbackCount = rollbackCount;
        this.readSkipCount = readSkipCount;
        this.processSkipCount = processSkipCount;
        this.writeSkipCount = writeSkipCount;
        this.filterCount = filterCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
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

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public int getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(int writeCount) {
        this.writeCount = writeCount;
    }

    public int getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(int commitCount) {
        this.commitCount = commitCount;
    }

    public int getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(int rollbackCount) {
        this.rollbackCount = rollbackCount;
    }

    public int getReadSkipCount() {
        return readSkipCount;
    }

    public void setReadSkipCount(int readSkipCount) {
        this.readSkipCount = readSkipCount;
    }

    public int getProcessSkipCount() {
        return processSkipCount;
    }

    public void setProcessSkipCount(int processSkipCount) {
        this.processSkipCount = processSkipCount;
    }

    public int getWriteSkipCount() {
        return writeSkipCount;
    }

    public void setWriteSkipCount(int writeSkipCount) {
        this.writeSkipCount = writeSkipCount;
    }

    public int getFilterCount() {
        return filterCount;
    }

    public void setFilterCount(int filterCount) {
        this.filterCount = filterCount;
    }

    @Override
    public String toString() {
        return "StepExecution{" +
                "id=" + id +
                ", stepName='" + stepName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                ", exitCode='" + exitCode + '\'' +
                ", exitMessage='" + exitMessage + '\'' +
                ", readCount=" + readCount +
                ", writeCount=" + writeCount +
                ", commitCount=" + commitCount +
                ", rollbackCount=" + rollbackCount +
                ", readSkipCount=" + readSkipCount +
                ", processSkipCount=" + processSkipCount +
                ", writeSkipCount=" + writeSkipCount +
                ", filterCount=" + filterCount +
                '}';
    }
}
