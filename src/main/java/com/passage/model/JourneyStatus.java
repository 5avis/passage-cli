package com.passage.model;

public enum JourneyStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public static JourneyStatus fromString(String status) {
        if (status == null) return PLANNED;
        return switch (status.trim().toUpperCase()) {
            case "IN_PROGRESS", "INPROGRESS" -> IN_PROGRESS;
            case "COMPLETED", "COMPLETE" -> COMPLETED;
            case "CANCELLED", "CANCELED" -> CANCELLED;
            default -> PLANNED;
        };
    }
}
