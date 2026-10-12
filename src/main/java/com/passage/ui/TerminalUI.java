package com.passage.ui;

import com.passage.model.*;
import com.passage.service.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TerminalUI {

    private final StudentService studentService = new StudentService();
    private final JourneyService journeyService = new JourneyService();
    private final SafetyWatchdogService watchdogService = new SafetyWatchdogService();
    private final DatabaseInitializer dbInitializer = new DatabaseInitializer();

    private final Scanner scanner = new Scanner(System.in);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public void start() {
        checkAndInitializeDatabase();

        boolean running = true;
        while (running) {
            displayMenu();
            String choice = readInput("Select operation [0-8]: ");

            switch (choice.trim()) {
                case "1" -> handleRegisterStudent();
                case "2" -> handleManageContacts();
                case "3" -> handleStartJourney();
                case "4" -> handleUpdateJourney();
                case "5" -> handleSafetyWatchdog();
                case "6" -> handleSafetyAlerts();
                case "7" -> handleStudentHistory();
                case "8" -> handleActiveJourneys();
                case "9" -> handlePurgeData();
                case "0", "exit", "quit", "q" -> {
                    System.out.println("\n" + MonochromeTheme.dim("Terminating Passage CLI session. Safe travels.\n"));
                    running = false;
                }
                default -> System.out.println(MonochromeTheme.dim("  Invalid selection. Please choose an option from 0 to 8."));
            }
        }
    }

    private void checkAndInitializeDatabase() {
        try {
            if (!dbInitializer.isSchemaInitialized()) {
                System.out.println(MonochromeTheme.dim("\n  Database tables not detected. Initializing schema..."));
                dbInitializer.initializeDatabase(true);
                System.out.println(MonochromeTheme.bold("  Database initialized successfully with seed records.\n"));
            }
        } catch (Exception e) {
            // Silently skip if DB is offline
        }
    }

    private void displayMenu() {
        boolean dbOk = com.passage.config.DBConnection.getInstance().testConnection();
        System.out.println(MonochromeTheme.banner(dbOk));
        System.out.println(MonochromeTheme.sectionHeader("Main Navigation"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [1] " + MonochromeTheme.BOLD_WHITE + "Register New Student" + MonochromeTheme.RESET + MonochromeTheme.dim("       Enroll student with contact details"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [2] " + MonochromeTheme.BOLD_WHITE + "Manage Trusted Contacts" + MonochromeTheme.RESET + MonochromeTheme.dim("    Add, inspect or remove safety contacts"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [3] " + MonochromeTheme.BOLD_WHITE + "Start / Schedule Journey" + MonochromeTheme.RESET + MonochromeTheme.dim("    Log new trip (PLANNED / IN_PROGRESS)"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [4] " + MonochromeTheme.BOLD_WHITE + "Update Journey & Checkpoints" + MonochromeTheme.RESET + MonochromeTheme.dim("  Add checkpoint, complete or cancel trip"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [5] " + MonochromeTheme.BOLD_WHITE + "Safety Watchdog Engine" + MonochromeTheme.RESET + MonochromeTheme.dim("      Scan active trips for overdue delays"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [6] " + MonochromeTheme.BOLD_WHITE + "View Safety Alerts" + MonochromeTheme.RESET + MonochromeTheme.dim("          Review & acknowledge safety notifications"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [7] " + MonochromeTheme.BOLD_WHITE + "Student Journey History" + MonochromeTheme.RESET + MonochromeTheme.dim("     Audit complete history with nested trees"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [8] " + MonochromeTheme.BOLD_WHITE + "Active Journeys Monitor" + MonochromeTheme.RESET + MonochromeTheme.dim("     Live tabular view of all underway trips"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [9] " + MonochromeTheme.BOLD_WHITE + "Purge All Database Records" + MonochromeTheme.RESET + MonochromeTheme.dim("  Delete all students, trips & alerts"));
        System.out.println("  " + MonochromeTheme.C_PURPLE + "✦ [0] " + MonochromeTheme.BOLD_WHITE + "Exit Application" + MonochromeTheme.RESET);
        System.out.println(MonochromeTheme.C_PURPLE + "╰" + MonochromeTheme.DIM_GRAY + "─".repeat(71) + MonochromeTheme.RESET);
    }

    // =========================================================================
    // 1. REGISTER STUDENT
    // =========================================================================
    private void handleRegisterStudent() {
        System.out.println(MonochromeTheme.sectionHeader("Register New Student"));
        String name = readInput("Full Name: ");
        if (name.isBlank()) {
            System.out.println(MonochromeTheme.dim("  Operation cancelled: Name cannot be blank."));
            return;
        }

        String email = readInput("Email Address: ");
        if (email.isBlank()) {
            System.out.println(MonochromeTheme.dim("  Operation cancelled: Email cannot be blank."));
            return;
        }

        String phone = readInput("Contact Phone: ");
        if (phone.isBlank()) {
            System.out.println(MonochromeTheme.dim("  Operation cancelled: Contact phone cannot be blank."));
            return;
        }

        try {
            Student student = studentService.registerStudent(name, email, phone);
            System.out.println("\n" + MonochromeTheme.card("STUDENT ENROLLED SUCCESSFULLY",
                    "Student ID   : " + student.getStudentId(),
                    "Name         : " + student.getStudentName(),
                    "Email        : " + student.getEmail(),
                    "Phone        : " + student.getContactInfo(),
                    "Status       : ACTIVE"));
        } catch (Exception e) {
            System.out.println(MonochromeTheme.dim("  Failed to register student: " + e.getMessage()));
        }
        pressEnterToContinue();
    }

    // =========================================================================
    // 2. MANAGE TRUSTED CONTACTS
    // =========================================================================
    private void handleManageContacts() {
        System.out.println(MonochromeTheme.sectionHeader("Manage Trusted Contacts"));
        listStudentsBrief();

        String sIdInput = readInput("Enter Student ID to inspect/manage contacts (or enter to cancel): ");
        if (sIdInput.isBlank()) return;

        try {
            int studentId = Integer.parseInt(sIdInput.trim());
            Optional<Student> studentOpt = studentService.getStudentById(studentId);
            if (studentOpt.isEmpty()) {
                System.out.println(MonochromeTheme.dim("  Student not found with ID " + studentId));
                pressEnterToContinue();
                return;
            }

            Student s = studentOpt.get();
            System.out.println("\n" + MonochromeTheme.bold("Contacts for: " + s.getStudentName() + " (" + s.getEmail() + ")"));
            renderContactsTable(studentId);

            System.out.println(MonochromeTheme.REGULAR_WHITE + "  [A] Add Contact  |  [D] Delete Contact  |  [B] Back to Menu");
            String action = readInput("Action: ").trim().toUpperCase();

            if ("A".equals(action)) {
                String cName = readInput("Contact Name: ");
                String cPhone = readInput("Contact Phone: ");
                String cRel = readInput("Relationship (e.g., Mother, Father, Guardian, Friend): ");
                if (!cName.isBlank() && !cPhone.isBlank() && !cRel.isBlank()) {
                    TrustedContact contact = studentService.addTrustedContact(studentId, cName, cPhone, cRel);
                    System.out.println(MonochromeTheme.bold("  Contact added successfully (ID: " + contact.getContactId() + ")."));
                } else {
                    System.out.println(MonochromeTheme.dim("  All fields are required. Aborted."));
                }
            } else if ("D".equals(action)) {
                String cIdInput = readInput("Enter Contact ID to delete: ");
                if (!cIdInput.isBlank()) {
                    int contactId = Integer.parseInt(cIdInput.trim());
                    boolean deleted = studentService.removeContact(contactId);
                    if (deleted) {
                        System.out.println(MonochromeTheme.bold("  Contact " + contactId + " removed."));
                    } else {
                        System.out.println(MonochromeTheme.dim("  Contact not found."));
                    }
                }
            }

        } catch (NumberFormatException e) {
            System.out.println(MonochromeTheme.dim("  Invalid integer ID."));
        } catch (Exception e) {
            System.out.println(MonochromeTheme.dim("  Error: " + e.getMessage()));
        }
        pressEnterToContinue();
    }

    private void renderContactsTable(int studentId) throws SQLException {
        List<TrustedContact> contacts = studentService.getContactsForStudent(studentId);
        AsciiTable table = new AsciiTable("Contact ID", "Name", "Phone", "Relationship");
        table.setEmptyMessage("No trusted contacts registered for this student.");
        for (TrustedContact c : contacts) {
            table.addRow(
                    String.valueOf(c.getContactId()),
                    c.getContactName(),
                    c.getContactPhone(),
                    c.getRelationship()
            );
        }
        System.out.print(table.render());
    }

    // =========================================================================
    // 3. START / SCHEDULE JOURNEY
    // =========================================================================
    private void handleStartJourney() {
        System.out.println(MonochromeTheme.sectionHeader("Start or Schedule New Journey"));
        listStudentsBrief();

        String sIdInput = readInput("Student ID: ");
        if (sIdInput.isBlank()) return;

        try {
            int studentId = Integer.parseInt(sIdInput.trim());
            Optional<Student> studentOpt = studentService.getStudentById(studentId);
            if (studentOpt.isEmpty()) {
                System.out.println(MonochromeTheme.dim("  Student not found with ID " + studentId));
                pressEnterToContinue();
                return;
            }

            String source = readInput("Source Location (e.g. North Gate Dorm): ");
            String destination = readInput("Destination (e.g. Metro Station): ");
            if (source.isBlank() || destination.isBlank()) {
                System.out.println(MonochromeTheme.dim("  Source and Destination are required."));
                pressEnterToContinue();
                return;
            }

            System.out.println(MonochromeTheme.REGULAR_WHITE + "  Initial Status:");
            System.out.println("  [1] IN_PROGRESS (Departing right now)");
            System.out.println("  [2] PLANNED     (Future scheduled trip)");
            String statusChoice = readInput("Choose [1/2, default 1]: ");
            JourneyStatus status = "2".equals(statusChoice.trim()) ? JourneyStatus.PLANNED : JourneyStatus.IN_PROGRESS;

            Timestamp departureTime;
            if (status == JourneyStatus.IN_PROGRESS) {
                departureTime = new Timestamp(System.currentTimeMillis());
                System.out.println(MonochromeTheme.dim("  Departure set to NOW (" + dateFormat.format(departureTime) + ")"));
            } else {
                departureTime = promptDateTime("Departure Time (yyyy-MM-dd HH:mm, or leave empty for +1h): ", 60);
            }

            String etaMins = readInput("Estimated travel duration in minutes (e.g. 45, or enter timestamp): ");
            Timestamp arrivalTime;
            if (etaMins.matches("\\d+")) {
                long durationMs = Long.parseLong(etaMins) * 60 * 1000L;
                arrivalTime = new Timestamp(departureTime.getTime() + durationMs);
            } else if (!etaMins.isBlank()) {
                try {
                    arrivalTime = new Timestamp(inputFormat.parse(etaMins.trim()).getTime());
                } catch (ParseException e) {
                    arrivalTime = new Timestamp(departureTime.getTime() + 45 * 60 * 1000L);
                }
            } else {
                arrivalTime = new Timestamp(departureTime.getTime() + 45 * 60 * 1000L); // Default 45 mins
            }

            String initialRemarks = readInput("Initial Checkpoint / Transport Note (optional): ");

            Journey journey = journeyService.startJourney(
                    studentId, source, destination, departureTime, arrivalTime, status, initialRemarks
            );

            System.out.println("\n" + MonochromeTheme.card("JOURNEY LOGGED CONFIRMATION",
                    "Journey ID       : " + journey.getJourneyId(),
                    "Student          : " + studentOpt.get().getStudentName(),
                    "Route            : " + journey.getSourceLocation() + " -> " + journey.getDestination(),
                    "Departure        : " + dateFormat.format(journey.getDepartureTime()),
                    "Expected Arrival : " + dateFormat.format(journey.getExpectedArrivalTime()),
                    "Status           : " + MonochromeTheme.statusBadge(journey.getJourneyStatus().name())));

        } catch (NumberFormatException e) {
            System.out.println(MonochromeTheme.dim("  Invalid numeric format."));
        } catch (Exception e) {
            System.out.println(MonochromeTheme.dim("  Error creating journey: " + e.getMessage()));
        }
        pressEnterToContinue();
    }

    // =========================================================================
    // 4. CHECK-IN & UPDATE JOURNEY STATUS
    // =========================================================================
    private void handleUpdateJourney() {
        System.out.println(MonochromeTheme.sectionHeader("Update Journey & Add Checkpoints"));

        try {
            List<Journey> activeJourneys = journeyService.getActiveJourneys();
            if (activeJourneys.isEmpty()) {
                System.out.println(MonochromeTheme.dim("  No active journeys currently underway or planned."));
                pressEnterToContinue();
                return;
            }

            renderActiveJourneysTable(activeJourneys);

            String jIdInput = readInput("Enter Journey ID to update: ");
            if (jIdInput.isBlank()) return;

            int journeyId = Integer.parseInt(jIdInput.trim());
            Optional<Journey> optJourney = journeyService.getJourneyById(journeyId);
            if (optJourney.isEmpty()) {
                System.out.println(MonochromeTheme.dim("  Journey ID not found."));
                pressEnterToContinue();
                return;
            }

            Journey j = optJourney.get();
            System.out.println("\n" + MonochromeTheme.bold("Managing Journey #" + j.getJourneyId() + " (" + j.getSourceLocation() + " -> " + j.getDestination() + ")"));
            System.out.println(MonochromeTheme.REGULAR_WHITE + "  [1] Add Intermediate Checkpoint Remark (Stay " + j.getJourneyStatus().name() + ")");
            System.out.println(MonochromeTheme.REGULAR_WHITE + "  [2] Mark COMPLETED (Auto-triggers safety notifications to contacts)");
            System.out.println(MonochromeTheme.REGULAR_WHITE + "  [3] Mark CANCELLED (Abort travel)");
            if (j.getJourneyStatus() == JourneyStatus.PLANNED) {
                System.out.println(MonochromeTheme.REGULAR_WHITE + "  [4] Transition to IN_PROGRESS (Commence trip now)");
            }

            String action = readInput("Select action: ").trim();
            if ("1".equals(action)) {
                String checkpointStatus = readInput("Current Checkpoint Status (e.g. IN_TRANSIT, TRAFFIC_HOLD, REST_STOP): ");
                String remark = readInput("Checkpoint Remark / Notes: ");
                journeyService.addCheckpointRemark(journeyId, checkpointStatus, remark);
                System.out.println(MonochromeTheme.bold("  Checkpoint logged to journey log."));

            } else if ("2".equals(action)) {
                String remark = readInput("Completion Remarks (e.g., Safely reached destination): ");
                journeyService.updateJourneyStatus(journeyId, JourneyStatus.COMPLETED, remark);
                System.out.println("\n" + MonochromeTheme.card("TRIP COMPLETED SUCCESSFULLY",
                        "Journey ID       : " + journeyId,
                        "Status           : " + MonochromeTheme.statusBadge("COMPLETED"),
                        "Actual Arrival   : " + dateFormat.format(new Date()),
                        "Safety Trigger   : JOURNEY_COMPLETION alerts auto-generated for trusted contacts."));

            } else if ("3".equals(action)) {
                String remark = readInput("Cancellation Reason: ");
                journeyService.updateJourneyStatus(journeyId, JourneyStatus.CANCELLED, remark);
                System.out.println(MonochromeTheme.bold("  Journey #" + journeyId + " marked as CANCELLED."));

            } else if ("4".equals(action) && j.getJourneyStatus() == JourneyStatus.PLANNED) {
                String remark = readInput("Departure Remarks: ");
                journeyService.updateJourneyStatus(journeyId, JourneyStatus.IN_PROGRESS, remark);
                System.out.println(MonochromeTheme.bold("  Journey #" + journeyId + " is now IN_PROGRESS."));
            }

        } catch (NumberFormatException e) {
            System.out.println(MonochromeTheme.dim("  Invalid numeric format."));
        } catch (Exception e) {
            System.out.println(MonochromeTheme.dim("  Error updating journey: " + e.getMessage()));
        }
        pressEnterToContinue();
    }

    // =========================================================================
    // 5. SAFETY WATCHDOG ENGINE
    // =========================================================================
    private void handleSafetyWatchdog() {
        System.out.println(MonochromeTheme.sectionHeader("Safety Watchdog Engine"));
        System.out.println(MonochromeTheme.dim("  Scanning active trips where journey_status = 'IN_PROGRESS' AND NOW() > expected_arrival_time...\n"));

        try {
            SafetyWatchdogService.SafetyScanResult result = watchdogService.runSafetyScan();

            AsciiTable summaryTable = new AsciiTable("Metric", "Value", "Description");
            summaryTable.addRow("Overdue Trips Found", String.valueOf(result.getOverdueJourneysFound()), "Trips past expected arrival time");
            summaryTable.addRow("New DELAY_ALERTS", String.valueOf(result.getNewAlertsTriggered()), "Fresh alerts dispatched to contacts");
            summaryTable.addRow("Skipped (Deduplicated)", String.valueOf(result.getExistingAlertsSkipped()), "Contacts already alerted previously");
            System.out.print(summaryTable.render());

            if (!result.getTriggeredAlerts().isEmpty()) {
                System.out.println("\n" + MonochromeTheme.bold("Dispatched Delay Alert Records:"));
                AsciiTable alertsTable = new AsciiTable("Alert ID", "Journey", "Recipient", "Relation", "Phone", "Status");
                for (SafetyAlert a : result.getTriggeredAlerts()) {
                    alertsTable.addRow(
                            String.valueOf(a.getAlertId()),
                            "#" + a.getJourneyId() + " (" + a.getSourceLocation() + " -> " + a.getDestination() + ")",
                            a.getContactName(),
                            a.getRelationship(),
                            a.getContactPhone(),
                            MonochromeTheme.badge(a.getAlertStatus().name())
                    );
                }
                System.out.print(alertsTable.render());
            } else if (result.getOverdueJourneysFound() == 0) {
                System.out.println(MonochromeTheme.dim("  [Watchdog Status: ALL CLEAR. No active journeys are overdue.]"));
            } else {
                System.out.println(MonochromeTheme.dim("  [Watchdog Status: Overdue journeys detected, but all contacts have already been alerted. Deduplication intact.]"));
            }

        } catch (Exception e) {
            System.out.println(MonochromeTheme.dim("  Watchdog scan error: " + e.getMessage()));
        }
        pressEnterToContinue();
    }

    // =========================================================================
    // 6. VIEW SAFETY ALERTS & ACKNOWLEDGE
    // =========================================================================
    private void handleSafetyAlerts() {
        System.out.println(MonochromeTheme.sectionHeader("Safety Alert Registry"));

        try {
            List<SafetyAlert> alerts = watchdogService.getAllAlerts();
            AsciiTable table = new AsciiTable("Alert ID", "Journey", "Student", "Recipient", "Phone", "Alert Type", "Status", "Timestamp");
            table.setEmptyMessage("No safety alerts recorded.");

            for (SafetyAlert a : alerts) {
                table.addRow(
                        String.valueOf(a.getAlertId()),
                        "#" + a.getJourneyId(),
                        a.getStudentName() != null ? a.getStudentName() : "-",
                        a.getContactName() + " (" + a.getRelationship() + ")",
                        a.getContactPhone(),
                        MonochromeTheme.statusBadge(a.getAlertType().name()),
                        MonochromeTheme.statusBadge(a.getAlertStatus().name()),
                        dateFormat.format(a.getAlertTime())
                );
            }
            System.out.print(table.render());

            System.out.println(MonochromeTheme.REGULAR_WHITE + "  [A] Acknowledge Alert  |  [B] Back to Menu");
            String action = readInput("Action: ").trim().toUpperCase();

            if ("A".equals(action)) {
                String aIdInput = readInput("Enter Alert ID to acknowledge: ");
                if (!aIdInput.isBlank()) {
                    int alertId = Integer.parseInt(aIdInput.trim());
                    boolean ok = watchdogService.acknowledgeAlert(alertId);
                    if (ok) {
                        System.out.println(MonochromeTheme.bold("  Alert #" + alertId + " status updated to ACKNOWLEDGED."));
                    } else {
                        System.out.println(MonochromeTheme.dim("  Alert ID not found or already acknowledged."));
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(MonochromeTheme.dim("  Error loading alerts: " + e.getMessage()));
        }
        pressEnterToContinue();
    }

    // =========================================================================
    // 7. STUDENT JOURNEY HISTORY & NESTED AUDIT REPORT
    // =========================================================================
    private void handleStudentHistory() {
        System.out.println(MonochromeTheme.sectionHeader("Student Journey History & Nested Audit"));
        listStudentsBrief();

        String sIdInput = readInput("Enter Student ID: ");
        if (sIdInput.isBlank()) return;

        try {
            int studentId = Integer.parseInt(sIdInput.trim());
            List<JourneyDetails> history = journeyService.getJourneyHistoryForStudent(studentId);

            if (history.isEmpty()) {
                Optional<Student> s = studentService.getStudentById(studentId);
                if (s.isEmpty()) {
                    System.out.println(MonochromeTheme.dim("  Student not found with ID " + studentId));
                } else {
                    System.out.println(MonochromeTheme.dim("  No journeys found for student: " + s.get().getStudentName()));
                }
                pressEnterToContinue();
                return;
            }

            Student student = history.get(0).getStudent();
            System.out.println("\n" + MonochromeTheme.card("STUDENT PROFILE",
                    "Student ID : " + student.getStudentId(),
                    "Name       : " + student.getStudentName(),
                    "Email      : " + student.getEmail(),
                    "Phone      : " + student.getContactInfo(),
                    "Total Trips: " + history.size()));

            System.out.println(MonochromeTheme.bold("\nTrip History & Nested Execution Log:"));

            for (JourneyDetails jd : history) {
                Journey j = jd.getJourney();
                System.out.println(MonochromeTheme.DIM_GRAY + "┌────────────────────────────────────────────────────────────────────────┐" + MonochromeTheme.RESET);
                System.out.printf(MonochromeTheme.DIM_GRAY + "│ " + MonochromeTheme.RESET + MonochromeTheme.BOLD_WHITE + "JOURNEY #%-4d" + MonochromeTheme.RESET + " %-42s %s" + MonochromeTheme.DIM_GRAY + "│\n" + MonochromeTheme.RESET,
                        j.getJourneyId(),
                        j.getSourceLocation() + " ➔ " + j.getDestination(),
                        MonochromeTheme.statusBadge(j.getJourneyStatus().name()));
                System.out.printf(MonochromeTheme.DIM_GRAY + "│ " + MonochromeTheme.RESET + MonochromeTheme.dim("Departed : %-22s  Expected : %-22s") + MonochromeTheme.DIM_GRAY + " │\n" + MonochromeTheme.RESET,
                        dateFormat.format(j.getDepartureTime()),
                        dateFormat.format(j.getExpectedArrivalTime()));
                if (j.getActualArrivalTime() != null) {
                    System.out.printf(MonochromeTheme.DIM_GRAY + "│ " + MonochromeTheme.RESET + MonochromeTheme.dim("Actual Arr: %-52s") + MonochromeTheme.DIM_GRAY + " │\n" + MonochromeTheme.RESET,
                            dateFormat.format(j.getActualArrivalTime()));
                }
                System.out.println(MonochromeTheme.DIM_GRAY + "├────────────────────────────────────────────────────────────────────────┤" + MonochromeTheme.RESET);

                // Nested Updates
                List<JourneyUpdate> updates = jd.getUpdates();
                System.out.println(MonochromeTheme.DIM_GRAY + "│ " + MonochromeTheme.RESET + MonochromeTheme.BOLD_WHITE + "├── Checkpoints & Status Updates (" + updates.size() + "):" + MonochromeTheme.RESET);
                if (updates.isEmpty()) {
                    System.out.println(MonochromeTheme.DIM_GRAY + "│ │   (No updates logged)" + MonochromeTheme.RESET);
                } else {
                    for (int i = 0; i < updates.size(); i++) {
                        JourneyUpdate u = updates.get(i);
                        String connector = (i == updates.size() - 1 && jd.getAlerts().isEmpty()) ? "└──" : "├──";
                        System.out.printf(MonochromeTheme.DIM_GRAY + "│ │   %s [%s] " + MonochromeTheme.RESET + "%-12s " + MonochromeTheme.dim("• %s") + "\n",
                                connector,
                                dateFormat.format(u.getUpdateTime()),
                                u.getCurrentStatus(),
                                u.getRemarks() != null ? u.getRemarks() : "-");
                    }
                }

                // Nested Alerts
                List<SafetyAlert> alerts = jd.getAlerts();
                System.out.println(MonochromeTheme.DIM_GRAY + "│ " + MonochromeTheme.RESET + MonochromeTheme.BOLD_WHITE + "└── Generated Safety Alerts (" + alerts.size() + "):" + MonochromeTheme.RESET);
                if (alerts.isEmpty()) {
                    System.out.println(MonochromeTheme.DIM_GRAY + "│     (No safety alerts triggered)" + MonochromeTheme.RESET);
                } else {
                    for (int i = 0; i < alerts.size(); i++) {
                        SafetyAlert a = alerts.get(i);
                        String connector = (i == alerts.size() - 1) ? "└──" : "├──";
                        System.out.printf(MonochromeTheme.DIM_GRAY + "│     %s [%s] " + MonochromeTheme.RESET + "%s %s " + MonochromeTheme.dim("➔ %s (%s) %s") + "\n",
                                connector,
                                dateFormat.format(a.getAlertTime()),
                                MonochromeTheme.badge(a.getAlertType().name()),
                                MonochromeTheme.badge(a.getAlertStatus().name()),
                                a.getContactName(),
                                a.getRelationship(),
                                a.getContactPhone());
                    }
                }

                System.out.println(MonochromeTheme.DIM_GRAY + "└────────────────────────────────────────────────────────────────────────┘" + MonochromeTheme.RESET);
                System.out.println();
            }

        } catch (NumberFormatException e) {
            System.out.println(MonochromeTheme.dim("  Invalid numeric format."));
        } catch (Exception e) {
            System.out.println(MonochromeTheme.dim("  Error loading history: " + e.getMessage()));
        }
        pressEnterToContinue();
    }

    // =========================================================================
    // 8. ACTIVE JOURNEYS MONITOR
    // =========================================================================
    private void handleActiveJourneys() {
        System.out.println(MonochromeTheme.sectionHeader("Active Journeys Monitor"));

        try {
            List<Journey> active = journeyService.getActiveJourneys();
            renderActiveJourneysTable(active);
        } catch (Exception e) {
            System.out.println(MonochromeTheme.dim("  Error retrieving active journeys: " + e.getMessage()));
        }
        pressEnterToContinue();
    }

    private void renderActiveJourneysTable(List<Journey> list) {
        AsciiTable table = new AsciiTable("Journey ID", "Student", "Source", "Destination", "Departure", "Expected ETA", "Status", "Flag");
        table.setEmptyMessage("No active or planned journeys underway.");

        for (Journey j : list) {
            String overdueFlag = j.isOverdue() ? MonochromeTheme.badge("OVERDUE") : "ON TIME";
            table.addRow(
                    String.valueOf(j.getJourneyId()),
                    j.getStudentName() != null ? j.getStudentName() : ("ID:" + j.getStudentId()),
                    j.getSourceLocation(),
                    j.getDestination(),
                    dateFormat.format(j.getDepartureTime()),
                    dateFormat.format(j.getExpectedArrivalTime()),
                    MonochromeTheme.badge(j.getJourneyStatus().name()),
                    overdueFlag
            );
        }
        System.out.print(table.render());
    }

    // =========================================================================
    // 9. PURGE ALL DATABASE DATA
    // =========================================================================
    private void handlePurgeData() {
        System.out.println(MonochromeTheme.sectionHeader("Purge All Database Records"));
        System.out.println(MonochromeTheme.dim("  WARNING: This will permanently delete all students, trusted contacts,"));
        System.out.println(MonochromeTheme.dim("  journeys, updates, and safety alerts from database 'ridesafe'.\n"));

        String confirm = readInput("Are you sure you want to delete ALL data? Type 'YES' to confirm: ");
        if ("YES".equalsIgnoreCase(confirm.trim())) {
            try {
                dbInitializer.truncateAllData();
                System.out.println("\n" + MonochromeTheme.bold("  All database records have been purged successfully."));
            } catch (Exception e) {
                System.out.println(MonochromeTheme.dim("  Failed to purge data: " + e.getMessage()));
            }
        } else {
            System.out.println(MonochromeTheme.dim("  Purge aborted. No data was deleted."));
        }
        pressEnterToContinue();
    }

    // =========================================================================
    // HELPER UTILITIES
    // =========================================================================
    private void listStudentsBrief() {
        try {
            List<Student> students = studentService.getAllStudents();
            AsciiTable table = new AsciiTable("Student ID", "Full Name", "Email", "Phone");
            table.setEmptyMessage("No students currently enrolled.");
            for (Student s : students) {
                table.addRow(String.valueOf(s.getStudentId()), s.getStudentName(), s.getEmail(), s.getContactInfo());
            }
            System.out.print(table.render());
        } catch (Exception e) {
            System.out.println(MonochromeTheme.dim("  Could not fetch students list."));
        }
    }

    private String readInput(String prompt) {
        System.out.print(MonochromeTheme.C_PURPLE + "◆ " + MonochromeTheme.BOLD_WHITE + prompt + MonochromeTheme.RESET);
        return scanner.nextLine();
    }

    private Timestamp promptDateTime(String prompt, int defaultMinutesFromNow) {
        String input = readInput(prompt);
        if (input.isBlank()) {
            return new Timestamp(System.currentTimeMillis() + defaultMinutesFromNow * 60 * 1000L);
        }
        try {
            Date parsed = inputFormat.parse(input.trim());
            return new Timestamp(parsed.getTime());
        } catch (ParseException e) {
            System.out.println(MonochromeTheme.dim("  Format unrecognized (expected yyyy-MM-dd HH:mm). Using offset from now."));
            return new Timestamp(System.currentTimeMillis() + defaultMinutesFromNow * 60 * 1000L);
        }
    }

    private void pressEnterToContinue() {
        System.out.print(MonochromeTheme.dim("\n  Press [Enter] to continue..."));
        scanner.nextLine();
    }
}
