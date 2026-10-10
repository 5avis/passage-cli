package com.passage;

import com.passage.model.Journey;
import com.passage.model.JourneyDetails;
import com.passage.model.SafetyAlert;
import com.passage.service.DatabaseInitializer;
import com.passage.service.JourneyService;
import com.passage.service.SafetyWatchdogService;
import com.passage.ui.AsciiTable;
import com.passage.ui.MonochromeTheme;
import com.passage.ui.TerminalUI;

import java.text.SimpleDateFormat;
import java.util.List;

public class Main {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        if (args.length == 0) {
            // Default: Interactive Terminal UI
            new TerminalUI().start();
            return;
        }

        String command = args[0].toLowerCase();
        try {
            switch (command) {
                case "--scan", "-s" -> runHeadlessScan();
                case "--active", "-a" -> runHeadlessActiveJourneys();
                case "--alerts" -> runHeadlessAlerts();
                case "--history" -> {
                    if (args.length < 2) {
                        System.out.println(MonochromeTheme.dim("Usage: passage --history <student_id>"));
                        return;
                    }
                    runHeadlessHistory(Integer.parseInt(args[1]));
                }
                case "--init" -> {
                    System.out.println(MonochromeTheme.bold("Initializing database schema from schema.sql..."));
                    new DatabaseInitializer().initializeDatabase(false);
                    System.out.println(MonochromeTheme.bold("Database schema initialized successfully."));
                }
                case "--seed" -> {
                    System.out.println(MonochromeTheme.bold("Seeding database schema and test data from schema.sql..."));
                    new DatabaseInitializer().initializeDatabase(true);
                    System.out.println(MonochromeTheme.bold("Database seeded successfully."));
                }
                case "--clean", "--clear", "--truncate" -> {
                    System.out.println(MonochromeTheme.bold("Deleting all data across all tables in ridesafe..."));
                    new DatabaseInitializer().truncateAllData();
                    System.out.println(MonochromeTheme.bold("All database records have been deleted successfully."));
                }
                case "--help", "-h", "help" -> printHelp();
                default -> {
                    System.out.println(MonochromeTheme.dim("Unknown option: " + command));
                    printHelp();
                }
            }
        } catch (Exception e) {
            System.err.println(MonochromeTheme.bold("Error: ") + e.getMessage());
            System.exit(1);
        }
    }

    private static void runHeadlessScan() throws Exception {
        System.out.println(MonochromeTheme.banner());
        System.out.println(MonochromeTheme.sectionHeader("Automated Safety Watchdog Scan"));
        SafetyWatchdogService watchdog = new SafetyWatchdogService();
        SafetyWatchdogService.SafetyScanResult result = watchdog.runSafetyScan();

        AsciiTable table = new AsciiTable("Metric", "Count", "Description");
        table.addRow("Overdue Trips Found", String.valueOf(result.getOverdueJourneysFound()), "Active journeys past expected arrival");
        table.addRow("New Alerts Dispatched", String.valueOf(result.getNewAlertsTriggered()), "DELAY_ALERT dispatched to trusted contacts");
        table.addRow("Existing Skipped", String.valueOf(result.getExistingAlertsSkipped()), "Deduplicated contacts already alerted");
        System.out.print(table.render());

        if (!result.getTriggeredAlerts().isEmpty()) {
            System.out.println("\n" + MonochromeTheme.bold("Dispatched Alerts:"));
            AsciiTable alertsTable = new AsciiTable("Alert ID", "Journey", "Recipient", "Phone", "Status");
            for (SafetyAlert a : result.getTriggeredAlerts()) {
                alertsTable.addRow(
                        String.valueOf(a.getAlertId()),
                        "#" + a.getJourneyId(),
                        a.getContactName() + " (" + a.getRelationship() + ")",
                        a.getContactPhone(),
                        MonochromeTheme.badge(a.getAlertStatus().name())
                );
            }
            System.out.print(alertsTable.render());
        }
    }

    private static void runHeadlessActiveJourneys() throws Exception {
        System.out.println(MonochromeTheme.banner());
        System.out.println(MonochromeTheme.sectionHeader("Active Journeys Monitor"));
        JourneyService journeyService = new JourneyService();
        List<Journey> active = journeyService.getActiveJourneys();

        AsciiTable table = new AsciiTable("ID", "Student", "Source", "Destination", "Departure", "ETA", "Status", "Flag");
        table.setEmptyMessage("No active journeys.");
        for (Journey j : active) {
            table.addRow(
                    String.valueOf(j.getJourneyId()),
                    j.getStudentName() != null ? j.getStudentName() : ("ID:" + j.getStudentId()),
                    j.getSourceLocation(),
                    j.getDestination(),
                    DATE_FORMAT.format(j.getDepartureTime()),
                    DATE_FORMAT.format(j.getExpectedArrivalTime()),
                    MonochromeTheme.badge(j.getJourneyStatus().name()),
                    j.isOverdue() ? MonochromeTheme.badge("OVERDUE") : "ON TIME"
            );
        }
        System.out.print(table.render());
    }

    private static void runHeadlessAlerts() throws Exception {
        System.out.println(MonochromeTheme.banner());
        System.out.println(MonochromeTheme.sectionHeader("Safety Alert Registry"));
        SafetyWatchdogService watchdog = new SafetyWatchdogService();
        List<SafetyAlert> alerts = watchdog.getAllAlerts();

        AsciiTable table = new AsciiTable("Alert ID", "Journey", "Recipient", "Phone", "Type", "Status", "Timestamp");
        table.setEmptyMessage("No alerts recorded.");
        for (SafetyAlert a : alerts) {
            table.addRow(
                    String.valueOf(a.getAlertId()),
                    "#" + a.getJourneyId(),
                    a.getContactName() + " (" + a.getRelationship() + ")",
                    a.getContactPhone(),
                    MonochromeTheme.badge(a.getAlertType().name()),
                    MonochromeTheme.badge(a.getAlertStatus().name()),
                    DATE_FORMAT.format(a.getAlertTime())
            );
        }
        System.out.print(table.render());
    }

    private static void runHeadlessHistory(int studentId) throws Exception {
        System.out.println(MonochromeTheme.banner());
        JourneyService journeyService = new JourneyService();
        List<JourneyDetails> history = journeyService.getJourneyHistoryForStudent(studentId);
        if (history.isEmpty()) {
            System.out.println(MonochromeTheme.dim("No history records found for student ID " + studentId));
            return;
        }

        System.out.println(MonochromeTheme.sectionHeader("Journey Audit: " + history.get(0).getStudent().getStudentName()));
        for (JourneyDetails jd : history) {
            Journey j = jd.getJourney();
            System.out.printf("\n#%d %s ➔ %s [%s]\n", j.getJourneyId(), j.getSourceLocation(), j.getDestination(), j.getJourneyStatus().name());
            System.out.printf("   Departure: %s | Expected: %s\n", DATE_FORMAT.format(j.getDepartureTime()), DATE_FORMAT.format(j.getExpectedArrivalTime()));
            System.out.println("   Updates: " + jd.getUpdates().size() + " checkpoints | Alerts: " + jd.getAlerts().size() + " dispatched");
        }
    }

    private static void printHelp() {
        System.out.println(MonochromeTheme.banner());
        System.out.println("\n" + MonochromeTheme.bold("USAGE:") + " passage [options]");
        System.out.println("\n" + MonochromeTheme.bold("OPTIONS:"));
        System.out.println("  (no args)             Launch interactive monochrome terminal interface");
        System.out.println("  --scan, -s            Run safety watchdog scan on all overdue journeys");
        System.out.println("  --active, -a          Display table of currently active journeys");
        System.out.println("  --alerts              Display all generated safety alerts");
        System.out.println("  --history <id>        Display nested journey history for student ID");
        System.out.println("  --init                Initialize database tables from schema.sql");
        System.out.println("  --seed                Reset and seed database with 3 full scenarios");
        System.out.println("  --clean               Delete all data across all tables (truncate)");
        System.out.println("  --help, -h            Show this help dialog");
    }
}
