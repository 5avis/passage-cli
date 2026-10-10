-- =====================================================================
-- PASSAGE CLI: College Travel Safety Platform Database Schema
-- Database: ridesafe
-- =====================================================================

CREATE DATABASE IF NOT EXISTS ridesafe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ridesafe;

-- 1. STUDENTS TABLE
CREATE TABLE IF NOT EXISTS STUDENTS (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contact_info VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. TRUSTED CONTACTS TABLE
CREATE TABLE IF NOT EXISTS TRUSTED_CONTACTS (
    contact_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trusted_contacts_student
        FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. JOURNEYS TABLE
CREATE TABLE IF NOT EXISTS JOURNEYS (
    journey_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    source_location VARCHAR(150) NOT NULL,
    destination VARCHAR(150) NOT NULL,
    departure_time DATETIME NOT NULL,
    expected_arrival_time DATETIME NOT NULL,
    actual_arrival_time DATETIME NULL,
    journey_status ENUM('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_journeys_student
        FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. JOURNEY UPDATES TABLE
CREATE TABLE IF NOT EXISTS JOURNEY_UPDATES (
    update_id INT AUTO_INCREMENT PRIMARY KEY,
    journey_id INT NOT NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    current_status VARCHAR(50) NOT NULL,
    remarks TEXT,
    CONSTRAINT fk_journey_updates_journey
        FOREIGN KEY (journey_id) REFERENCES JOURNEYS(journey_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. SAFETY ALERTS TABLE
CREATE TABLE IF NOT EXISTS SAFETY_ALERTS (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    journey_id INT NOT NULL,
    contact_id INT NOT NULL,
    alert_type ENUM('DELAY_ALERT', 'JOURNEY_COMPLETION') NOT NULL,
    alert_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    alert_status ENUM('PENDING', 'SENT', 'ACKNOWLEDGED') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_safety_alerts_journey
        FOREIGN KEY (journey_id) REFERENCES JOURNEYS(journey_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_safety_alerts_contact
        FOREIGN KEY (contact_id) REFERENCES TRUSTED_CONTACTS(contact_id)
        ON DELETE CASCADE,
    -- Prevent duplicate alert triggers for same journey, contact, and type
    UNIQUE KEY uq_journey_contact_type (journey_id, contact_id, alert_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- SEED DATA: 3 Comprehensive Student Profiles & Lifecycle Scenarios
-- =====================================================================

-- Clean existing seed data cleanly if present (safe reload)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE SAFETY_ALERTS;
TRUNCATE TABLE JOURNEY_UPDATES;
TRUNCATE TABLE JOURNEYS;
TRUNCATE TABLE TRUSTED_CONTACTS;
TRUNCATE TABLE STUDENTS;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- Set 1: Priya Sharma (Completed Journey + Active Overdue Trip for Watchdog)
-- ---------------------------------------------------------------------
INSERT INTO STUDENTS (student_id, student_name, email, contact_info) VALUES
(1, 'Priya Sharma', 'priya.sharma@university.edu', '+91-9876543210');

INSERT INTO TRUSTED_CONTACTS (contact_id, student_id, contact_name, contact_phone, relationship) VALUES
(1, 1, 'Rajesh Sharma', '+91-9876543211', 'Father'),
(2, 1, 'Anita Sharma', '+91-9876543212', 'Mother');

-- Trip 1: Completed Journey
INSERT INTO JOURNEYS (journey_id, student_id, source_location, destination, departure_time, expected_arrival_time, actual_arrival_time, journey_status) VALUES
(1, 1, 'North Campus Dormitory', 'Central Railway Station', '2026-10-09 08:00:00', '2026-10-09 09:15:00', '2026-10-09 09:12:30', 'COMPLETED');

INSERT INTO JOURNEY_UPDATES (update_id, journey_id, update_time, current_status, remarks) VALUES
(1, 1, '2026-10-09 08:05:00', 'DEPARTED', 'Boarded University Express Cab #402'),
(2, 1, '2026-10-09 08:40:00', 'IN_TRANSIT', 'Passing Ring Road Flyover, traffic is normal'),
(3, 1, '2026-10-09 09:12:30', 'COMPLETED', 'Safely reached Platform 3');

INSERT INTO SAFETY_ALERTS (alert_id, journey_id, contact_id, alert_type, alert_time, alert_status) VALUES
(1, 1, 1, 'JOURNEY_COMPLETION', '2026-10-09 09:12:35', 'ACKNOWLEDGED'),
(2, 1, 2, 'JOURNEY_COMPLETION', '2026-10-09 09:12:35', 'SENT');

-- Trip 2: Overdue In-Progress Trip (Expected arrival 45 minutes ago)
INSERT INTO JOURNEYS (journey_id, student_id, source_location, destination, departure_time, expected_arrival_time, actual_arrival_time, journey_status) VALUES
(2, 1, 'Main Library', 'South Extension Residence', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 45 MINUTE), NULL, 'IN_PROGRESS');

INSERT INTO JOURNEY_UPDATES (update_id, journey_id, update_time, current_status, remarks) VALUES
(4, 2, DATE_SUB(NOW(), INTERVAL 115 MINUTE), 'STARTED', 'Left library basement parking via e-rickshaw'),
(5, 2, DATE_SUB(NOW(), INTERVAL 60 MINUTE), 'DELAYED', 'Severe roadblock and thunderstorm diversion near Outer Ring Road');

-- Pre-existing pending alert for one contact (the other will be discovered by watchdog)
INSERT INTO SAFETY_ALERTS (alert_id, journey_id, contact_id, alert_type, alert_time, alert_status) VALUES
(3, 2, 1, 'DELAY_ALERT', DATE_SUB(NOW(), INTERVAL 35 MINUTE), 'SENT');

-- ---------------------------------------------------------------------
-- Set 2: Rohan Patel (On-Schedule In-Progress + Planned Trip)
-- ---------------------------------------------------------------------
INSERT INTO STUDENTS (student_id, student_name, email, contact_info) VALUES
(2, 'Rohan Patel', 'rohan.patel@university.edu', '+91-9123456780');

INSERT INTO TRUSTED_CONTACTS (contact_id, student_id, contact_name, contact_phone, relationship) VALUES
(3, 2, 'Vikram Patel', '+91-9123456781', 'Brother'),
(4, 2, 'Dr. S. Rao', '+91-9123456782', 'Campus Guardian');

-- Trip 3: Currently In-Progress and On-Schedule (Departure 15 mins ago, arriving in 45 mins)
INSERT INTO JOURNEYS (journey_id, student_id, source_location, destination, departure_time, expected_arrival_time, actual_arrival_time, journey_status) VALUES
(3, 2, 'Engineering Block B', 'City Metro Interchange', DATE_SUB(NOW(), INTERVAL 15 MINUTE), DATE_ADD(NOW(), INTERVAL 45 MINUTE), NULL, 'IN_PROGRESS');

INSERT INTO JOURNEY_UPDATES (update_id, journey_id, update_time, current_status, remarks) VALUES
(6, 3, DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'BOARDED', 'Boarded Campus Shuttle Route 4, seat 12');

-- Trip 4: Planned Trip scheduled for tomorrow
INSERT INTO JOURNEYS (journey_id, student_id, source_location, destination, departure_time, expected_arrival_time, actual_arrival_time, journey_status) VALUES
(4, 2, 'University Main Gate', 'International Airport T3', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 26 HOUR), NULL, 'PLANNED');

INSERT INTO JOURNEY_UPDATES (update_id, journey_id, update_time, current_status, remarks) VALUES
(7, 4, NOW(), 'SCHEDULED', 'Pre-booked airport shuttle voucher #SH-9921');

-- ---------------------------------------------------------------------
-- Set 3: Ananya Iyer (Cancelled Journey + Completed Outstation Journey)
-- ---------------------------------------------------------------------
INSERT INTO STUDENTS (student_id, student_name, email, contact_info) VALUES
(3, 'Ananya Iyer', 'ananya.iyer@university.edu', '+91-9988776655');

INSERT INTO TRUSTED_CONTACTS (contact_id, student_id, contact_name, contact_phone, relationship) VALUES
(5, 3, 'Meera Iyer', '+91-9988776656', 'Mother'),
(6, 3, 'Aditi Sen', '+91-9988776657', 'Roommate');

-- Trip 5: Cancelled Journey
INSERT INTO JOURNEYS (journey_id, student_id, source_location, destination, departure_time, expected_arrival_time, actual_arrival_time, journey_status) VALUES
(5, 3, 'Girls Hostel 4', 'Downtown Tech Park', '2026-10-08 14:00:00', '2026-10-08 15:30:00', NULL, 'CANCELLED');

INSERT INTO JOURNEY_UPDATES (update_id, journey_id, update_time, current_status, remarks) VALUES
(8, 5, '2026-10-08 13:45:00', 'CANCELLED', 'Meeting postponed due to bad weather alert. Travel aborted safely.');

-- Trip 6: Completed Weekend Outing
INSERT INTO JOURNEYS (journey_id, student_id, source_location, destination, departure_time, expected_arrival_time, actual_arrival_time, journey_status) VALUES
(6, 3, 'Girls Hostel 4', 'National Museum & Cultural Center', '2026-10-07 10:00:00', '2026-10-07 11:30:00', '2026-10-07 11:25:00', 'COMPLETED');

INSERT INTO JOURNEY_UPDATES (update_id, journey_id, update_time, current_status, remarks) VALUES
(9, 6, '2026-10-07 10:05:00', 'DEPARTED', 'Joined group bus at Gate 2'),
(10, 6, '2026-10-07 11:25:00', 'COMPLETED', 'Arrived safely at Museum reception desk');

INSERT INTO SAFETY_ALERTS (alert_id, journey_id, contact_id, alert_type, alert_time, alert_status) VALUES
(4, 6, 5, 'JOURNEY_COMPLETION', '2026-10-07 11:25:10', 'ACKNOWLEDGED'),
(5, 6, 6, 'JOURNEY_COMPLETION', '2026-10-07 11:25:10', 'SENT');
