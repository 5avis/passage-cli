package com.passage.model;

public enum AlertStatus {
    PENDING,
    SENT,
    ACKNOWLEDGED;

    public static AlertStatus fromString(String status) {
        if (status == null) return PENDING;
        return switch (status.trim().toUpperCase()) {
            case "SENT" -> SENT;
            case "ACKNOWLEDGED", "ACK" -> ACKNOWLEDGED;
            default -> PENDING;
        };
    }
}
