-- ============================================================================
-- SAMVAYA SOCIETY MANAGEMENT SYSTEM - DATABASE SCHEMA (DDL)
-- Compatible with MySQL 8.0+ and MySQL Workbench
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `samvaya` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `samvaya`;

-- Disable Foreign Key Checks during table creation
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Societies Table
DROP TABLE IF EXISTS `societies`;
CREATE TABLE `societies` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL,
    `registration_number` VARCHAR(100) UNIQUE NOT NULL,
    `address` TEXT NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `state` VARCHAR(100) NOT NULL,
    `pincode` VARCHAR(20) NOT NULL,
    `contact_email` VARCHAR(150),
    `contact_phone` VARCHAR(30),
    `total_wings` INT DEFAULT 1,
    `total_flats` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Roles Table
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) UNIQUE NOT NULL -- 'ADMIN', 'RESIDENT', 'SECURITY_GUARD'
) ENGINE=InnoDB;

-- 3. Users Table
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(80) UNIQUE NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(150) UNIQUE NOT NULL,
    `full_name` VARCHAR(150) NOT NULL,
    `phone` VARCHAR(30),
    `role_id` BIGINT NOT NULL,
    `is_active` BOOLEAN DEFAULT TRUE,
    `account_status` VARCHAR(30) DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE', 'OFFBOARDED'
    `resident_type` VARCHAR(30) NULL, -- 'OWNER', 'TENANT'
    `moved_out_at` TIMESTAMP NULL,
    `last_login` TIMESTAMP NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 4. Flats / Units Table
DROP TABLE IF EXISTS `flats`;
CREATE TABLE `flats` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `society_id` BIGINT NOT NULL DEFAULT 1,
    `wing` VARCHAR(20) NOT NULL,
    `flat_number` VARCHAR(30) NOT NULL,
    `floor_number` INT NOT NULL,
    `bhk_type` VARCHAR(20) DEFAULT '2BHK', -- '1BHK', '2BHK', '3BHK', '4BHK', 'PENTHOUSE'
    `flat_type` VARCHAR(20) DEFAULT '2BHK',
    `square_feet` DOUBLE DEFAULT 1000.0,
    `carpet_area_sq_ft` DOUBLE DEFAULT 900.0,
    `resident_id` BIGINT NULL,
    `current_resident_id` BIGINT NULL,
    `status` VARCHAR(30) DEFAULT 'OCCUPIED', -- 'OCCUPIED', 'VACANT', 'UNDER_MAINTENANCE'
    `occupancy_status` VARCHAR(30) DEFAULT 'OCCUPIED_OWNER', -- 'OCCUPIED_OWNER', 'OCCUPIED_TENANT', 'VACANT'
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_wing_flat` (`wing`, `flat_number`),
    CONSTRAINT `fk_flat_society` FOREIGN KEY (`society_id`) REFERENCES `societies` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Residents Table (Owner or Tenant)
DROP TABLE IF EXISTS `residents`;
CREATE TABLE `residents` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNIQUE NOT NULL,
    `flat_id` BIGINT NOT NULL,
    `resident_type` VARCHAR(30) NOT NULL DEFAULT 'OWNER', -- 'OWNER', 'TENANT'
    `emergency_contact_name` VARCHAR(150),
    `emergency_contact_phone` VARCHAR(30),
    `move_in_date` DATE NOT NULL,
    `move_out_date` DATE NULL,
    `status` VARCHAR(30) DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE', 'PENDING_APPROVAL'
    `account_status` VARCHAR(30) DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE', 'OFFBOARDED'
    `moved_out_at` TIMESTAMP NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_resident_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_resident_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 6. Family / Household Members Table
DROP TABLE IF EXISTS `household_members`;
CREATE TABLE `household_members` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `resident_id` BIGINT NOT NULL,
    `full_name` VARCHAR(150) NOT NULL,
    `relation` VARCHAR(50) NOT NULL, -- 'SPOUSE', 'CHILD', 'PARENT', 'SIBLING', 'OTHER'
    `phone` VARCHAR(30),
    `age` INT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_member_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Parking Slots Table
DROP TABLE IF EXISTS `parking_slots`;
CREATE TABLE `parking_slots` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `slot_number` VARCHAR(50) UNIQUE NOT NULL,
    `slot_type` VARCHAR(30) DEFAULT '4_WHEELER', -- '2_WHEELER', '4_WHEELER'
    `is_occupied` BOOLEAN NOT NULL DEFAULT FALSE,
    `basement_level` VARCHAR(20) DEFAULT 'B1',
    `flat_id` BIGINT NULL,
    `assigned_flat_id` BIGINT NULL,
    `status` VARCHAR(30) DEFAULT 'AVAILABLE', -- 'ASSIGNED', 'AVAILABLE', 'RESERVED'
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_parking_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_parking_assigned_flat` FOREIGN KEY (`assigned_flat_id`) REFERENCES `flats` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 8. Vehicles Table
DROP TABLE IF EXISTS `vehicles`;
CREATE TABLE `vehicles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `resident_id` BIGINT NOT NULL,
    `flat_id` BIGINT NOT NULL,
    `vehicle_number` VARCHAR(50) UNIQUE NOT NULL,
    `vehicle_type` VARCHAR(30) NOT NULL, -- 'CAR', 'BIKE', 'SCOOTER', 'OTHER'
    `make_model` VARCHAR(100),
    `parking_slot_id` BIGINT NULL,
    `status` VARCHAR(30) DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE'
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_vehicle_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_vehicle_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_vehicle_parking` FOREIGN KEY (`parking_slot_id`) REFERENCES `parking_slots` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 9. Domestic Staff Table
DROP TABLE IF EXISTS `domestic_staff`;
CREATE TABLE `domestic_staff` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `role_type` VARCHAR(50) NOT NULL, -- 'MAID', 'COOK', 'DRIVER', 'CLEANER', 'OTHER'
    `pass_code` VARCHAR(50) UNIQUE,
    `resident_id` BIGINT NOT NULL,
    `flat_id` BIGINT NOT NULL,
    `status` VARCHAR(30) DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE'
    `valid_until` DATE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_staff_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_staff_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Society Staff Management Table (Admin Managed)
DROP TABLE IF EXISTS `staff_members`;
CREATE TABLE `staff_members` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `emergency_contact` VARCHAR(30),
    `designation` VARCHAR(50) NOT NULL, -- 'GARDENER', 'CLEANER', 'TRASH_COLLECTOR', 'PLUMBER', 'ELECTRICIAN', 'MAINTENANCE_WORKER', 'SECURITY_GUARD', 'OTHER'
    `joining_date` DATE NOT NULL,
    `salary` DECIMAL(10,2) DEFAULT 0.00,
    `assigned_area` VARCHAR(100) DEFAULT 'All Wings',
    `status` VARCHAR(30) DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE', 'ON_LEAVE'
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 11. Staff Attendance Table (Security Recorded)
DROP TABLE IF EXISTS `staff_attendance`;
CREATE TABLE `staff_attendance` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `staff_member_id` BIGINT NOT NULL,
    `attendance_date` DATE NOT NULL,
    `status` VARCHAR(30) NOT NULL, -- 'PRESENT', 'ABSENT', 'LEAVE'
    `check_in_time` TIME NULL,
    `check_out_time` TIME NULL,
    `remarks` VARCHAR(255),
    `recorded_by_guard_id` BIGINT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_staff_date` (`staff_member_id`, `attendance_date`),
    CONSTRAINT `fk_att_staff` FOREIGN KEY (`staff_member_id`) REFERENCES `staff_members` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 12. Visitors Table
DROP TABLE IF EXISTS `visitors`;
CREATE TABLE `visitors` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `visitor_name` VARCHAR(150) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `flat_id` BIGINT NOT NULL,
    `resident_id` BIGINT NOT NULL,
    `purpose` VARCHAR(255) NOT NULL,
    `expected_date` DATE NOT NULL,
    `expected_time` TIME NOT NULL,
    `vehicle_number` VARCHAR(50),
    `number_of_visitors` INT DEFAULT 1,
    `total_guest_count` INT DEFAULT 1,
    `primary_guest_photo` LONGTEXT NULL,
    `pre_approved_by_resident_id` BIGINT NULL,
    `status` VARCHAR(30) DEFAULT 'EXPECTED', -- 'EXPECTED', 'ARRIVED', 'INSIDE', 'EXITED', 'CANCELLED'
    `approval_status` VARCHAR(30) DEFAULT 'APPROVED', -- 'PENDING', 'APPROVED', 'DENIED', 'PRE_APPROVED', 'VERIFIED_ENTRY', 'REJECTED'
    `pass_code` VARCHAR(50) UNIQUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_visitor_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_visitor_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 13. Visitor Entry/Exit Logs Table
DROP TABLE IF EXISTS `visitor_entry_exits`;
CREATE TABLE `visitor_entry_exits` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `visitor_id` BIGINT NOT NULL,
    `entry_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `exit_time` TIMESTAMP NULL,
    `guard_id` BIGINT NULL,
    `gate_number` VARCHAR(20) DEFAULT 'Main Gate 1',
    `verification_notes` VARCHAR(255),
    CONSTRAINT `fk_entry_visitor` FOREIGN KEY (`visitor_id`) REFERENCES `visitors` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 14. Deliveries Table
DROP TABLE IF EXISTS `deliveries`;
CREATE TABLE `deliveries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `company` VARCHAR(100) NOT NULL, -- 'Amazon', 'Flipkart', 'Swiggy', 'Zomato', 'Blinkit', 'Other'
    `delivery_person_name` VARCHAR(150),
    `phone` VARCHAR(30),
    `flat_id` BIGINT NOT NULL,
    `resident_id` BIGINT NOT NULL,
    `reference_number` VARCHAR(100),
    `vehicle_number` VARCHAR(50),
    `is_expected` BOOLEAN DEFAULT TRUE,
    `status` VARCHAR(30) DEFAULT 'EXPECTED', -- 'EXPECTED', 'ARRIVED', 'VERIFIED', 'COMPLETED', 'CANCELLED'
    `approval_status` VARCHAR(30) DEFAULT 'APPROVED', -- 'PENDING', 'APPROVED', 'DENIED'
    `arrived_at` TIMESTAMP NULL,
    `completed_at` TIMESTAMP NULL,
    `guard_id` BIGINT NULL,
    `notes` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_delivery_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_delivery_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 15. Temporary Workers Table
DROP TABLE IF EXISTS `temporary_workers`;
CREATE TABLE `temporary_workers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `company_name` VARCHAR(150),
    `purpose` VARCHAR(255) NOT NULL,
    `flat_id` BIGINT NULL,
    `area` VARCHAR(100),
    `entry_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `exit_time` TIMESTAMP NULL,
    `guard_id` BIGINT NULL,
    `status` VARCHAR(30) DEFAULT 'INSIDE', -- 'EXPECTED', 'INSIDE', 'EXITED'
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_worker_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 16. Complaints Table
DROP TABLE IF EXISTS `complaints`;
CREATE TABLE `complaints` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `resident_id` BIGINT NOT NULL,
    `flat_id` BIGINT NOT NULL,
    `category` VARCHAR(50) NOT NULL, -- 'PLUMBING', 'ELECTRICAL', 'CARPENTRY', 'SECURITY', 'NOISE', 'CLEANLINESS', 'PARKING', 'OTHER'
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT NOT NULL,
    `priority` VARCHAR(20) DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH', 'EMERGENCY'
    `status` VARCHAR(30) DEFAULT 'NEW', -- 'NEW', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'
    `assigned_staff_id` BIGINT NULL,
    `admin_remarks` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_complaint_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_complaint_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_complaint_staff` FOREIGN KEY (`assigned_staff_id`) REFERENCES `staff_members` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 17. Complaint History Table
DROP TABLE IF EXISTS `complaint_history`;
CREATE TABLE `complaint_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `complaint_id` BIGINT NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `remarks` TEXT,
    `changed_by_user_id` BIGINT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_ch_complaint` FOREIGN KEY (`complaint_id`) REFERENCES `complaints` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 18. Service Requests Table
DROP TABLE IF EXISTS `service_requests`;
CREATE TABLE `service_requests` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `resident_id` BIGINT NOT NULL,
    `flat_id` BIGINT NOT NULL,
    `service_type` VARCHAR(100) NOT NULL, -- 'PEST_CONTROL', 'DEEP_CLEANING', 'AC_SERVICE', 'ELECTRICIAN', 'PLUMBING'
    `preferred_date` DATE NOT NULL,
    `preferred_slot` VARCHAR(50) NOT NULL, -- '09:00 AM - 12:00 PM', '02:00 PM - 05:00 PM'
    `description` TEXT,
    `status` VARCHAR(30) DEFAULT 'REQUESTED', -- 'REQUESTED', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
    `assigned_staff_id` BIGINT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_sr_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_sr_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_sr_staff` FOREIGN KEY (`assigned_staff_id`) REFERENCES `staff_members` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 19. Amenities Table
DROP TABLE IF EXISTS `amenities`;
CREATE TABLE `amenities` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL, -- 'Clubhouse Lounge', 'Swimming Pool', 'Rooftop Garden', 'Gymnasium', 'Party Hall'
    `description` TEXT,
    `capacity` INT DEFAULT 50,
    `open_time` TIME DEFAULT '06:00:00',
    `close_time` TIME DEFAULT '22:00:00',
    `hourly_rate` DECIMAL(10,2) DEFAULT 0.00,
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 20. Amenity Bookings Table (With Conflict Prevention)
DROP TABLE IF EXISTS `amenity_bookings`;
CREATE TABLE `amenity_bookings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `amenity_id` BIGINT NOT NULL,
    `resident_id` BIGINT NOT NULL,
    `flat_id` BIGINT NOT NULL,
    `booking_date` DATE NOT NULL,
    `start_time` TIME NOT NULL,
    `end_time` TIME NOT NULL,
    `number_of_guests` INT DEFAULT 1,
    `total_amount` DECIMAL(10,2) DEFAULT 0.00,
    `status` VARCHAR(30) DEFAULT 'CONFIRMED', -- 'UPCOMING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_booking_amenity` FOREIGN KEY (`amenity_id`) REFERENCES `amenities` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_booking_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_booking_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 21. Maintenance Bills Table
DROP TABLE IF EXISTS `maintenance_bills`;
CREATE TABLE `maintenance_bills` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `flat_id` BIGINT NOT NULL,
    `resident_id` BIGINT NOT NULL,
    `bill_month` VARCHAR(20) NOT NULL, -- 'August 2026' or '2026-10'
    `flat_type` VARCHAR(20) DEFAULT '2BHK',
    `carpet_area_sq_ft` DOUBLE DEFAULT 900.0,
    `rate_per_sq_ft` DECIMAL(10,2) DEFAULT 3.50,
    `variable_area_charge` DECIMAL(10,2) DEFAULT 3150.00,
    `security_charge` DECIMAL(10,2) DEFAULT 1000.00,
    `lift_electricity_charge` DECIMAL(10,2) DEFAULT 800.00,
    `sinking_fund` DECIMAL(10,2) DEFAULT 500.00,
    `administrative_fee` DECIMAL(10,2) DEFAULT 200.00,
    `total_fixed_charges` DECIMAL(10,2) DEFAULT 2500.00,
    `maintenance_charge` DECIMAL(10,2) NOT NULL DEFAULT 3500.00,
    `water_charge` DECIMAL(10,2) DEFAULT 0.00,
    `parking_charge` DECIMAL(10,2) DEFAULT 0.00,
    `penalty_charge` DECIMAL(10,2) DEFAULT 0.00,
    `total_amount` DECIMAL(10,2) NOT NULL,
    `due_date` DATE NOT NULL,
    `status` VARCHAR(30) DEFAULT 'PENDING', -- 'PAID', 'PENDING', 'PARTIAL', 'OVERDUE'
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_bill_flat` FOREIGN KEY (`flat_id`) REFERENCES `flats` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bill_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 22. Payments Table
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `bill_id` BIGINT NOT NULL,
    `resident_id` BIGINT NOT NULL,
    `amount_paid` DECIMAL(10,2) NOT NULL,
    `payment_mode` VARCHAR(50) NOT NULL, -- 'UPI', 'NET_BANKING', 'CREDIT_CARD', 'CASH', 'CHEQUE'
    `transaction_reference` VARCHAR(100) UNIQUE NOT NULL,
    `payment_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(30) DEFAULT 'COMPLETED', -- 'COMPLETED', 'FAILED', 'PENDING'
    CONSTRAINT `fk_payment_bill` FOREIGN KEY (`bill_id`) REFERENCES `maintenance_bills` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_payment_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 23. Notices Table
DROP TABLE IF EXISTS `notices`;
CREATE TABLE `notices` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `category` VARCHAR(50) NOT NULL, -- 'GENERAL', 'MAINTENANCE', 'WATER_SUPPLY', 'EVENT', 'SECURITY', 'EMERGENCY', 'RULES', 'IMPORTANT', 'OTHER'
    `priority` VARCHAR(20) DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH', 'EMERGENCY'
    `published_date` DATE NOT NULL,
    `expiry_date` DATE NULL,
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_by_user_id` BIGINT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 24. Society Documents Table
DROP TABLE IF EXISTS `documents`;
CREATE TABLE `documents` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `category` VARCHAR(50) NOT NULL, -- 'BYLAWS', 'RULES', 'MEETING_MINUTES', 'FINANCIAL_REPORT', 'AUDIT', 'FORM', 'OTHER'
    `file_path` VARCHAR(255) NOT NULL,
    `file_size` VARCHAR(50),
    `description` TEXT,
    `visibility` VARCHAR(30) DEFAULT 'ALL', -- 'ALL', 'OWNERS_ONLY', 'ADMIN_ONLY'
    `uploaded_by_user_id` BIGINT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 25. Notifications Table
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `message` TEXT NOT NULL,
    `type` VARCHAR(50) NOT NULL, -- 'VISITOR', 'DELIVERY', 'COMPLAINT', 'SERVICE_REQUEST', 'PAYMENT', 'AMENITY', 'NOTICE', 'EMERGENCY', 'SYSTEM'
    `priority` VARCHAR(20) DEFAULT 'NORMAL', -- 'LOW', 'NORMAL', 'HIGH', 'URGENT'
    `is_read` BOOLEAN DEFAULT FALSE,
    `related_entity_id` BIGINT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 26. Emergency Alerts Table
DROP TABLE IF EXISTS `emergency_alerts`;
CREATE TABLE `emergency_alerts` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `category` VARCHAR(50) NOT NULL, -- 'FIRE', 'MEDICAL', 'SECURITY', 'WATER', 'POWER', 'MAINTENANCE', 'OTHER'
    `title` VARCHAR(200) NOT NULL,
    `message` TEXT NOT NULL,
    `location` VARCHAR(150) NOT NULL,
    `priority` VARCHAR(20) DEFAULT 'HIGH', -- 'HIGH', 'CRITICAL'
    `status` VARCHAR(30) DEFAULT 'ACTIVE', -- 'ACTIVE', 'RESOLVED', 'CLOSED'
    `issued_by_user_id` BIGINT NULL,
    `resolved_at` TIMESTAMP NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 27. Security Incidents Table
DROP TABLE IF EXISTS `incidents`;
CREATE TABLE `incidents` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `incident_type` VARCHAR(100) NOT NULL, -- 'Suspicious Activity', 'Theft', 'Property Damage', 'Unauthorized Entry', 'Fire Hazard', 'Water Leakage', 'Vehicle Incident', 'Other'
    `description` TEXT NOT NULL,
    `location` VARCHAR(150) NOT NULL,
    `priority` VARCHAR(20) DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    `status` VARCHAR(30) DEFAULT 'REPORTED', -- 'REPORTED', 'INVESTIGATING', 'RESOLVED', 'CLOSED'
    `reported_by_guard_id` BIGINT NULL,
    `related_flat_id` BIGINT NULL,
    `resolution_notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inc_flat` FOREIGN KEY (`related_flat_id`) REFERENCES `flats` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 28. Admin Activity Logs Table
DROP TABLE IF EXISTS `admin_activity_logs`;
CREATE TABLE `admin_activity_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `action` VARCHAR(50) NOT NULL, -- 'CREATED', 'UPDATED', 'DELETED', 'APPROVED', 'REJECTED', 'PUBLISHED', 'DEACTIVATED'
    `module` VARCHAR(50) NOT NULL, -- 'RESIDENTS', 'FLATS', 'STAFF', 'MAINTENANCE', 'COMPLAINTS', 'AMENITIES', 'NOTICES', 'USERS'
    `description` TEXT NOT NULL,
    `target_id` BIGINT NULL,
    `ip_address` VARCHAR(50),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- PERFORMANCE & SEARCH INDEXES
-- ============================================================================
CREATE INDEX `idx_user_username` ON `users` (`username`);
CREATE INDEX `idx_user_role` ON `users` (`role_id`);
CREATE INDEX `idx_flat_wing_num` ON `flats` (`wing`, `flat_number`);
CREATE INDEX `idx_resident_flat` ON `residents` (`flat_id`);
CREATE INDEX `idx_resident_type` ON `residents` (`resident_type`);
CREATE INDEX `idx_visitor_status` ON `visitors` (`status`);
CREATE INDEX `idx_visitor_flat` ON `visitors` (`flat_id`);
CREATE INDEX `idx_visitor_date` ON `visitors` (`expected_date`);
CREATE INDEX `idx_visitor_phone` ON `visitors` (`phone`);
CREATE INDEX `idx_delivery_status` ON `deliveries` (`status`);
CREATE INDEX `idx_delivery_flat` ON `deliveries` (`flat_id`);
CREATE INDEX `idx_vehicle_num` ON `vehicles` (`vehicle_number`);
CREATE INDEX `idx_complaint_status` ON `complaints` (`status`);
CREATE INDEX `idx_bill_status` ON `maintenance_bills` (`status`);
CREATE INDEX `idx_bill_flat` ON `maintenance_bills` (`flat_id`);
CREATE INDEX `idx_booking_amenity_date` ON `amenity_bookings` (`amenity_id`, `booking_date`);
CREATE INDEX `idx_incident_status` ON `incidents` (`status`);

SET FOREIGN_KEY_CHECKS = 1;
