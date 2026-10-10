package com.passage.model;

import java.sql.Timestamp;

public class Journey {
    private Integer journeyId;
    private Integer studentId;
    private String sourceLocation;
    private String destination;
    private Timestamp departureTime;
    private Timestamp expectedArrivalTime;
    private Timestamp actualArrivalTime;
    private JourneyStatus journeyStatus;
    private Timestamp createdAt;

    // Optional joins for display
    private String studentName;
    private String studentEmail;

    public Journey() {}

    public Journey(Integer journeyId, Integer studentId, String sourceLocation, String destination,
                   Timestamp departureTime, Timestamp expectedArrivalTime, Timestamp actualArrivalTime,
                   JourneyStatus journeyStatus, Timestamp createdAt) {
        this.journeyId = journeyId;
        this.studentId = studentId;
        this.sourceLocation = sourceLocation;
        this.destination = destination;
        this.departureTime = departureTime;
        this.expectedArrivalTime = expectedArrivalTime;
        this.actualArrivalTime = actualArrivalTime;
        this.journeyStatus = journeyStatus;
        this.createdAt = createdAt;
    }

    public Journey(Integer studentId, String sourceLocation, String destination,
                   Timestamp departureTime, Timestamp expectedArrivalTime, JourneyStatus journeyStatus) {
        this.studentId = studentId;
        this.sourceLocation = sourceLocation;
        this.destination = destination;
        this.departureTime = departureTime;
        this.expectedArrivalTime = expectedArrivalTime;
        this.journeyStatus = journeyStatus;
    }

    public Integer getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(Integer journeyId) {
        this.journeyId = journeyId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
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

    public Timestamp getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Timestamp departureTime) {
        this.departureTime = departureTime;
    }

    public Timestamp getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    public void setExpectedArrivalTime(Timestamp expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public Timestamp getActualArrivalTime() {
        return actualArrivalTime;
    }

    public void setActualArrivalTime(Timestamp actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    public JourneyStatus getJourneyStatus() {
        return journeyStatus;
    }

    public void setJourneyStatus(JourneyStatus journeyStatus) {
        this.journeyStatus = journeyStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public boolean isOverdue() {
        if (journeyStatus != JourneyStatus.IN_PROGRESS) return false;
        long now = System.currentTimeMillis();
        return expectedArrivalTime != null && now > expectedArrivalTime.getTime();
    }
}
