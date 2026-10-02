-- ============================================================================
-- SAMVAYA INFRASTRUCTURE PATCH: 48 FLATS & 114 STANDARDIZED PARKING SLOTS
-- Architecture: 3 Wings (A, B, C) × 4 Floors × 4 Flats = 48 Flats
-- Parking Allocation:
--   - 44 Car Slots ('4_WHEELER'): 34 Occupied/Assigned, 10 Available/Visitor
--   - 70 Bike Slots ('2_WHEELER'): 50 Occupied/Assigned, 20 Available/Guest
--   - Total Combined: 114 Slots (84 Occupied, 30 Available)
-- ============================================================================

USE `samvaya`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Ensure Society Info exists
INSERT INTO `societies` (`id`, `name`, `registration_number`, `address`, `city`, `state`, `pincode`, `contact_email`, `contact_phone`, `total_wings`, `total_flats`)
VALUES (1, 'Samvaya Luxury Enclave', 'REG-MH-2024-SAMVAYA-088', 'Plot 42, Palm Boulevard, Silicon Hills', 'Mumbai', 'Maharashtra', '400076', 'admin@samvaya.com', '+91 98765 43210', 3, 48)
ON DUPLICATE KEY UPDATE `total_flats` = 48;

-- 2. Ensure all 48 Flats exist (Wing A: 1-16, Wing B: 17-32, Wing C: 33-48)
INSERT INTO `flats` (`id`, `society_id`, `wing`, `flat_number`, `floor_number`, `bhk_type`, `flat_type`, `square_feet`, `carpet_area_sq_ft`, `status`, `occupancy_status`) VALUES
-- Wing A (Flats 1-16)
(1, 1, 'A', '101', 1, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(2, 1, 'A', '102', 1, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(3, 1, 'A', '103', 1, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(4, 1, 'A', '104', 1, '1BHK', '1BHK', 650.0, 650.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(5, 1, 'A', '201', 2, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(6, 1, 'A', '202', 2, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(7, 1, 'A', '203', 2, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(8, 1, 'A', '204', 2, '1BHK', '1BHK', 650.0, 650.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(9, 1, 'A', '301', 3, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(10, 1, 'A', '302', 3, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(11, 1, 'A', '303', 3, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(12, 1, 'A', '304', 3, '1BHK', '1BHK', 650.0, 650.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(13, 1, 'A', '401', 4, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(14, 1, 'A', '402', 4, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(15, 1, 'A', '403', 4, '2BHK', '2BHK', 950.0, 950.0, 'VACANT', 'VACANT'),
(16, 1, 'A', '404', 4, '1BHK', '1BHK', 650.0, 650.0, 'VACANT', 'VACANT'),

-- Wing B (Flats 17-32)
(17, 1, 'B', '101', 1, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(18, 1, 'B', '102', 1, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(19, 1, 'B', '103', 1, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(20, 1, 'B', '104', 1, '1BHK', '1BHK', 650.0, 650.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(21, 1, 'B', '201', 2, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(22, 1, 'B', '202', 2, '3BHK', '3BHK', 1500.0, 1450.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(23, 1, 'B', '203', 2, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(24, 1, 'B', '204', 2, '1BHK', '1BHK', 650.0, 650.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(25, 1, 'B', '301', 3, '4BHK', '4BHK', 2200.0, 2200.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(26, 1, 'B', '302', 3, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(27, 1, 'B', '303', 3, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(28, 1, 'B', '304', 3, '1BHK', '1BHK', 650.0, 650.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(29, 1, 'B', '401', 4, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(30, 1, 'B', '402', 4, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(31, 1, 'B', '403', 4, '2BHK', '2BHK', 950.0, 950.0, 'VACANT', 'VACANT'),
(32, 1, 'B', '404', 4, '1BHK', '1BHK', 650.0, 650.0, 'VACANT', 'VACANT'),

-- Wing C (Flats 33-48)
(33, 1, 'C', '101', 1, 'PENTHOUSE', 'PENTHOUSE', 3200.0, 3200.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(34, 1, 'C', '102', 1, '3BHK', '3BHK', 1600.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(35, 1, 'C', '103', 1, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(36, 1, 'C', '104', 1, '1BHK', '1BHK', 650.0, 650.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(37, 1, 'C', '201', 2, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(38, 1, 'C', '202', 2, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(39, 1, 'C', '203', 2, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(40, 1, 'C', '204', 2, '1BHK', '1BHK', 650.0, 650.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(41, 1, 'C', '301', 3, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(42, 1, 'C', '302', 3, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(43, 1, 'C', '303', 3, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_TENANT'),
(44, 1, 'C', '304', 3, '1BHK', '1BHK', 650.0, 650.0, 'VACANT', 'VACANT'),
(45, 1, 'C', '401', 4, '3BHK', '3BHK', 1450.0, 1450.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(46, 1, 'C', '402', 4, '2BHK', '2BHK', 950.0, 950.0, 'OCCUPIED', 'OCCUPIED_OWNER'),
(47, 1, 'C', '403', 4, '2BHK', '2BHK', 950.0, 950.0, 'VACANT', 'VACANT'),
(48, 1, 'C', '404', 4, '1BHK', '1BHK', 650.0, 650.0, 'VACANT', 'VACANT')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`), `occupancy_status` = VALUES(`occupancy_status`);

-- 3. Reset vehicle parking slot associations temporarily
UPDATE `vehicles` SET `parking_slot_id` = NULL;

-- 4. Clean and recreate parking_slots table data
DELETE FROM `parking_slots`;

-- 5. Insert 44 Car (4_WHEELER) Parking Slots (34 Occupied, 10 Available)
INSERT INTO `parking_slots` (`id`, `slot_number`, `slot_type`, `basement_level`, `is_occupied`, `flat_id`, `assigned_flat_id`, `status`) VALUES
-- Wing A Cars: C-A101 to C-A403 (15 bays)
(1, 'C-A101', '4_WHEELER', 'B1', TRUE, 1, 1, 'ASSIGNED'),
(2, 'C-A102', '4_WHEELER', 'B1', TRUE, 2, 2, 'ASSIGNED'),
(3, 'C-A103', '4_WHEELER', 'B1', TRUE, 3, 3, 'ASSIGNED'),
(4, 'C-A104', '4_WHEELER', 'B1', TRUE, 4, 4, 'ASSIGNED'),
(5, 'C-A201', '4_WHEELER', 'B1', TRUE, 5, 5, 'ASSIGNED'),
(6, 'C-A202', '4_WHEELER', 'B1', TRUE, 6, 6, 'ASSIGNED'),
(7, 'C-A203', '4_WHEELER', 'B1', TRUE, 7, 7, 'ASSIGNED'),
(8, 'C-A204', '4_WHEELER', 'B1', TRUE, 8, 8, 'ASSIGNED'),
(9, 'C-A301', '4_WHEELER', 'B1', TRUE, 9, 9, 'ASSIGNED'),
(10, 'C-A302', '4_WHEELER', 'B1', TRUE, 10, 10, 'ASSIGNED'),
(11, 'C-A303', '4_WHEELER', 'B1', TRUE, 11, 11, 'ASSIGNED'),
(12, 'C-A304', '4_WHEELER', 'B1', TRUE, 12, 12, 'ASSIGNED'),
(13, 'C-A401', '4_WHEELER', 'B1', FALSE, NULL, NULL, 'AVAILABLE'),
(14, 'C-A402', '4_WHEELER', 'B1', FALSE, NULL, NULL, 'AVAILABLE'),
(15, 'C-A403', '4_WHEELER', 'B1', FALSE, NULL, NULL, 'AVAILABLE'),

-- Wing B Cars: C-B101 to C-B403 (15 bays)
(16, 'C-B101', '4_WHEELER', 'B2', TRUE, 17, 17, 'ASSIGNED'),
(17, 'C-B102', '4_WHEELER', 'B2', TRUE, 18, 18, 'ASSIGNED'),
(18, 'C-B103', '4_WHEELER', 'B2', TRUE, 19, 19, 'ASSIGNED'),
(19, 'C-B104', '4_WHEELER', 'B2', TRUE, 20, 20, 'ASSIGNED'),
(20, 'C-B201', '4_WHEELER', 'B2', TRUE, 21, 21, 'ASSIGNED'),
(21, 'C-B202', '4_WHEELER', 'B2', TRUE, 22, 22, 'ASSIGNED'),
(22, 'C-B203', '4_WHEELER', 'B2', TRUE, 23, 23, 'ASSIGNED'),
(23, 'C-B204', '4_WHEELER', 'B2', TRUE, 24, 24, 'ASSIGNED'),
(24, 'C-B301', '4_WHEELER', 'B2', TRUE, 25, 25, 'ASSIGNED'),
(25, 'C-B302', '4_WHEELER', 'B2', TRUE, 26, 26, 'ASSIGNED'),
(26, 'C-B303', '4_WHEELER', 'B2', TRUE, 27, 27, 'ASSIGNED'),
(27, 'C-B304', '4_WHEELER', 'B2', TRUE, 28, 28, 'ASSIGNED'),
(28, 'C-B401', '4_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(29, 'C-B402', '4_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(30, 'C-B403', '4_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),

-- Wing C Cars: C-C101 to C-C402 (14 bays)
(31, 'C-C101', '4_WHEELER', 'Ground', TRUE, 33, 33, 'ASSIGNED'),
(32, 'C-C102', '4_WHEELER', 'Ground', TRUE, 34, 34, 'ASSIGNED'),
(33, 'C-C103', '4_WHEELER', 'Ground', TRUE, 35, 35, 'ASSIGNED'),
(34, 'C-C104', '4_WHEELER', 'Ground', TRUE, 36, 36, 'ASSIGNED'),
(35, 'C-C201', '4_WHEELER', 'Ground', TRUE, 37, 37, 'ASSIGNED'),
(36, 'C-C202', '4_WHEELER', 'Ground', TRUE, 38, 38, 'ASSIGNED'),
(37, 'C-C203', '4_WHEELER', 'Ground', TRUE, 39, 39, 'ASSIGNED'),
(38, 'C-C204', '4_WHEELER', 'Ground', TRUE, 40, 40, 'ASSIGNED'),
(39, 'C-C301', '4_WHEELER', 'Ground', TRUE, 41, 41, 'ASSIGNED'),
(40, 'C-C302', '4_WHEELER', 'Ground', TRUE, 42, 42, 'ASSIGNED'),
(41, 'C-C303', '4_WHEELER', 'Ground', FALSE, NULL, NULL, 'AVAILABLE'),
(42, 'C-C304', '4_WHEELER', 'Ground', FALSE, NULL, NULL, 'AVAILABLE'),
(43, 'C-C401', '4_WHEELER', 'Ground', FALSE, NULL, NULL, 'AVAILABLE'),
(44, 'C-C402', '4_WHEELER', 'Ground', FALSE, NULL, NULL, 'AVAILABLE');

-- 6. Insert 70 Bike (2_WHEELER) Parking Slots: B-01 to B-70 (50 Occupied, 20 Available)
INSERT INTO `parking_slots` (`id`, `slot_number`, `slot_type`, `basement_level`, `is_occupied`, `flat_id`, `assigned_flat_id`, `status`) VALUES
-- Occupied Bikes: B-01 to B-50 (50 bays assigned to society units)
(45, 'B-01', '2_WHEELER', 'B1', TRUE, 1, 1, 'ASSIGNED'),
(46, 'B-02', '2_WHEELER', 'B1', TRUE, 2, 2, 'ASSIGNED'),
(47, 'B-03', '2_WHEELER', 'B1', TRUE, 3, 3, 'ASSIGNED'),
(48, 'B-04', '2_WHEELER', 'B1', TRUE, 4, 4, 'ASSIGNED'),
(49, 'B-05', '2_WHEELER', 'B1', TRUE, 5, 5, 'ASSIGNED'),
(50, 'B-06', '2_WHEELER', 'B1', TRUE, 6, 6, 'ASSIGNED'),
(51, 'B-07', '2_WHEELER', 'B1', TRUE, 7, 7, 'ASSIGNED'),
(52, 'B-08', '2_WHEELER', 'B1', TRUE, 8, 8, 'ASSIGNED'),
(53, 'B-09', '2_WHEELER', 'B1', TRUE, 9, 9, 'ASSIGNED'),
(54, 'B-10', '2_WHEELER', 'B1', TRUE, 10, 10, 'ASSIGNED'),
(55, 'B-11', '2_WHEELER', 'B1', TRUE, 11, 11, 'ASSIGNED'),
(56, 'B-12', '2_WHEELER', 'B1', TRUE, 12, 12, 'ASSIGNED'),
(57, 'B-13', '2_WHEELER', 'B1', TRUE, 13, 13, 'ASSIGNED'),
(58, 'B-14', '2_WHEELER', 'B1', TRUE, 14, 14, 'ASSIGNED'),
(59, 'B-15', '2_WHEELER', 'B1', TRUE, 15, 15, 'ASSIGNED'),
(60, 'B-16', '2_WHEELER', 'B1', TRUE, 16, 16, 'ASSIGNED'),
(61, 'B-17', '2_WHEELER', 'B1', TRUE, 17, 17, 'ASSIGNED'),
(62, 'B-18', '2_WHEELER', 'B1', TRUE, 18, 18, 'ASSIGNED'),
(63, 'B-19', '2_WHEELER', 'B1', TRUE, 19, 19, 'ASSIGNED'),
(64, 'B-20', '2_WHEELER', 'B1', TRUE, 20, 20, 'ASSIGNED'),
(65, 'B-21', '2_WHEELER', 'B1', TRUE, 21, 21, 'ASSIGNED'),
(66, 'B-22', '2_WHEELER', 'B1', TRUE, 22, 22, 'ASSIGNED'),
(67, 'B-23', '2_WHEELER', 'B1', TRUE, 23, 23, 'ASSIGNED'),
(68, 'B-24', '2_WHEELER', 'B1', TRUE, 24, 24, 'ASSIGNED'),
(69, 'B-25', '2_WHEELER', 'B1', TRUE, 25, 25, 'ASSIGNED'),
(70, 'B-26', '2_WHEELER', 'B2', TRUE, 26, 26, 'ASSIGNED'),
(71, 'B-27', '2_WHEELER', 'B2', TRUE, 27, 27, 'ASSIGNED'),
(72, 'B-28', '2_WHEELER', 'B2', TRUE, 28, 28, 'ASSIGNED'),
(73, 'B-29', '2_WHEELER', 'B2', TRUE, 29, 29, 'ASSIGNED'),
(74, 'B-30', '2_WHEELER', 'B2', TRUE, 30, 30, 'ASSIGNED'),
(75, 'B-31', '2_WHEELER', 'B2', TRUE, 31, 31, 'ASSIGNED'),
(76, 'B-32', '2_WHEELER', 'B2', TRUE, 32, 32, 'ASSIGNED'),
(77, 'B-33', '2_WHEELER', 'B2', TRUE, 33, 33, 'ASSIGNED'),
(78, 'B-34', '2_WHEELER', 'B2', TRUE, 34, 34, 'ASSIGNED'),
(79, 'B-35', '2_WHEELER', 'B2', TRUE, 35, 35, 'ASSIGNED'),
(80, 'B-36', '2_WHEELER', 'B2', TRUE, 36, 36, 'ASSIGNED'),
(81, 'B-37', '2_WHEELER', 'B2', TRUE, 37, 37, 'ASSIGNED'),
(82, 'B-38', '2_WHEELER', 'B2', TRUE, 38, 38, 'ASSIGNED'),
(83, 'B-39', '2_WHEELER', 'B2', TRUE, 39, 39, 'ASSIGNED'),
(84, 'B-40', '2_WHEELER', 'B2', TRUE, 40, 40, 'ASSIGNED'),
(85, 'B-41', '2_WHEELER', 'B2', TRUE, 41, 41, 'ASSIGNED'),
(86, 'B-42', '2_WHEELER', 'B2', TRUE, 42, 42, 'ASSIGNED'),
(87, 'B-43', '2_WHEELER', 'B2', TRUE, 43, 43, 'ASSIGNED'),
(88, 'B-44', '2_WHEELER', 'B2', TRUE, 44, 44, 'ASSIGNED'),
(89, 'B-45', '2_WHEELER', 'B2', TRUE, 45, 45, 'ASSIGNED'),
(90, 'B-46', '2_WHEELER', 'B2', TRUE, 46, 46, 'ASSIGNED'),
(91, 'B-47', '2_WHEELER', 'B2', TRUE, 47, 47, 'ASSIGNED'),
(92, 'B-48', '2_WHEELER', 'B2', TRUE, 48, 48, 'ASSIGNED'),
(93, 'B-49', '2_WHEELER', 'B2', TRUE, 1, 1, 'ASSIGNED'),
(94, 'B-50', '2_WHEELER', 'B2', TRUE, 2, 2, 'ASSIGNED'),

-- Available Bikes: B-51 to B-70 (20 bays available for guests / visitors)
(95, 'B-51', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(96, 'B-52', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(97, 'B-53', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(98, 'B-54', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(99, 'B-55', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(100, 'B-56', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(101, 'B-57', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(102, 'B-58', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(103, 'B-59', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(104, 'B-60', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(105, 'B-61', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(106, 'B-62', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(107, 'B-63', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(108, 'B-64', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(109, 'B-65', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(110, 'B-66', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(111, 'B-67', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(112, 'B-68', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(113, 'B-69', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE'),
(114, 'B-70', '2_WHEELER', 'B2', FALSE, NULL, NULL, 'AVAILABLE');

-- 7. Re-associate Sample Vehicles
UPDATE `vehicles` SET `parking_slot_id` = 1 WHERE `id` = 1;
UPDATE `vehicles` SET `parking_slot_id` = 45 WHERE `id` = 2;
UPDATE `vehicles` SET `parking_slot_id` = 22 WHERE `id` = 3;
UPDATE `vehicles` SET `parking_slot_id` = 4 WHERE `id` = 4;

SET FOREIGN_KEY_CHECKS = 1;

-- Verification check query
SELECT 
    COUNT(*) AS total_slots,
    SUM(CASE WHEN is_occupied = TRUE THEN 1 ELSE 0 END) AS total_occupied,
    SUM(CASE WHEN is_occupied = FALSE THEN 1 ELSE 0 END) AS total_available,
    SUM(CASE WHEN slot_type IN ('4_WHEELER', 'FOUR_WHEELER') THEN 1 ELSE 0 END) AS total_cars,
    SUM(CASE WHEN slot_type IN ('4_WHEELER', 'FOUR_WHEELER') AND is_occupied = TRUE THEN 1 ELSE 0 END) AS cars_occupied,
    SUM(CASE WHEN slot_type IN ('4_WHEELER', 'FOUR_WHEELER') AND is_occupied = FALSE THEN 1 ELSE 0 END) AS cars_available,
    SUM(CASE WHEN slot_type IN ('2_WHEELER', 'TWO_WHEELER') THEN 1 ELSE 0 END) AS total_bikes,
    SUM(CASE WHEN slot_type IN ('2_WHEELER', 'TWO_WHEELER') AND is_occupied = TRUE THEN 1 ELSE 0 END) AS bikes_occupied,
    SUM(CASE WHEN slot_type IN ('2_WHEELER', 'TWO_WHEELER') AND is_occupied = FALSE THEN 1 ELSE 0 END) AS bikes_available
FROM `parking_slots`;
