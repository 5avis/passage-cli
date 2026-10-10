package com.passage.model;

import java.sql.Timestamp;

public class Student {
    private Integer studentId;
    private String studentName;
    private String email;
    private String contactInfo;
    private Timestamp createdAt;

    public Student() {}

    public Student(Integer studentId, String studentName, String email, String contactInfo, Timestamp createdAt) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.contactInfo = contactInfo;
        this.createdAt = createdAt;
    }

    public Student(String studentName, String email, String contactInfo) {
        this.studentName = studentName;
        this.email = email;
        this.contactInfo = contactInfo;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", email='" + email + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                '}';
    }
}
