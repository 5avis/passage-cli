package com.passage.model;

import java.sql.Timestamp;

public class JourneyUpdate {
    private Integer updateId;
    private Integer journeyId;
    private Timestamp updateTime;
    private String currentStatus;
    private String remarks;

    public JourneyUpdate() {}

    public JourneyUpdate(Integer updateId, Integer journeyId, Timestamp updateTime, String currentStatus, String remarks) {
        this.updateId = updateId;
        this.journeyId = journeyId;
        this.updateTime = updateTime;
        this.currentStatus = currentStatus;
        this.remarks = remarks;
    }

    public JourneyUpdate(Integer journeyId, String currentStatus, String remarks) {
        this.journeyId = journeyId;
        this.currentStatus = currentStatus;
        this.remarks = remarks;
    }

    public Integer getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    public Integer getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(Integer journeyId) {
        this.journeyId = journeyId;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "JourneyUpdate{" +
                "updateId=" + updateId +
                ", journeyId=" + journeyId +
                ", updateTime=" + updateTime +
                ", currentStatus='" + currentStatus + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
