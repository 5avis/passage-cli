package com.passage.dao;

import com.passage.config.DBConnection;
import com.passage.model.JourneyUpdate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JourneyUpdateDao {

    private final DBConnection db = DBConnection.getInstance();

    public int create(JourneyUpdate update) throws SQLException {
        String sql = "INSERT INTO JOURNEY_UPDATES (journey_id, update_time, current_status, remarks) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, update.getJourneyId());
            if (update.getUpdateTime() != null) {
                ps.setTimestamp(2, update.getUpdateTime());
            } else {
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }
            ps.setString(3, update.getCurrentStatus());
            ps.setString(4, update.getRemarks());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    update.setUpdateId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public List<JourneyUpdate> findByJourneyId(int journeyId) throws SQLException {
        List<JourneyUpdate> list = new ArrayList<>();
        String sql = "SELECT update_id, journey_id, update_time, current_status, remarks " +
                     "FROM JOURNEY_UPDATES WHERE journey_id = ? ORDER BY update_time ASC, update_id ASC";
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

    public Optional<JourneyUpdate> getLatestUpdate(int journeyId) throws SQLException {
        String sql = "SELECT update_id, journey_id, update_time, current_status, remarks " +
                     "FROM JOURNEY_UPDATES WHERE journey_id = ? ORDER BY update_time DESC, update_id DESC LIMIT 1";
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

    private JourneyUpdate mapRow(ResultSet rs) throws SQLException {
        return new JourneyUpdate(
                rs.getInt("update_id"),
                rs.getInt("journey_id"),
                rs.getTimestamp("update_time"),
                rs.getString("current_status"),
                rs.getString("remarks")
        );
    }
}
