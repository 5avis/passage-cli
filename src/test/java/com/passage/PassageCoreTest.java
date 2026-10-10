package com.passage;

import com.passage.config.DBConnection;
import com.passage.model.*;
import com.passage.service.DatabaseInitializer;
import com.passage.service.JourneyService;
import com.passage.service.SafetyWatchdogService;
import com.passage.service.StudentService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PassageCoreTest {

    private static StudentService studentService;
    private static JourneyService journeyService;
    private static SafetyWatchdogService watchdogService;

    @BeforeAll
    static void setUp() throws Exception {
        assertTrue(DBConnection.getInstance().testConnection(), "Database connection must be valid");
        new DatabaseInitializer().initializeDatabase(true);
        studentService = new StudentService();
        journeyService = new JourneyService();
        watchdogService = new SafetyWatchdogService();
    }

    @Test
    @Order(1)
    void testRegisterStudentAndContacts() throws Exception {
        String testEmail = "test.student." + System.currentTimeMillis() + "@campus.edu";
        Student s = studentService.registerStudent("Test Student", testEmail, "+91-9999900001");
        assertNotNull(s.getStudentId());
        assertTrue(s.getStudentId() > 0);

        // Disallow duplicate email
        assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("Duplicate Student", testEmail, "+91-9999900002")
        );

        // Add trusted contacts
        TrustedContact c1 = studentService.addTrustedContact(s.getStudentId(), "Emergency Contact 1", "+91-9999911111", "Guardian");
        assertNotNull(c1.getContactId());

        TrustedContact c2 = studentService.addTrustedContact(s.getStudentId(), "Emergency Contact 2", "+91-9999922222", "Sibling");
        assertNotNull(c2.getContactId());

        List<TrustedContact> contacts = studentService.getContactsForStudent(s.getStudentId());
        assertEquals(2, contacts.size());
    }

    @Test
    @Order(2)
    void testJourneyLifecycleAndCompletionAlerts() throws Exception {
        // Create student with contacts
        String email = "lifecycle." + System.currentTimeMillis() + "@campus.edu";
        Student s = studentService.registerStudent("Lifecycle Student", email, "+91-8888800001");
        TrustedContact c1 = studentService.addTrustedContact(s.getStudentId(), "Parent One", "+91-8888811111", "Father");

        Timestamp departure = new Timestamp(System.currentTimeMillis() - 3600_000L); // 1h ago
        Timestamp expected = new Timestamp(System.currentTimeMillis() + 1800_000L);  // in 30 mins

        // 1. Start journey
        Journey j = journeyService.startJourney(s.getStudentId(), "Hostel A", "Library", departure, expected, JourneyStatus.IN_PROGRESS, "Began walk");
        assertEquals(JourneyStatus.IN_PROGRESS, j.getJourneyStatus());

        // 2. Add checkpoint
        journeyService.addCheckpointRemark(j.getJourneyId(), "MIDWAY", "Reached clock tower");

        // 3. Mark completed
        journeyService.updateJourneyStatus(j.getJourneyId(), JourneyStatus.COMPLETED, "Reached study hall safely");

        // 4. Verify nested history and generated completion alert
        List<JourneyDetails> history = journeyService.getJourneyHistoryForStudent(s.getStudentId());
        assertEquals(1, history.size());
        JourneyDetails details = history.get(0);
        assertEquals(JourneyStatus.COMPLETED, details.getJourney().getJourneyStatus());
        assertNotNull(details.getJourney().getActualArrivalTime());

        // Verification of auto-generated JOURNEY_COMPLETION alerts
        List<SafetyAlert> alerts = details.getAlerts();
        assertFalse(alerts.isEmpty());
        assertTrue(alerts.stream().anyMatch(a -> a.getAlertType() == AlertType.JOURNEY_COMPLETION && a.getContactId().equals(c1.getContactId())));
    }

    @Test
    @Order(3)
    void testSafetyWatchdogAndDeduplication() throws Exception {
        // Create student with contact
        String email = "watchdog." + System.currentTimeMillis() + "@campus.edu";
        Student s = studentService.registerStudent("Overdue Student", email, "+91-7777700001");
        studentService.addTrustedContact(s.getStudentId(), "Emergency Mother", "+91-7777711111", "Mother");

        // Create journey that is overdue: departure 2h ago, expected arrival 30 mins ago
        Timestamp departure = new Timestamp(System.currentTimeMillis() - 7200_000L);
        Timestamp expected = new Timestamp(System.currentTimeMillis() - 1800_000L);

        Journey overdueJourney = journeyService.startJourney(s.getStudentId(), "Lab 3", "Hostel", departure, expected, JourneyStatus.IN_PROGRESS, "Departed lab");
        assertTrue(overdueJourney.isOverdue());

        // Run initial scan
        SafetyWatchdogService.SafetyScanResult scan1 = watchdogService.runSafetyScan();
        assertTrue(scan1.getOverdueJourneysFound() >= 1);
        assertTrue(scan1.getNewAlertsTriggered() >= 1);

        // Run second scan: deduplication must ensure 0 new alerts for this trip
        SafetyWatchdogService.SafetyScanResult scan2 = watchdogService.runSafetyScan();
        assertEquals(0, scan2.getNewAlertsTriggered(), "Subsequent scan without new delays must trigger 0 new alerts");
        assertTrue(scan2.getExistingAlertsSkipped() >= 1, "Skipped count should reflect deduplicated contacts");
    }
}
