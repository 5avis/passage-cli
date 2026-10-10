package com.passage.dao;

import com.passage.config.DBConnection;
import com.passage.model.AlertStatus;
import com.passage.model.AlertType;
import com.passage.model.SafetyAlert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SafetyAlertDao {

    private final DBConnection db = DBConnection.getInstance();

    public int create(SafetyAlert alert) throws SQLException {
        // Use INSERT IGNORE or explicit check to guarantee no duplicates
        String sql = "INSERT INTO SAFETY_ALERTS (journey_id, contact_id, alert_type, alert_time, alert_status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, alert.getJourneyId());
            ps.setInt(2, alert.getContactId());
            ps.setString(3, alert.getAlertType().name());
            if (alert.getAlertTime() != null) {
                ps.setTimestamp(4, alert.getAlertTime());
            } else {
                ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            }
            ps.setString(5, alert.getAlertStatus() != null ? alert.getAlertStatus().name() : AlertStatus.PENDING.name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    alert.setAlertId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean exists(int journeyId, int contactId, AlertType alertType) throws SQLException {
        String sql = "SELECT 1 FROM SAFETY_ALERTS WHERE journey_id = ? AND contact_id = ? AND alert_type = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journeyId);
            ps.setInt(2, contactId);
            ps.setString(3, alertType.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<SafetyAlert> findByJourneyId(int journeyId) throws SQLException {
        List<SafetyAlert> list = new ArrayList<>();
        String sql = "SELECT a.alert_id, a.journey_id, a.contact_id, a.alert_type, a.alert_time, a.alert_status, " +
                     "c.contact_name, c.contact_phone, c.relationship, " +
                     "s.student_name, j.source_location, j.destination " +
                     "FROM SAFETY_ALERTS a " +
                     "JOIN TRUSTED_CONTACTS c ON a.contact_id = c.contact_id " +
                     "JOIN JOURNEYS j ON a.journey_id = j.journey_id " +
                     "JOIN STUDENTS s ON j.student_id = s.student_id " +
                     "WHERE a.journey_id = ? " +
                     "ORDER BY a.alert_time ASC, a.alert_id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journeyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<SafetyAlert> findAll() throws SQLException {
        List<SafetyAlert> list = new ArrayList<>();
        String sql = "SELECT a.alert_id, a.journey_id, a.contact_id, a.alert_type, a.alert_time, a.alert_status, " +
                     "c.contact_name, c.contact_phone, c.relationship, " +
                     "s.student_name, j.source_location, j.destination " +
                     "FROM SAFETY_ALERTS a " +
                     "JOIN TRUSTED_CONTACTS c ON a.contact_id = c.contact_id " +
                     "JOIN JOURNEYS j ON a.journey_id = j.journey_id " +
                     "JOIN STUDENTS s ON j.student_id = s.student_id " +
                     "ORDER BY a.alert_time DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<SafetyAlert> findPendingAlerts() throws SQLException {
        List<SafetyAlert> list = new ArrayList<>();
        String sql = "SELECT a.alert_id, a.journey_id, a.contact_id, a.alert_type, a.alert_time, a.alert_status, " +
                     "c.contact_name, c.contact_phone, c.relationship, " +
                     "s.student_name, j.source_location, j.destination " +
                     "FROM SAFETY_ALERTS a " +
                     "JOIN TRUSTED_CONTACTS c ON a.contact_id = c.contact_id " +
                     "JOIN JOURNEYS j ON a.journey_id = j.journey_id " +
                     "JOIN STUDENTS s ON j.student_id = s.student_id " +
                     "WHERE a.alert_status = 'PENDING' " +
                     "ORDER BY a.alert_time ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<SafetyAlert> findById(int alertId) throws SQLException {
        String sql = "SELECT a.alert_id, a.journey_id, a.contact_id, a.alert_type, a.alert_time, a.alert_status, " +
                     "c.contact_name, c.contact_phone, c.relationship, " +
                     "s.student_name, j.source_location, j.destination " +
                     "FROM SAFETY_ALERTS a " +
                     "JOIN TRUSTED_CONTACTS c ON a.contact_id = c.contact_id " +
                     "JOIN JOURNEYS j ON a.journey_id = j.journey_id " +
                     "JOIN STUDENTS s ON j.student_id = s.student_id " +
                     "WHERE a.alert_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alertId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateStatus(int alertId, AlertStatus status) throws SQLException {
        String sql = "UPDATE SAFETY_ALERTS SET alert_status = ? WHERE alert_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, alertId);
            return ps.executeUpdate() > 0;
        }
    }

    private SafetyAlert mapRow(ResultSet rs) throws SQLException {
        SafetyAlert alert = new SafetyAlert(
                rs.getInt("alert_id"),
                rs.getInt("journey_id"),
                rs.getInt("contact_id"),
                AlertType.fromString(rs.getString("alert_type")),
                rs.getTimestamp("alert_time"),
                AlertStatus.fromString(rs.getString("alert_status"))
        );
        try {
            alert.setContactName(rs.getString("contact_name"));
            alert.setContactPhone(rs.getString("contact_phone"));
            alert.setRelationship(rs.getString("relationship"));
            alert.setStudentName(rs.getString("student_name"));
            alert.setSourceLocation(rs.getString("source_location"));
            alert.setDestination(rs.getString("destination"));
        } catch (SQLException ignored) {}
        return alert;
    }
}
