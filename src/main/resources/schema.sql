CREATE DATABASE IF NOT EXISTS campusrent_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campusrent_db;

CREATE TABLE IF NOT EXISTS campus (
                                      campus_id INT AUTO_INCREMENT PRIMARY KEY,
                                      campus_name VARCHAR(120) NOT NULL,
    city VARCHAR(80) NOT NULL,
    province VARCHAR(80) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(30),
    email VARCHAR(150)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS app_user (
                                        user_id INT AUTO_INCREMENT PRIMARY KEY,
                                        campus_id INT NOT NULL,
                                        first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30),
    password_hash VARCHAR(255) NOT NULL,
    user_role ENUM('STUDENT','STAFF','ADMIN')
    NOT NULL DEFAULT 'STUDENT',
    account_status ENUM('ACTIVE','INACTIVE')
    NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_campus
    FOREIGN KEY (campus_id)
    REFERENCES campus(campus_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS equipment (
                                         equipment_id INT AUTO_INCREMENT PRIMARY KEY,
                                         campus_id INT NOT NULL,
                                         serial_number VARCHAR(100) NOT NULL UNIQUE,
    equipment_name VARCHAR(150) NOT NULL,
    description TEXT,
    category ENUM(
                     'LAPTOP','PROJECTOR','CAMERA',
                     'TABLET','NETWORK_KIT','OTHER'
                 ) NOT NULL,
    daily_rate DECIMAL(10,2) NOT NULL,
    condition_status ENUM(
                             'NEW','GOOD','FAIR','DAMAGED'
                         ) NOT NULL DEFAULT 'GOOD',
    availability_status ENUM(
                                'AVAILABLE','BOOKED','MAINTENANCE','RETIRED'
                            ) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_equipment_campus
    FOREIGN KEY (campus_id)
    REFERENCES campus(campus_id),

    CONSTRAINT chk_equipment_rate
    CHECK (daily_rate >= 0)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS booking (
                                       booking_id INT AUTO_INCREMENT PRIMARY KEY,
                                       user_id INT NOT NULL,
                                       equipment_id INT NOT NULL,
                                       booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       start_date DATE NOT NULL,
                                       due_date DATE NOT NULL,
                                       actual_return_date DATE NULL,
                                       daily_rate DECIMAL(10,2) NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    booking_status ENUM(
                           'PENDING','APPROVED','ACTIVE',
                           'COMPLETED','CANCELLED','OVERDUE'
                       ) NOT NULL DEFAULT 'PENDING',
    notes TEXT,

    CONSTRAINT fk_booking_user
    FOREIGN KEY (user_id)
    REFERENCES app_user(user_id),

    CONSTRAINT fk_booking_equipment
    FOREIGN KEY (equipment_id)
    REFERENCES equipment(equipment_id),

    CONSTRAINT chk_booking_dates
    CHECK (due_date >= start_date),

    CONSTRAINT chk_booking_cost
    CHECK (daily_rate >= 0 AND total_cost >= 0),

    INDEX idx_booking_dates (
                                equipment_id, start_date, due_date
                            ),

    INDEX idx_booking_status (booking_status)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_log (
                                         log_id INT AUTO_INCREMENT PRIMARY KEY,
                                         user_id INT NULL,
                                         action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id INT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user
    FOREIGN KEY (user_id)
    REFERENCES app_user(user_id)
    ON DELETE SET NULL,

    INDEX idx_audit_created (created_at)
    ) ENGINE=InnoDB;