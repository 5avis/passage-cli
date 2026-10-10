package com.passage.model;

import java.util.ArrayList;
import java.util.List;

public class JourneyDetails {
    private Journey journey;
    private Student student;
    private List<JourneyUpdate> updates = new ArrayList<>();
    private List<SafetyAlert> alerts = new ArrayList<>();

    public JourneyDetails() {}

    public JourneyDetails(Journey journey, Student student) {
        this.journey = journey;
        this.student = student;
    }

    public Journey getJourney() {
        return journey;
    }

    public void setJourney(Journey journey) {
        this.journey = journey;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public List<JourneyUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<JourneyUpdate> updates) {
        this.updates = updates;
    }

    public List<SafetyAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<SafetyAlert> alerts) {
        this.alerts = alerts;
    }
}
