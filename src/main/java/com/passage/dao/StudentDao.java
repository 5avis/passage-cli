package com.passage.dao;

import com.passage.config.DBConnection;
import com.passage.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDao {

    private final DBConnection db = DBConnection.getInstance();

    public int create(Student student) throws SQLException {
        String sql = "INSERT INTO STUDENTS (student_name, email, contact_info) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getStudentName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getContactInfo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    student.setStudentId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Optional<Student> findById(int studentId) throws SQLException {
        String sql = "SELECT student_id, student_name, email, contact_info, created_at FROM STUDENTS WHERE student_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Student> findByEmail(String email) throws SQLException {
        String sql = "SELECT student_id, student_name, email, contact_info, created_at FROM STUDENTS WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, student_name, email, contact_info, created_at FROM STUDENTS ORDER BY student_id ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        }
        return students;
    }

    public boolean exists(int studentId) throws SQLException {
        String sql = "SELECT 1 FROM STUDENTS WHERE student_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("student_name"),
                rs.getString("email"),
                rs.getString("contact_info"),
                rs.getTimestamp("created_at")
        );
    }
}
