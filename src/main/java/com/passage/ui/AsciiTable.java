package com.passage.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AsciiTable {

    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[[;?0-9]*[a-zA-Z]");

    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();
    private String emptyMessage = "No records found.";

    public AsciiTable(String... headers) {
        this.headers.addAll(Arrays.asList(headers));
    }

    public void addRow(String... cells) {
        rows.add(Arrays.asList(cells));
    }

    public void setEmptyMessage(String message) {
        this.emptyMessage = message;
    }

    public static int visibleLength(String s) {
        if (s == null) return 0;
        return ANSI_PATTERN.matcher(s).replaceAll("").length();
    }

    public String render() {
        if (rows.isEmpty()) {
            return MonochromeTheme.dim("  [" + emptyMessage + "]\n");
        }

        int colCount = headers.size();
        int[] colWidths = new int[colCount];

        for (int i = 0; i < colCount; i++) {
            colWidths[i] = visibleLength(headers.get(i));
        }

        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(colCount, row.size()); i++) {
                int len = visibleLength(row.get(i));
                if (len > colWidths[i]) {
                    colWidths[i] = len;
                }
            }
        }

        // Add 2 padding spaces to each column
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = Math.max(colWidths[i], 3);
        }

        StringBuilder sb = new StringBuilder();

        // 1. Top border: ┌───┬───┐
        sb.append(MonochromeTheme.C_PURPLE).append("┌");
        for (int i = 0; i < colCount; i++) {
            sb.append("─".repeat(colWidths[i] + 2));
            if (i < colCount - 1) sb.append("┬");
        }
        sb.append("┐\n").append(MonochromeTheme.RESET);

        // 2. Headers: │ COL1 │ COL2 │
        sb.append(MonochromeTheme.C_PURPLE).append("│").append(MonochromeTheme.RESET);
        for (int i = 0; i < colCount; i++) {
            String h = headers.get(i);
            int pad = colWidths[i] - visibleLength(h);
            sb.append(" ").append(MonochromeTheme.C_CYAN).append(MonochromeTheme.BOLD).append(h).append(MonochromeTheme.RESET)
              .append(" ".repeat(pad + 1))
              .append(MonochromeTheme.C_PURPLE).append("│").append(MonochromeTheme.RESET);
        }
        sb.append("\n");

        // 3. Header separator: ├───┼───┤
        sb.append(MonochromeTheme.C_PURPLE).append("├");
        for (int i = 0; i < colCount; i++) {
            sb.append("─".repeat(colWidths[i] + 2));
            if (i < colCount - 1) sb.append("┼");
        }
        sb.append("┤\n").append(MonochromeTheme.RESET);

        // 4. Data rows
        for (List<String> row : rows) {
            sb.append(MonochromeTheme.DIM_GRAY).append("│").append(MonochromeTheme.RESET);
            for (int i = 0; i < colCount; i++) {
                String val = (i < row.size() && row.get(i) != null) ? row.get(i) : "";
                int pad = colWidths[i] - visibleLength(val);
                sb.append(" ").append(val).append(" ".repeat(Math.max(0, pad) + 1))
                  .append(MonochromeTheme.DIM_GRAY).append("│").append(MonochromeTheme.RESET);
            }
            sb.append("\n");
        }

        // 5. Bottom border: └───┴───┘
        sb.append(MonochromeTheme.DIM_GRAY).append("└");
        for (int i = 0; i < colCount; i++) {
            sb.append("─".repeat(colWidths[i] + 2));
            if (i < colCount - 1) sb.append("┴");
        }
        sb.append("┘\n").append(MonochromeTheme.RESET);

        return sb.toString();
    }
}
