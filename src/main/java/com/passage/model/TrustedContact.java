package com.passage.model;

import java.sql.Timestamp;

public class TrustedContact {
    private Integer contactId;
    private Integer studentId;
    private String contactName;
    private String contactPhone;
    private String relationship;
    private Timestamp createdAt;

    public TrustedContact() {}

    public TrustedContact(Integer contactId, Integer studentId, String contactName, String contactPhone, String relationship, Timestamp createdAt) {
        this.contactId = contactId;
        this.studentId = studentId;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.relationship = relationship;
        this.createdAt = createdAt;
    }

    public TrustedContact(Integer studentId, String contactName, String contactPhone, String relationship) {
        this.studentId = studentId;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.relationship = relationship;
    }

    public Integer getContactId() {
        return contactId;
    }

    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TrustedContact{" +
                "contactId=" + contactId +
                ", studentId=" + studentId +
                ", contactName='" + contactName + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }
}
