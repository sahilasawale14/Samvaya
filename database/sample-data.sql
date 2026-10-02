-- ============================================================================
-- SAMVAYA SOCIETY MANAGEMENT SYSTEM - SAMPLE SEED DATA
-- Default Logins:
-- 1. Admin:           username: admin    / password: password123 (or admin123)
-- 2. Resident Owner:  username: owner1   / password: password123 (or owner123)
-- 3. Resident Tenant: username: tenant1  / password: password123 (or tenant123)
-- 4. Security Guard:  username: guard1   / password: password123 (or guard123)
-- ============================================================================

USE `samvaya`;

-- 1. Insert Society Info
INSERT INTO `societies` (`id`, `name`, `registration_number`, `address`, `city`, `state`, `pincode`, `contact_email`, `contact_phone`, `total_wings`, `total_flats`)
VALUES (1, 'Samvaya Luxury Enclave', 'REG-MH-2024-SAMVAYA-088', 'Plot 42, Palm Boulevard, Silicon Hills', 'Mumbai', 'Maharashtra', '400076', 'admin@samvaya.com', '+91 98765 43210', 3, 36)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 2. Insert Roles (3 Core Roles)
INSERT INTO `roles` (`id`, `name`) VALUES
(1, 'ADMIN'),
(2, 'RESIDENT'),
(3, 'SECURITY_GUARD')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 3. Insert Default Users
-- Passwords stored in plain-text/compatible format for sample authentication (e.g. "admin123", "owner123", "tenant123", "guard123", "password123")
INSERT INTO `users` (`id`, `username`, `password`, `email`, `full_name`, `phone`, `role_id`, `is_active`, `account_status`, `resident_type`) VALUES
(1, 'admin', 'admin123', 'admin@samvaya.com', 'Vikramaditya Singhania', '+91 98200 11223', 1, TRUE, 'ACTIVE', NULL),
(2, 'owner1', 'owner123', 'rahul.sharma@samvaya.com', 'Rahul Sharma', '+91 98111 22334', 2, TRUE, 'ACTIVE', 'OWNER'),
(3, 'tenant1', 'tenant123', 'priya.patel@samvaya.com', 'Priya Patel', '+91 98333 44556', 2, TRUE, 'ACTIVE', 'TENANT'),
(4, 'guard1', 'guard123', 'ramesh.guard@samvaya.com', 'Ramesh Kumar (Gate 1)', '+91 97111 99887', 3, TRUE, 'ACTIVE', NULL),
(5, 'owner2', 'owner123', 'ananya.deshmukh@samvaya.com', 'Ananya Deshmukh', '+91 98444 55667', 2, TRUE, 'ACTIVE', 'OWNER'),
(6, 'guard2', 'guard123', 'suresh.guard@samvaya.com', 'Suresh Shinde (Gate 2)', '+91 97222 88776', 3, TRUE, 'ACTIVE', NULL)
ON DUPLICATE KEY UPDATE `username`=VALUES(`username`);

-- 4. Insert Flats (Wing A, B, C)
INSERT INTO `flats` (`id`, `society_id`, `wing`, `flat_number`, `floor_number`, `bhk_type`, `flat_type`, `square_feet`, `carpet_area_sq_ft`, `resident_id`, `status`, `occupancy_status`) VALUES
(1, 1, 'A', '101', 1, '3BHK', '3BHK', 1450.0, 1450.0, 1, 'OCCUPIED', 'OCCUPIED_OWNER'),
(2, 1, 'A', '102', 1, '2BHK', '2BHK', 900.0, 900.0, NULL, 'OCCUPIED', 'OCCUPIED_OWNER'),
(3, 1, 'A', '201', 2, '3BHK', '3BHK', 1450.0, 1450.0, NULL, 'VACANT', 'VACANT'),
(4, 1, 'A', '202', 2, '2BHK', '2BHK', 900.0, 900.0, 3, 'OCCUPIED', 'OCCUPIED_OWNER'),
(5, 1, 'B', '101', 1, '2BHK', '2BHK', 900.0, 900.0, NULL, 'OCCUPIED', 'OCCUPIED_OWNER'),
(6, 1, 'B', '202', 2, '3BHK', '3BHK', 1500.0, 1450.0, 2, 'OCCUPIED', 'OCCUPIED_TENANT'),
(7, 1, 'B', '301', 3, '4BHK', '4BHK', 2200.0, 2200.0, NULL, 'OCCUPIED', 'OCCUPIED_OWNER'),
(8, 1, 'B', '302', 3, '2BHK', '2BHK', 900.0, 900.0, NULL, 'VACANT', 'VACANT'),
(9, 1, 'C', '101', 1, 'PENTHOUSE', 'PENTHOUSE', 3200.0, 3200.0, NULL, 'OCCUPIED', 'OCCUPIED_OWNER'),
(10, 1, 'C', '102', 1, '3BHK', '3BHK', 1600.0, 1450.0, NULL, 'OCCUPIED', 'OCCUPIED_OWNER'),
(11, 1, 'A', '103', 1, '1BHK', '1BHK', 550.0, 550.0, NULL, 'OCCUPIED', 'OCCUPIED_OWNER')
ON DUPLICATE KEY UPDATE `flat_number`=VALUES(`flat_number`), `carpet_area_sq_ft`=VALUES(`carpet_area_sq_ft`), `resident_id`=VALUES(`resident_id`);

-- 5. Insert Residents (Owners and Tenants)
INSERT INTO `residents` (`id`, `user_id`, `flat_id`, `resident_type`, `emergency_contact_name`, `emergency_contact_phone`, `move_in_date`, `status`, `account_status`) VALUES
(1, 2, 1, 'OWNER', 'Sunita Sharma (Mother)', '+91 98111 22999', '2023-01-15', 'ACTIVE', 'ACTIVE'),
(2, 3, 6, 'TENANT', 'Kiran Patel (Father)', '+91 98333 44999', '2024-03-01', 'ACTIVE', 'ACTIVE'),
(3, 5, 4, 'OWNER', 'Rohan Deshmukh (Brother)', '+91 98444 55999', '2023-06-10', 'ACTIVE', 'ACTIVE')
ON DUPLICATE KEY UPDATE `resident_type`=VALUES(`resident_type`);

-- 6. Insert Household Members
INSERT INTO `household_members` (`id`, `resident_id`, `full_name`, `relation`, `phone`, `age`) VALUES
(1, 1, 'Neha Sharma', 'SPOUSE', '+91 98111 22335', 32),
(2, 1, 'Aarav Sharma', 'CHILD', NULL, 6),
(3, 2, 'Sameer Patel', 'SPOUSE', '+91 98333 44557', 29)
ON DUPLICATE KEY UPDATE `full_name`=VALUES(`full_name`);

-- 7. Insert Parking Slots (2-Wheeler and 4-Wheeler)
INSERT INTO `parking_slots` (`id`, `slot_number`, `slot_type`, `is_occupied`, `basement_level`, `flat_id`, `assigned_flat_id`, `status`) VALUES
(1, 'P-A101', '4_WHEELER', TRUE, 'B1', 1, 1, 'ASSIGNED'),
(2, 'P-A102', '4_WHEELER', TRUE, 'B1', 2, 2, 'ASSIGNED'),
(3, 'P-B202', '4_WHEELER', TRUE, 'B2', 6, 6, 'ASSIGNED'),
(4, 'P-A202', '4_WHEELER', TRUE, 'B1', 4, 4, 'ASSIGNED'),
(5, 'P-V01', '4_WHEELER', FALSE, 'Ground', NULL, NULL, 'AVAILABLE'),
(6, 'P-V02', '4_WHEELER', FALSE, 'Ground', NULL, NULL, 'AVAILABLE'),
(7, 'P-2W-01', '2_WHEELER', TRUE, 'B1', 1, 1, 'ASSIGNED'),
(8, 'P-2W-02', '2_WHEELER', FALSE, 'B1', NULL, NULL, 'AVAILABLE'),
(9, 'P-2W-03', '2_WHEELER', FALSE, 'B2', NULL, NULL, 'AVAILABLE'),
(10, 'P-2W-04', '2_WHEELER', FALSE, 'B2', NULL, NULL, 'AVAILABLE')
ON DUPLICATE KEY UPDATE `slot_number`=VALUES(`slot_number`), `is_occupied`=VALUES(`is_occupied`), `assigned_flat_id`=VALUES(`assigned_flat_id`);

-- 8. Insert Vehicles
INSERT INTO `vehicles` (`id`, `resident_id`, `flat_id`, `vehicle_number`, `vehicle_type`, `make_model`, `parking_slot_id`, `status`) VALUES
(1, 1, 1, 'MH-02-DN-7788', 'CAR', 'Honda City (White)', 1, 'ACTIVE'),
(2, 1, 1, 'MH-02-EQ-4421', 'SCOOTER', 'Ather 450X (Space Grey)', NULL, 'ACTIVE'),
(3, 2, 6, 'MH-03-BZ-1920', 'CAR', 'Hyundai Creta (Black)', 3, 'ACTIVE'),
(4, 3, 4, 'MH-02-CP-9009', 'CAR', 'Tata Nexon EV (Teal)', 4, 'ACTIVE')
ON DUPLICATE KEY UPDATE `vehicle_number`=VALUES(`vehicle_number`);

-- 9. Insert Domestic Staff
INSERT INTO `domestic_staff` (`id`, `name`, `phone`, `role_type`, `pass_code`, `resident_id`, `flat_id`, `status`, `valid_until`) VALUES
(1, 'Laxmi Bai', '+91 98920 12345', 'MAID', 'DOM-A101-01', 1, 1, 'ACTIVE', '2026-12-31'),
(2, 'Kailash Yadav', '+91 98921 54321', 'DRIVER', 'DOM-A101-02', 1, 1, 'ACTIVE', '2026-12-31'),
(3, 'Sunita Kamble', '+91 98922 67890', 'COOK', 'DOM-B202-01', 2, 6, 'ACTIVE', '2026-12-31')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 10. Insert Society Staff (Admin Managed)
INSERT INTO `staff_members` (`id`, `name`, `phone`, `emergency_contact`, `designation`, `joining_date`, `salary`, `assigned_area`, `status`) VALUES
(1, 'Santosh Jadhav', '+91 98670 11111', '+91 98670 99999', 'ELECTRICIAN', '2022-04-01', 28000.00, 'All Wings', 'ACTIVE'),
(2, 'Ganesh More', '+91 98670 22222', '+91 98670 88888', 'PLUMBER', '2022-05-15', 26000.00, 'All Wings', 'ACTIVE'),
(3, 'Munna Lal', '+91 98670 33333', '+91 98670 77777', 'GARDENER', '2023-01-10', 18000.00, 'Courtyard & Rooftop', 'ACTIVE'),
(4, 'Ramesh Kumar', '+91 97111 99887', '+91 97111 66666', 'SECURITY_GUARD', '2023-02-01', 22000.00, 'Main Gate 1', 'ACTIVE'),
(5, 'Suresh Shinde', '+91 97222 88776', '+91 97222 55555', 'SECURITY_GUARD', '2023-03-01', 22000.00, 'Gate 2 (Service)', 'ACTIVE')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 11. Insert Staff Attendance
INSERT INTO `staff_attendance` (`id`, `staff_member_id`, `attendance_date`, `status`, `check_in_time`, `check_out_time`, `remarks`, `recorded_by_guard_id`) VALUES
(1, 1, CURDATE(), 'PRESENT', '09:00:00', NULL, 'On duty - electrical room inspection', 4),
(2, 2, CURDATE(), 'PRESENT', '09:15:00', NULL, 'On duty - water pump station', 4),
(3, 3, CURDATE(), 'PRESENT', '08:30:00', NULL, 'Garden trimming Wing A', 4),
(4, 4, CURDATE(), 'PRESENT', '07:00:00', NULL, 'Shift A (07:00 - 15:00)', 4)
ON DUPLICATE KEY UPDATE `status`=VALUES(`status`);

-- 12. Insert Visitors
INSERT INTO `visitors` (`id`, `visitor_name`, `phone`, `flat_id`, `resident_id`, `purpose`, `expected_date`, `expected_time`, `vehicle_number`, `number_of_visitors`, `status`, `approval_status`, `pass_code`) VALUES
(1, 'Amitabh Verma', '+91 99887 76655', 1, 1, 'Family Dinner & Get-Together', CURDATE(), '19:30:00', 'MH-01-CR-1122', 2, 'EXPECTED', 'APPROVED', 'VIS-SAM-8891'),
(2, 'Rohit Kulkarni', '+91 98220 33445', 6, 2, 'Personal Meeting', CURDATE(), '16:00:00', NULL, 1, 'INSIDE', 'APPROVED', 'VIS-SAM-4420'),
(3, 'Dr. Aruna Sengupta', '+91 98700 55443', 4, 3, 'Medical Home Visit', CURDATE(), '11:00:00', 'MH-02-AA-9988', 1, 'EXITED', 'APPROVED', 'VIS-SAM-1102')
ON DUPLICATE KEY UPDATE `visitor_name`=VALUES(`visitor_name`);

-- 13. Insert Visitor Entry/Exit Logs
INSERT INTO `visitor_entry_exits` (`id`, `visitor_id`, `entry_time`, `exit_time`, `guard_id`, `gate_number`, `verification_notes`) VALUES
(1, 2, NOW() - INTERVAL 1 HOUR, NULL, 4, 'Main Gate 1', 'OTP verified with flat B-202 resident'),
(2, 3, NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 2 HOUR, 4, 'Main Gate 1', 'Doctor ID verified')
ON DUPLICATE KEY UPDATE `gate_number`=VALUES(`gate_number`);

-- 14. Insert Deliveries
INSERT INTO `deliveries` (`id`, `company`, `delivery_person_name`, `phone`, `flat_id`, `resident_id`, `reference_number`, `vehicle_number`, `is_expected`, `status`, `approval_status`, `arrived_at`, `completed_at`, `guard_id`, `notes`) VALUES
(1, 'Amazon', 'Deepak Singh', '+91 98190 77665', 1, 1, 'AMZ-IN-8899201', 'MH-02-DL-3344', TRUE, 'ARRIVED', 'APPROVED', NOW(), NULL, 4, 'Package placed at gate security counter'),
(2, 'Swiggy', 'Imran Khan', '+91 98200 44556', 6, 2, 'SWG-998271', 'MH-03-BK-1092', TRUE, 'COMPLETED', 'APPROVED', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 90 MINUTE, 4, 'Handed over directly to resident'),
(3, 'Blinkit', 'Rajesh Gupta', '+91 98330 22119', 4, 3, 'BLK-445511', 'MH-02-ER-7711', TRUE, 'EXPECTED', 'APPROVED', NULL, NULL, 4, 'Grocery order in transit')
ON DUPLICATE KEY UPDATE `company`=VALUES(`company`);

-- 15. Insert Temporary Workers
INSERT INTO `temporary_workers` (`id`, `name`, `phone`, `company_name`, `purpose`, `flat_id`, `area`, `entry_time`, `exit_time`, `guard_id`, `status`) VALUES
(1, 'Akash Carpentry Works', '+91 98110 55443', 'Urban Company', 'Wardrobe fitting & repair', 1, 'Flat A-101', NOW() - INTERVAL 3 HOUR, NULL, 4, 'INSIDE'),
(2, 'Sunil Painter', '+91 98220 88990', 'Asian Paints Service', 'Touchup paint work', 6, 'Flat B-202', NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 1 HOUR, 4, 'EXITED')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 16. Insert Complaints
INSERT INTO `complaints` (`id`, `resident_id`, `flat_id`, `category`, `title`, `description`, `priority`, `status`, `assigned_staff_id`, `admin_remarks`) VALUES
(1, 1, 1, 'PLUMBING', 'Low water pressure in master bathroom', 'Since yesterday evening the water pressure in the top shower is very low.', 'MEDIUM', 'IN_PROGRESS', 2, 'Assigned to plumber Ganesh. Scheduled visit today at 3:00 PM.'),
(2, 2, 6, 'ELECTRICAL', 'Corridor light flickering outside B-202', 'The ceiling LED panel outside our flat entrance is continuously flickering.', 'LOW', 'RESOLVED', 1, 'Replaced LED driver panel on 2nd floor corridor.'),
(3, 3, 4, 'PARKING', 'Unauthorized scooter parked in slot P-A202', 'A yellow scooter is parked in my assigned slot since morning.', 'HIGH', 'NEW', NULL, NULL)
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`);

-- 17. Insert Complaint History
INSERT INTO `complaint_history` (`id`, `complaint_id`, `status`, `remarks`, `changed_by_user_id`) VALUES
(1, 1, 'NEW', 'Complaint registered by resident Rahul Sharma', 2),
(2, 1, 'IN_PROGRESS', 'Admin assigned staff Ganesh More (Plumber)', 1),
(3, 2, 'NEW', 'Complaint registered by resident Priya Patel', 3),
(4, 2, 'RESOLVED', 'Issue resolved by Electrician Santosh Jadhav', 1)
ON DUPLICATE KEY UPDATE `status`=VALUES(`status`);

-- 18. Insert Service Requests
INSERT INTO `service_requests` (`id`, `resident_id`, `flat_id`, `service_type`, `preferred_date`, `preferred_slot`, `description`, `status`, `assigned_staff_id`) VALUES
(1, 1, 1, 'PEST_CONTROL', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00 AM - 01:00 PM', 'Quarterly herbal pest control for kitchen and balconies', 'ACCEPTED', 1),
(2, 2, 6, 'AC_SERVICE', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '02:00 PM - 05:00 PM', 'Master bedroom split AC filter cleanup and gas check', 'REQUESTED', NULL)
ON DUPLICATE KEY UPDATE `service_type`=VALUES(`service_type`);

-- 19. Insert Amenities (Clubhouse Lounge, Swimming Pool, Rooftop Garden, Gym, Party Hall)
INSERT INTO `amenities` (`id`, `name`, `description`, `capacity`, `open_time`, `close_time`, `hourly_rate`, `is_active`) VALUES
(1, 'Clubhouse Lounge', 'Air-conditioned luxury lounge with plush seating, high-speed WiFi, library, and billiards table.', 40, '07:00:00', '22:00:00', 500.00, TRUE),
(2, 'Swimming Pool Area', 'Temperature-controlled lap pool with dedicated adult deck, ambient evening lighting, and lounge chairs.', 30, '06:00:00', '21:00:00', 0.00, TRUE),
(3, 'Rooftop Garden & Deck', 'Panoramic sky deck with landscaped walking tracks, pergolas, sunset view pods, and barbecue stations.', 60, '06:00:00', '23:00:00', 1000.00, TRUE),
(4, 'Fitness Gymnasium', 'Fully equipped gym with modern cardio treadmills, cross-trainers, free weights, and yoga studio.', 25, '05:30:00', '22:30:00', 0.00, TRUE),
(5, 'Grand Community Banquet Hall', 'Soundproof banquet hall with banquet stage, audio-visual projection, and catering pantry.', 150, '09:00:00', '23:30:00', 3500.00, TRUE)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 20. Insert Amenity Bookings
INSERT INTO `amenity_bookings` (`id`, `amenity_id`, `resident_id`, `flat_id`, `booking_date`, `start_time`, `end_time`, `number_of_guests`, `total_amount`, `status`) VALUES
(1, 1, 1, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '18:00:00', '21:00:00', 15, 1500.00, 'CONFIRMED'),
(2, 3, 2, 6, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '17:00:00', '20:00:00', 20, 3000.00, 'CONFIRMED'),
(3, 2, 3, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00', '08:30:00', 2, 0.00, 'CONFIRMED')
ON DUPLICATE KEY UPDATE `booking_date`=VALUES(`booking_date`);

-- 21. Insert Maintenance Bills (Itemized Fixed vs Variable Area Charges)
-- Flat 1: 3 BHK (1450 sqft) -> Var = 3.50 * 1450 = 5075.00, Fixed = 2500.00, Total = 7575.00
-- Flat 6: 3 BHK (1450 sqft) -> Var = 3.50 * 1450 = 5075.00, Fixed = 2500.00, Total = 7575.00
-- Flat 4: 2 BHK (900 sqft)  -> Var = 3.50 * 900  = 3150.00, Fixed = 2500.00, Total = 5650.00
-- Flat 11: 1 BHK (550 sqft) -> Var = 3.50 * 550  = 1925.00, Fixed = 2500.00, Total = 4425.00
INSERT INTO `maintenance_bills` (`id`, `flat_id`, `resident_id`, `bill_month`, `flat_type`, `carpet_area_sq_ft`, `rate_per_sq_ft`, `variable_area_charge`, `security_charge`, `lift_electricity_charge`, `sinking_fund`, `administrative_fee`, `total_fixed_charges`, `maintenance_charge`, `water_charge`, `parking_charge`, `penalty_charge`, `total_amount`, `due_date`, `status`) VALUES
(1, 1, 1, 'August 2026', '3BHK', 1450.0, 3.50, 5075.00, 1000.00, 800.00, 500.00, 200.00, 2500.00, 5075.00, 0.00, 0.00, 0.00, 7575.00, '2026-08-31', 'PAID'),
(2, 6, 2, 'August 2026', '3BHK', 1450.0, 3.50, 5075.00, 1000.00, 800.00, 500.00, 200.00, 2500.00, 5075.00, 0.00, 0.00, 0.00, 7575.00, '2026-08-31', 'PENDING'),
(3, 4, 3, 'August 2026', '2BHK', 900.0,  3.50, 3150.00, 1000.00, 800.00, 500.00, 200.00, 2500.00, 3150.00, 0.00, 0.00, 0.00, 5650.00, '2026-08-31', 'PENDING'),
(4, 11, 1, 'August 2026', '1BHK', 550.0,  3.50, 1925.00, 1000.00, 800.00, 500.00, 200.00, 2500.00, 1925.00, 0.00, 0.00, 0.00, 4425.00, '2026-08-31', 'PENDING')
ON DUPLICATE KEY UPDATE `bill_month`=VALUES(`bill_month`), `total_amount`=VALUES(`total_amount`);

-- 22. Insert Payments
INSERT INTO `payments` (`id`, `bill_id`, `resident_id`, `amount_paid`, `payment_mode`, `transaction_reference`, `payment_date`, `status`) VALUES
(1, 1, 1, 4500.00, 'UPI', 'UPI/20260810/SAMVAYA/998124', '2026-08-10 14:30:00', 'COMPLETED')
ON DUPLICATE KEY UPDATE `transaction_reference`=VALUES(`transaction_reference`);

-- 23. Insert Notices
INSERT INTO `notices` (`id`, `title`, `content`, `category`, `priority`, `published_date`, `expiry_date`, `is_active`, `created_by_user_id`) VALUES
(1, 'Scheduled Water Tank Cleaning & Maintenance', 'Please be informed that the overhead and underground water reservoirs for Wings A, B, and C will undergo annual cleaning on Sunday from 9:00 AM to 2:00 PM. Water supply will remain suspended during this window.', 'WATER_SUPPLY', 'HIGH', '2026-08-25', '2026-09-02', TRUE, 1),
(2, 'Annual General Meeting (AGM) 2026 Announcement', 'The Annual General Body Meeting of Samvaya Luxury Enclave will be held on September 14, 2026 at 10:30 AM in the Grand Community Banquet Hall. Key agenda includes audited financials, solar rooftop adoption, and security upgrade.', 'GENERAL', 'MEDIUM', '2026-08-20', '2026-09-15', TRUE, 1),
(3, 'EV Charging Station Expansion in Basement B1', 'Installation of 8 additional Level-2 AC smart EV charging points in Basement B1 is complete. Residents wishing to activate app-based charging can register their vehicle at the society management office.', 'MAINTENANCE', 'LOW', '2026-08-15', '2026-09-30', TRUE, 1)
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`);

-- 24. Insert Society Documents
INSERT INTO `documents` (`id`, `title`, `category`, `file_path`, `file_size`, `description`, `visibility`, `uploaded_by_user_id`) VALUES
(1, 'Samvaya Society Bylaws & Constitution (2024 Edition)', 'BYLAWS', '/documents/samvaya_bylaws_2024.pdf', '2.4 MB', 'Complete registered rules, voting procedures, and resident guidelines.', 'ALL', 1),
(2, 'Clubhouse & Amenity Usage Guidelines & Timings', 'RULES', '/documents/amenity_guidelines.pdf', '850 KB', 'Operating hours, booking cancellation rules, and guest dress codes.', 'ALL', 1),
(3, 'Audited Financial Statement FY 2025-26', 'FINANCIAL_REPORT', '/documents/financial_statement_2025_26.pdf', '4.1 MB', 'Annual audit report prepared by M/s Deshpande & Associates Chartered Accountants.', 'ALL', 1)
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`);

-- 25. Insert Notifications
INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `type`, `priority`, `is_read`, `related_entity_id`) VALUES
(1, 2, 'Visitor Pre-Approval Confirmed', 'Your visitor pass for Amitabh Verma has been generated and approved for today.', 'VISITOR', 'NORMAL', FALSE, 1),
(2, 2, 'Amazon Delivery at Gate', 'Delivery person Deepak Singh has arrived with package AMZ-IN-8899201.', 'DELIVERY', 'NORMAL', FALSE, 1),
(3, 3, 'Maintenance Due Reminder', 'Maintenance bill of Rs. 4,500 for August 2026 is due on 31st August.', 'PAYMENT', 'HIGH', FALSE, 2)
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`);

-- 26. Insert Security Incidents
INSERT INTO `incidents` (`id`, `incident_type`, `description`, `location`, `priority`, `status`, `reported_by_guard_id`, `related_flat_id`, `resolution_notes`) VALUES
(1, 'Unauthorized Entry Attempt', 'An unknown individual attempted to enter Wing B claiming to be a courier without order reference. Stopped at Gate 1 and entry denied.', 'Main Gate 1', 'HIGH', 'RESOLVED', 4, 6, 'Person turned back. Guard logged details and CCTV footage tagged.'),
(2, 'Water Leakage in Basement B2', 'Minor pipe leakage noticed near parking slot P-B202.', 'Basement B2', 'MEDIUM', 'INVESTIGATING', 4, NULL, 'Informed plumber Ganesh for immediate inspection.')
ON DUPLICATE KEY UPDATE `incident_type`=VALUES(`incident_type`);

-- 27. Insert Admin Activity Logs
INSERT INTO `admin_activity_logs` (`id`, `user_id`, `action`, `module`, `description`, `target_id`, `ip_address`) VALUES
(1, 1, 'PUBLISHED', 'NOTICES', 'Published notice: Scheduled Water Tank Cleaning & Maintenance', 1, '127.0.0.1'),
(2, 1, 'CREATED', 'RESIDENTS', 'Added resident profile for Rahul Sharma (Flat A-101)', 1, '127.0.0.1'),
(3, 1, 'UPDATED', 'COMPLAINTS', 'Updated complaint #1 status to IN_PROGRESS and assigned plumber', 1, '127.0.0.1')
ON DUPLICATE KEY UPDATE `action`=VALUES(`action`);
