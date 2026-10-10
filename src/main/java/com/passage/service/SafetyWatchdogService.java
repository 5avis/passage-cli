package com.passage.service;

import com.passage.dao.JourneyDao;
import com.passage.dao.JourneyUpdateDao;
import com.passage.dao.SafetyAlertDao;
import com.passage.dao.TrustedContactDao;
import com.passage.model.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SafetyWatchdogService {

    private final JourneyDao journeyDao = new JourneyDao();
    private final TrustedContactDao contactDao = new TrustedContactDao();
    private final SafetyAlertDao alertDao = new SafetyAlertDao();
    private final JourneyUpdateDao updateDao = new JourneyUpdateDao();

    public static class SafetyScanResult {
        private final int overdueJourneysFound;
        private final int newAlertsTriggered;
        private final int existingAlertsSkipped;
        private final List<SafetyAlert> triggeredAlerts;
        private final List<Journey> overdueJourneys;

        public SafetyScanResult(int overdueJourneysFound, int newAlertsTriggered,
                                int existingAlertsSkipped, List<SafetyAlert> triggeredAlerts,
                                List<Journey> overdueJourneys) {
            this.overdueJourneysFound = overdueJourneysFound;
            this.newAlertsTriggered = newAlertsTriggered;
            this.existingAlertsSkipped = existingAlertsSkipped;
            this.triggeredAlerts = triggeredAlerts;
            this.overdueJourneys = overdueJourneys;
        }

        public int getOverdueJourneysFound() {
            return overdueJourneysFound;
        }

        public int getNewAlertsTriggered() {
            return newAlertsTriggered;
        }

        public int getExistingAlertsSkipped() {
            return existingAlertsSkipped;
        }

        public List<SafetyAlert> getTriggeredAlerts() {
            return triggeredAlerts;
        }

        public List<Journey> getOverdueJourneys() {
            return overdueJourneys;
        }
    }

    /**
     * Executes Safety Watchdog scan:
     * Queries trips where journey_status = 'IN_PROGRESS' AND NOW() > expected_arrival_time.
     * Deduplicates against existing DELAY_ALERT records.
     */
    public SafetyScanResult runSafetyScan() throws SQLException {
        List<Journey> overdueJourneys = journeyDao.findOverdueInProgressJourneys();
        List<SafetyAlert> newlyTriggered = new ArrayList<>();
        int skippedAlerts = 0;
        Timestamp now = new Timestamp(System.currentTimeMillis());

        for (Journey journey : overdueJourneys) {
            List<TrustedContact> contacts = contactDao.findByStudentId(journey.getStudentId());
            int newAlertsForThisTrip = 0;

            for (TrustedContact contact : contacts) {
                boolean alreadyAlerted = alertDao.exists(
                        journey.getJourneyId(),
                        contact.getContactId(),
                        AlertType.DELAY_ALERT
                );

                if (!alreadyAlerted) {
                    SafetyAlert alert = new SafetyAlert(
                            journey.getJourneyId(),
                            contact.getContactId(),
                            AlertType.DELAY_ALERT,
                            AlertStatus.SENT
                    );
                    alert.setAlertTime(now);
                    int alertId = alertDao.create(alert);
                    alert.setAlertId(alertId);
                    alert.setContactName(contact.getContactName());
                    alert.setContactPhone(contact.getContactPhone());
                    alert.setRelationship(contact.getRelationship());
                    alert.setStudentName(journey.getStudentName());
                    alert.setSourceLocation(journey.getSourceLocation());
                    alert.setDestination(journey.getDestination());

                    newlyTriggered.add(alert);
                    newAlertsForThisTrip++;
                } else {
                    skippedAlerts++;
                }
            }

            // If we generated new alerts for this overdue journey, record an audit checkpoint
            if (newAlertsForThisTrip > 0) {
                String auditRemarks = String.format(
                        "Safety Watchdog: Trip exceeded expected arrival time (%s). Automatically dispatched DELAY_ALERT to %d trusted contact(s).",
                        journey.getExpectedArrivalTime(), newAlertsForThisTrip
                );
                updateDao.create(new JourneyUpdate(journey.getJourneyId(), "DELAY_ALERT_DISPATCHED", auditRemarks));
            }
        }

        return new SafetyScanResult(
                overdueJourneys.size(),
                newlyTriggered.size(),
                skippedAlerts,
                newlyTriggered,
                overdueJourneys
        );
    }

    public List<SafetyAlert> getAllAlerts() throws SQLException {
        return alertDao.findAll();
    }

    public List<SafetyAlert> getPendingAlerts() throws SQLException {
        return alertDao.findPendingAlerts();
    }

    public boolean acknowledgeAlert(int alertId) throws SQLException {
        return alertDao.updateStatus(alertId, AlertStatus.ACKNOWLEDGED);
    }
}
