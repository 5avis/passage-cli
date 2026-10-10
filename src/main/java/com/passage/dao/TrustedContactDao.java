package com.passage.dao;

import com.passage.config.DBConnection;
import com.passage.model.TrustedContact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrustedContactDao {

    private final DBConnection db = DBConnection.getInstance();

    public int create(TrustedContact contact) throws SQLException {
        String sql = "INSERT INTO TRUSTED_CONTACTS (student_id, contact_name, contact_phone, relationship) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, contact.getStudentId());
            ps.setString(2, contact.getContactName());
            ps.setString(3, contact.getContactPhone());
            ps.setString(4, contact.getRelationship());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    contact.setContactId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public List<TrustedContact> findByStudentId(int studentId) throws SQLException {
        List<TrustedContact> contacts = new ArrayList<>();
        String sql = "SELECT contact_id, student_id, contact_name, contact_phone, relationship, created_at " +
                     "FROM TRUSTED_CONTACTS WHERE student_id = ? ORDER BY contact_id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contacts.add(mapRow(rs));
                }
            }
        }
        return contacts;
    }

    public Optional<TrustedContact> findById(int contactId) throws SQLException {
        String sql = "SELECT contact_id, student_id, contact_name, contact_phone, relationship, created_at " +
                     "FROM TRUSTED_CONTACTS WHERE contact_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean delete(int contactId) throws SQLException {
        String sql = "DELETE FROM TRUSTED_CONTACTS WHERE contact_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contactId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<TrustedContact> findAll() throws SQLException {
        List<TrustedContact> contacts = new ArrayList<>();
        String sql = "SELECT contact_id, student_id, contact_name, contact_phone, relationship, created_at " +
                     "FROM TRUSTED_CONTACTS ORDER BY contact_id ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                contacts.add(mapRow(rs));
            }
        }
        return contacts;
    }

    private TrustedContact mapRow(ResultSet rs) throws SQLException {
        return new TrustedContact(
                rs.getInt("contact_id"),
                rs.getInt("student_id"),
                rs.getString("contact_name"),
                rs.getString("contact_phone"),
                rs.getString("relationship"),
                rs.getTimestamp("created_at")
        );
    }
}
