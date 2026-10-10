package com.passage.ui;

public class MonochromeTheme {

    // Strictly monochrome ANSI sequences
    public static final String RESET = "\u001B[0m";
    public static final String BOLD_WHITE = "\u001B[1m\u001B[37m";
    public static final String REGULAR_WHITE = "\u001B[37m";
    public static final String DIM_GRAY = "\u001B[2m\u001B[37m";
    public static final String INVERTED = "\u001B[7m";

    public static String bold(String text) {
        return BOLD_WHITE + text + RESET;
    }

    public static String regular(String text) {
        return REGULAR_WHITE + text + RESET;
    }

    public static String dim(String text) {
        return DIM_GRAY + text + RESET;
    }

    public static String inverted(String text) {
        return INVERTED + text + RESET;
    }

    public static String badge(String text) {
        return " " + INVERTED + " " + text.trim() + " " + RESET + " ";
    }

    public static String statusBadge(String status) {
        if (status == null) status = "UNKNOWN";
        return badge(status.toUpperCase());
    }

    public static String alertBadge(String type, String status) {
        String typeLabel = "DELAY_ALERT".equalsIgnoreCase(type) ? "DELAY ALERT" : "COMPLETION";
        return badge(typeLabel + " : " + status.toUpperCase());
    }

    public static String prompt() {
        return BOLD_WHITE + "passage ❯ " + RESET;
    }

    public static String banner() {
        return DIM_GRAY + "┌────────────────────────────────────────────────────────────────────────┐\n" + RESET +
               DIM_GRAY + "│ " + RESET + BOLD_WHITE + "PASSAGE CLI" + RESET + "  " + DIM_GRAY + "v1.0.0" + RESET +
               "             " + REGULAR_WHITE + "College Travel Safety Platform" + RESET +
               "           " + DIM_GRAY + "│\n" + RESET +
               DIM_GRAY + "│ " + RESET + DIM_GRAY + "Minimalist Monochrome Interface      Powered by MariaDB / Pure JDBC      │\n" + RESET +
               DIM_GRAY + "└────────────────────────────────────────────────────────────────────────┘" + RESET;
    }

    public static String sectionHeader(String title) {
        int width = 72;
        int pad = width - title.length() - 5;
        if (pad < 2) pad = 2;
        return "\n" + DIM_GRAY + "─── " + RESET + BOLD_WHITE + title.toUpperCase() + RESET + " " + DIM_GRAY + "─".repeat(pad) + RESET;
    }

    public static String card(String title, String... lines) {
        int maxWidth = 68;
        for (String l : lines) {
            if (l != null && l.length() > maxWidth) maxWidth = l.length();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(DIM_GRAY).append("┌─ ").append(RESET).append(BOLD_WHITE).append(title).append(RESET).append(" ")
          .append(DIM_GRAY).append("─".repeat(Math.max(2, maxWidth - title.length() - 2))).append("┐\n").append(RESET);

        for (String line : lines) {
            sb.append(DIM_GRAY).append("│ ").append(RESET)
              .append(REGULAR_WHITE).append(String.format("%-" + maxWidth + "s", line != null ? line : "")).append(RESET)
              .append(DIM_GRAY).append(" │\n").append(RESET);
        }

        sb.append(DIM_GRAY).append("└").append("─".repeat(maxWidth + 2)).append("┘\n").append(RESET);
        return sb.toString();
    }
}
