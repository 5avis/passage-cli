package com.passage.ui;

public class MonochromeTheme {

    // Reset
    public static final String RESET = "\u001B[0m";

    // Text formatting
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String INVERTED = "\u001B[7m";

    // Standard ANSI
    public static final String BOLD_WHITE = "\u001B[1m\u001B[37m";
    public static final String REGULAR_WHITE = "\u001B[37m";
    public static final String DIM_GRAY = "\u001B[90m";

    // Truecolor RGB sequences for Antigravity-style modern aesthetic
    public static final String C_PURPLE = "\u001B[38;2;168;85;247m";   // Violet/Purple
    public static final String C_CYAN = "\u001B[38;2;6;182;212m";      // Electric Cyan
    public static final String C_BLUE = "\u001B[38;2;59;130;246m";     // Vivid Blue
    public static final String C_GREEN = "\u001B[38;2;34;197;94m";     // Emerald Green
    public static final String C_AMBER = "\u001B[38;2;245;158;11m";    // Amber/Orange
    public static final String C_ROSE = "\u001B[38;2;244;63;94m";      // Coral/Rose
    public static final String C_MUTED = "\u001B[38;2;148;163;184m";   // Slate Gray

    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    public static String dim(String text) {
        return DIM_GRAY + text + RESET;
    }

    public static String regular(String text) {
        return REGULAR_WHITE + text + RESET;
    }

    public static String cyan(String text) {
        return C_CYAN + text + RESET;
    }

    public static String purple(String text) {
        return C_PURPLE + text + RESET;
    }

    public static String green(String text) {
        return C_GREEN + text + RESET;
    }

    public static String amber(String text) {
        return C_AMBER + text + RESET;
    }

    public static String rose(String text) {
        return C_ROSE + text + RESET;
    }

    public static String badge(String text) {
        return statusBadge(text);
    }

    public static String statusBadge(String status) {
        if (status == null) status = "UNKNOWN";
        String s = status.trim().toUpperCase();
        return switch (s) {
            case "IN_PROGRESS" -> C_CYAN + BOLD + "[ IN PROGRESS ]" + RESET;
            case "COMPLETED" -> C_GREEN + BOLD + "[ COMPLETED ]" + RESET;
            case "OVERDUE" -> C_ROSE + BOLD + "[ OVERDUE ]" + RESET;
            case "CANCELLED" -> DIM_GRAY + BOLD + "[ CANCELLED ]" + RESET;
            case "PLANNED" -> C_PURPLE + BOLD + "[ PLANNED ]" + RESET;
            case "DELAY_ALERT" -> C_ROSE + BOLD + "[ DELAY ALERT ]" + RESET;
            case "JOURNEY_COMPLETION" -> C_GREEN + BOLD + "[ SAFE ARRIVAL ]" + RESET;
            case "SENT" -> C_CYAN + BOLD + "[ SENT ]" + RESET;
            case "ACKNOWLEDGED", "ACK" -> C_GREEN + BOLD + "[ ACKNOWLEDGED ]" + RESET;
            case "PENDING" -> C_AMBER + BOLD + "[ PENDING ]" + RESET;
            default -> BOLD_WHITE + "[ " + s + " ]" + RESET;
        };
    }

    public static String prompt() {
        return C_PURPLE + "◆ " + C_CYAN + "passage " + C_PURPLE + "❯ " + RESET;
    }

    /**
     * Polished Pixel Art Banner with finished typography and clean framing
     */
    public static String banner() {
        int w = 74;
        String emblemTop = C_PURPLE + "▟" + C_BLUE + "█" + C_CYAN + "▙" + RESET;
        String emblemBot = C_BLUE + "▜" + C_CYAN + "█" + C_GREEN + "▛" + RESET;
        String logoTop = C_CYAN + "█▀█ ▄▀█ █▀ █▀ ▄▀█ █▀▀ █▀▀" + RESET;
        String logoBot = C_PURPLE + "█▀  █▀█ ▄█ ▄█ █▀█ █▄█ ██▄" + RESET;
        String tagline = BOLD_WHITE + "Guarding Every Step of Your Journey" + RESET;
        String version = C_MUTED + "v1.0.0" + RESET;

        String line1Content = "  " + emblemTop + "  " + logoTop + "   " + tagline;
        String line2Content = "  " + emblemBot + "  " + logoBot + "   " + version;

        int pad1 = Math.max(1, w - 2 - AsciiTable.visibleLength(line1Content));
        int pad2 = Math.max(1, w - 2 - AsciiTable.visibleLength(line2Content));

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(C_PURPLE).append("╭").append("─".repeat(w - 2)).append("╮\n").append(RESET);
        sb.append(C_PURPLE).append("│").append(RESET).append(line1Content).append(" ".repeat(pad1)).append(C_PURPLE).append("│\n").append(RESET);
        sb.append(C_PURPLE).append("│").append(RESET).append(line2Content).append(" ".repeat(pad2)).append(C_PURPLE).append("│\n").append(RESET);
        sb.append(C_PURPLE).append("╰").append("─".repeat(w - 2)).append("╯\n").append(RESET);
        return sb.toString();
    }

    public static String sectionHeader(String title) {
        int width = 68;
        int pad = width - title.length() - 5;
        if (pad < 2) pad = 2;
        return "\n" + C_PURPLE + "╭── " + RESET + BOLD_WHITE + title.toUpperCase() + RESET + " " + DIM_GRAY + "─".repeat(pad) + RESET;
    }

    public static String card(String title, String... lines) {
        int maxWidth = 66;
        for (String l : lines) {
            if (l != null && l.length() > maxWidth) maxWidth = l.length();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(C_PURPLE).append("┌─ ").append(RESET).append(BOLD_WHITE).append(title).append(RESET).append(" ")
          .append(C_PURPLE).append("─".repeat(Math.max(2, maxWidth - title.length() - 2))).append("┐\n").append(RESET);

        for (String line : lines) {
            sb.append(C_PURPLE).append("│ ").append(RESET)
              .append(REGULAR_WHITE).append(String.format("%-" + maxWidth + "s", line != null ? line : "")).append(RESET)
              .append(C_PURPLE).append(" │\n").append(RESET);
        }

        sb.append(C_PURPLE).append("└").append("─".repeat(maxWidth + 2)).append("┘\n").append(RESET);
        return sb.toString();
    }
}
