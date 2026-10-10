package com.passage.dao;

import com.passage.config.DBConnection;
import com.passage.model.Journey;
import com.passage.model.JourneyStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JourneyDao {

    private final DBConnection db = DBConnection.getInstance();

    public int create(Journey journey) throws SQLException {
        String sql = "INSERT INTO JOURNEYS (student_id, source_location, destination, departure_time, expected_arrival_time, actual_arrival_time, journey_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, journey.getStudentId());
            ps.setString(2, journey.getSourceLocation());
            ps.setString(3, journey.getDestination());
            ps.setTimestamp(4, journey.getDepartureTime());
            ps.setTimestamp(5, journey.getExpectedArrivalTime());
            if (journey.getActualArrivalTime() != null) {
                ps.setTimestamp(6, journey.getActualArrivalTime());
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.setString(7, journey.getJourneyStatus().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    journey.setJourneyId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Optional<Journey> findById(int journeyId) throws SQLException {
        String sql = "SELECT j.journey_id, j.student_id, j.source_location, j.destination, " +
                     "j.departure_time, j.expected_arrival_time, j.actual_arrival_time, j.journey_status, j.created_at, " +
                     "s.student_name, s.email " +
                     "FROM JOURNEYS j " +
                     "JOIN STUDENTS s ON j.student_id = s.student_id " +
                     "WHERE j.journey_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journeyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Journey> findByStudentId(int studentId) throws SQLException {
        List<Journey> list = new ArrayList<>();
        String sql = "SELECT j.journey_id, j.student_id, j.source_location, j.destination, " +
                     "j.departure_time, j.expected_arrival_time, j.actual_arrival_time, j.journey_status, j.created_at, " +
                     "s.student_name, s.email " +
                     "FROM JOURNEYS j " +
                     "JOIN STUDENTS s ON j.student_id = s.student_id " +
                     "WHERE j.student_id = ? " +
                     "ORDER BY j.departure_time DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Journey> findOverdueInProgressJourneys() throws SQLException {
        List<Journey> list = new ArrayList<>();
        // Query active trips where journey_status = 'IN_PROGRESS' AND NOW() > expected_arrival_time
        String sql = "SELECT j.journey_id, j.student_id, j.source_location, j.destination, " +
                     "j.departure_time, j.expected_arrival_time, j.actual_arrival_time, j.journey_status, j.created_at, " +
                     "s.student_name, s.email " +
                     "FROM JOURNEYS j " +
                     "JOIN STUDENTS s ON j.student_id = s.student_id " +
                     "WHERE j.journey_status = 'IN_PROGRESS' AND NOW() > j.expected_arrival_time " +
                     "ORDER BY j.expected_arrival_time ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Journey> findActiveJourneys() throws SQLException {
        List<Journey> list = new ArrayList<>();
        String sql = "SELECT j.journey_id, j.student_id, j.source_location, j.destination, " +
                     "j.departure_time, j.expected_arrival_time, j.actual_arrival_time, j.journey_status, j.created_at, " +
                     "s.student_name, s.email " +
                     "FROM JOURNEYS j " +
                     "JOIN STUDENTS s ON j.student_id = s.student_id " +
                     "WHERE j.journey_status IN ('PLANNED', 'IN_PROGRESS') " +
                     "ORDER BY j.departure_time ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Journey> findAll() throws SQLException {
        List<Journey> list = new ArrayList<>();
        String sql = "SELECT j.journey_id, j.student_id, j.source_location, j.destination, " +
                     "j.departure_time, j.expected_arrival_time, j.actual_arrival_time, j.journey_status, j.created_at, " +
                     "s.student_name, s.email " +
                     "FROM JOURNEYS j " +
                     "JOIN STUDENTS s ON j.student_id = s.student_id " +
                     "ORDER BY j.journey_id DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean updateStatus(int journeyId, JourneyStatus status, Timestamp actualArrivalTime) throws SQLException {
        String sql = "UPDATE JOURNEYS SET journey_status = ?, actual_arrival_time = ? WHERE journey_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            if (actualArrivalTime != null) {
                ps.setTimestamp(2, actualArrivalTime);
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setInt(3, journeyId);
            return ps.executeUpdate() > 0;
        }
    }

    private Journey mapRow(ResultSet rs) throws SQLException {
        Journey journey = new Journey(
                rs.getInt("journey_id"),
                rs.getInt("student_id"),
                rs.getString("source_location"),
                rs.getString("destination"),
                rs.getTimestamp("departure_time"),
                rs.getTimestamp("expected_arrival_time"),
                rs.getTimestamp("actual_arrival_time"),
                JourneyStatus.fromString(rs.getString("journey_status")),
                rs.getTimestamp("created_at")
        );
        try {
            journey.setStudentName(rs.getString("student_name"));
            journey.setStudentEmail(rs.getString("email"));
        } catch (SQLException ignored) {}
        return journey;
    }
}
