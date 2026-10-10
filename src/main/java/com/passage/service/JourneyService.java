package com.passage.service;

import com.passage.dao.JourneyDao;
import com.passage.dao.JourneyUpdateDao;
import com.passage.dao.SafetyAlertDao;
import com.passage.dao.StudentDao;
import com.passage.dao.TrustedContactDao;
import com.passage.model.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JourneyService {

    private final JourneyDao journeyDao = new JourneyDao();
    private final JourneyUpdateDao updateDao = new JourneyUpdateDao();
    private final SafetyAlertDao alertDao = new SafetyAlertDao();
    private final StudentDao studentDao = new StudentDao();
    private final TrustedContactDao contactDao = new TrustedContactDao();

    /**
     * Start/schedule a new journey.
     */
    public Journey startJourney(int studentId, String source, String destination,
                               Timestamp departureTime, Timestamp expectedArrivalTime,
                               JourneyStatus initialStatus, String initialRemarks) throws SQLException {
        if (!studentDao.exists(studentId)) {
            throw new IllegalArgumentException("Student ID " + studentId + " does not exist.");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Source location cannot be empty.");
        }
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination cannot be empty.");
        }
        if (departureTime == null || expectedArrivalTime == null) {
            throw new IllegalArgumentException("Departure and expected arrival timestamps are required.");
        }
        if (expectedArrivalTime.before(departureTime)) {
            throw new IllegalArgumentException("Expected arrival time cannot be before departure time.");
        }
        if (initialStatus == null) {
            initialStatus = JourneyStatus.PLANNED;
        }

        Journey journey = new Journey(studentId, source.trim(), destination.trim(), departureTime, expectedArrivalTime, initialStatus);
        int journeyId = journeyDao.create(journey);
        journey.setJourneyId(journeyId);

        // Add initial journey update checkpoint
        String remarks = (initialRemarks != null && !initialRemarks.isBlank())
                ? initialRemarks.trim()
                : (initialStatus == JourneyStatus.IN_PROGRESS ? "Journey commenced." : "Journey planned and scheduled.");
        
        JourneyUpdate initialUpdate = new JourneyUpdate(journeyId, initialStatus.name(), remarks);
        initialUpdate.setUpdateTime(departureTime.before(new Timestamp(System.currentTimeMillis())) ? departureTime : new Timestamp(System.currentTimeMillis()));
        updateDao.create(initialUpdate);

        return journey;
    }

    /**
     * Update journey status with intermediate checkpoint remarks.
     * When marking COMPLETED: sets actual_arrival_time = NOW() and auto-generates JOURNEY_COMPLETION alerts for all trusted contacts.
     */
    public boolean updateJourneyStatus(int journeyId, JourneyStatus newStatus, String checkpointRemarks) throws SQLException {
        Optional<Journey> optJourney = journeyDao.findById(journeyId);
        if (optJourney.isEmpty()) {
            throw new IllegalArgumentException("Journey ID " + journeyId + " not found.");
        }

        Journey journey = optJourney.get();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (newStatus == JourneyStatus.COMPLETED) {
            // Set actual arrival time to now
            journeyDao.updateStatus(journeyId, JourneyStatus.COMPLETED, now);

            // Add completion update
            String remark = (checkpointRemarks != null && !checkpointRemarks.isBlank())
                    ? checkpointRemarks.trim()
                    : "Journey safely completed and confirmed by student.";
            updateDao.create(new JourneyUpdate(journeyId, "COMPLETED", remark));

            // Auto-generate JOURNEY_COMPLETION alerts for all registered trusted contacts
            List<TrustedContact> contacts = contactDao.findByStudentId(journey.getStudentId());
            for (TrustedContact contact : contacts) {
                if (!alertDao.exists(journeyId, contact.getContactId(), AlertType.JOURNEY_COMPLETION)) {
                    SafetyAlert alert = new SafetyAlert(
                            journeyId,
                            contact.getContactId(),
                            AlertType.JOURNEY_COMPLETION,
                            AlertStatus.SENT
                    );
                    alert.setAlertTime(now);
                    alertDao.create(alert);
                }
            }
            return true;

        } else if (newStatus == JourneyStatus.CANCELLED) {
            journeyDao.updateStatus(journeyId, JourneyStatus.CANCELLED, null);
            String remark = (checkpointRemarks != null && !checkpointRemarks.isBlank())
                    ? checkpointRemarks.trim()
                    : "Journey cancelled by student.";
            updateDao.create(new JourneyUpdate(journeyId, "CANCELLED", remark));
            return true;

        } else {
            // IN_PROGRESS or PLANNED status update
            journeyDao.updateStatus(journeyId, newStatus, null);
            String remark = (checkpointRemarks != null && !checkpointRemarks.isBlank())
                    ? checkpointRemarks.trim()
                    : "Status updated to " + newStatus.name();
            updateDao.create(new JourneyUpdate(journeyId, newStatus.name(), remark));
            return true;
        }
    }

    /**
     * Add intermediate checkpoint remarks without changing status.
     */
    public void addCheckpointRemark(int journeyId, String checkpointStatus, String remarks) throws SQLException {
        Optional<Journey> optJourney = journeyDao.findById(journeyId);
        if (optJourney.isEmpty()) {
            throw new IllegalArgumentException("Journey ID " + journeyId + " not found.");
        }
        String status = (checkpointStatus != null && !checkpointStatus.isBlank()) ? checkpointStatus.trim() : "CHECKPOINT";
        updateDao.create(new JourneyUpdate(journeyId, status, remarks));
    }

    public List<Journey> getActiveJourneys() throws SQLException {
        return journeyDao.findActiveJourneys();
    }

    public List<Journey> getAllJourneys() throws SQLException {
        return journeyDao.findAll();
    }

    public Optional<Journey> getJourneyById(int journeyId) throws SQLException {
        return journeyDao.findById(journeyId);
    }

    /**
     * Retrieve complete student journey history with nested updates and generated safety alerts.
     */
    public List<JourneyDetails> getJourneyHistoryForStudent(int studentId) throws SQLException {
        Optional<Student> optStudent = studentDao.findById(studentId);
        if (optStudent.isEmpty()) {
            throw new IllegalArgumentException("Student ID " + studentId + " not found.");
        }
        Student student = optStudent.get();

        List<Journey> journeys = journeyDao.findByStudentId(studentId);
        List<JourneyDetails> history = new ArrayList<>();

        for (Journey j : journeys) {
            JourneyDetails details = new JourneyDetails(j, student);
            details.setUpdates(updateDao.findByJourneyId(j.getJourneyId()));
            details.setAlerts(alertDao.findByJourneyId(j.getJourneyId()));
            history.add(details);
        }

        return history;
    }
}
