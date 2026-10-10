package com.passage.model;

public enum AlertType {
    DELAY_ALERT,
    JOURNEY_COMPLETION;

    public static AlertType fromString(String type) {
        if (type == null) return DELAY_ALERT;
        return switch (type.trim().toUpperCase()) {
            case "JOURNEY_COMPLETION", "COMPLETION" -> JOURNEY_COMPLETION;
            default -> DELAY_ALERT;
        };
    }
}
