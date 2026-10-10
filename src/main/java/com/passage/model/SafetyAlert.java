package com.passage.model;

import java.sql.Timestamp;

public class SafetyAlert {
    private Integer alertId;
    private Integer journeyId;
    private Integer contactId;
    private AlertType alertType;
    private Timestamp alertTime;
    private AlertStatus alertStatus;

    // Joined metadata for display
    private String contactName;
    private String contactPhone;
    private String relationship;
    private String studentName;
    private String sourceLocation;
    private String destination;

    public SafetyAlert() {}

    public SafetyAlert(Integer alertId, Integer journeyId, Integer contactId, AlertType alertType,
                       Timestamp alertTime, AlertStatus alertStatus) {
        this.alertId = alertId;
        this.journeyId = journeyId;
        this.contactId = contactId;
        this.alertType = alertType;
        this.alertTime = alertTime;
        this.alertStatus = alertStatus;
    }

    public SafetyAlert(Integer journeyId, Integer contactId, AlertType alertType, AlertStatus alertStatus) {
        this.journeyId = journeyId;
        this.contactId = contactId;
        this.alertType = alertType;
        this.alertStatus = alertStatus;
    }

    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public Integer getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(Integer journeyId) {
        this.journeyId = journeyId;
    }

    public Integer getContactId() {
        return contactId;
    }

    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public Timestamp getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Timestamp alertTime) {
        this.alertTime = alertTime;
    }

    public AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "SafetyAlert{" +
                "alertId=" + alertId +
                ", journeyId=" + journeyId +
                ", contactId=" + contactId +
                ", alertType=" + alertType +
                ", alertTime=" + alertTime +
                ", alertStatus=" + alertStatus +
                '}';
    }
}
