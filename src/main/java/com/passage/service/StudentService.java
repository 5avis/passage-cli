package com.passage.service;

import com.passage.dao.StudentDao;
import com.passage.dao.TrustedContactDao;
import com.passage.model.Student;
import com.passage.model.TrustedContact;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class StudentService {

    private final StudentDao studentDao = new StudentDao();
    private final TrustedContactDao contactDao = new TrustedContactDao();

    public Student registerStudent(String name, String email, String contactInfo) throws SQLException, IllegalArgumentException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address format.");
        }
        if (contactInfo == null || contactInfo.isBlank()) {
            throw new IllegalArgumentException("Contact info cannot be empty.");
        }

        Optional<Student> existing = studentDao.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Student with email '" + email + "' is already registered (ID: " + existing.get().getStudentId() + ").");
        }

        Student student = new Student(name.trim(), email.trim().toLowerCase(), contactInfo.trim());
        int id = studentDao.create(student);
        student.setStudentId(id);
        return student;
    }

    public List<Student> getAllStudents() throws SQLException {
        return studentDao.findAll();
    }

    public Optional<Student> getStudentById(int studentId) throws SQLException {
        return studentDao.findById(studentId);
    }

    public TrustedContact addTrustedContact(int studentId, String contactName, String contactPhone, String relationship) throws SQLException {
        if (!studentDao.exists(studentId)) {
            throw new IllegalArgumentException("Student ID " + studentId + " does not exist.");
        }
        if (contactName == null || contactName.isBlank()) {
            throw new IllegalArgumentException("Contact name cannot be empty.");
        }
        if (contactPhone == null || contactPhone.isBlank()) {
            throw new IllegalArgumentException("Contact phone cannot be empty.");
        }
        if (relationship == null || relationship.isBlank()) {
            throw new IllegalArgumentException("Relationship cannot be empty.");
        }

        TrustedContact contact = new TrustedContact(studentId, contactName.trim(), contactPhone.trim(), relationship.trim());
        int id = contactDao.create(contact);
        contact.setContactId(id);
        return contact;
    }

    public List<TrustedContact> getContactsForStudent(int studentId) throws SQLException {
        return contactDao.findByStudentId(studentId);
    }

    public boolean removeContact(int contactId) throws SQLException {
        return contactDao.delete(contactId);
    }

    public List<TrustedContact> getAllContacts() throws SQLException {
        return contactDao.findAll();
    }
}
