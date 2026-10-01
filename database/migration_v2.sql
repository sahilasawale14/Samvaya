-- ============================================================================
-- SAMVAYA HOUSING SOCIETY MANAGEMENT SYSTEM - DATABASE MIGRATION SCRIPT (V2)
-- ============================================================================
-- Purpose:
-- 1. Parking Slot Management (is_occupied, assigned_flat_id, slot_type normalization)
-- 2. Standard Maintenance Billing (flat_type, carpet_area_sq_ft, resident_id in flats)
-- 3. Itemized Fixed vs. Variable Billing Breakdown in maintenance_bills
-- ============================================================================

USE `samvaya`;

-- ----------------------------------------------------------------------------
-- 1. Update `parking_slots` Table
-- ----------------------------------------------------------------------------
-- Add is_occupied column if not present
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'parking_slots' 
      AND COLUMN_NAME = 'is_occupied'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `parking_slots` ADD COLUMN `is_occupied` BOOLEAN NOT NULL DEFAULT FALSE AFTER `slot_type`', 
    'SELECT "Column is_occupied already exists"'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add assigned_flat_id column if not present
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'parking_slots' 
      AND COLUMN_NAME = 'assigned_flat_id'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `parking_slots` ADD COLUMN `assigned_flat_id` BIGINT NULL AFTER `flat_id`, ADD CONSTRAINT `fk_parking_assigned_flat` FOREIGN KEY (`assigned_flat_id`) REFERENCES `flats` (`id`) ON DELETE SET NULL', 
    'SELECT "Column assigned_flat_id already exists"'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Sync data: Populate assigned_flat_id from existing flat_id
UPDATE `parking_slots` 
SET `assigned_flat_id` = `flat_id` 
WHERE `assigned_flat_id` IS NULL AND `flat_id` IS NOT NULL;

-- Normalize slot_type to '2_WHEELER' and '4_WHEELER'
UPDATE `parking_slots` SET `slot_type` = '4_WHEELER' WHERE `slot_type` IN ('FOUR_WHEELER', '4_WHEELER', 'CAR');
UPDATE `parking_slots` SET `slot_type` = '2_WHEELER' WHERE `slot_type` IN ('TWO_WHEELER', '2_WHEELER', 'BIKE', 'SCOOTER');

-- Set is_occupied based on status and assigned_flat_id
UPDATE `parking_slots` 
SET `is_occupied` = TRUE 
WHERE `status` = 'ASSIGNED' OR `assigned_flat_id` IS NOT NULL OR `flat_id` IS NOT NULL;

UPDATE `parking_slots` 
SET `is_occupied` = FALSE 
WHERE `is_occupied` IS NULL OR (`assigned_flat_id` IS NULL AND `status` != 'ASSIGNED');

-- Add new dedicated 2_WHEELER slots if none exist to test 2W vs 4W calculations
INSERT INTO `parking_slots` (`slot_number`, `slot_type`, `basement_level`, `flat_id`, `assigned_flat_id`, `status`, `is_occupied`)
VALUES 
('P-2W-01', '2_WHEELER', 'B1', 1, 1, 'ASSIGNED', TRUE),
('P-2W-02', '2_WHEELER', 'B1', NULL, NULL, 'AVAILABLE', FALSE),
('P-2W-03', '2_WHEELER', 'B2', NULL, NULL, 'AVAILABLE', FALSE),
('P-2W-04', '2_WHEELER', 'B2', NULL, NULL, 'AVAILABLE', FALSE)
ON DUPLICATE KEY UPDATE `slot_type` = VALUES(`slot_type`);


-- ----------------------------------------------------------------------------
-- 2. Update `flats` Table
-- ----------------------------------------------------------------------------
-- Add flat_type column if not present
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'flats' 
      AND COLUMN_NAME = 'flat_type'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `flats` ADD COLUMN `flat_type` VARCHAR(20) DEFAULT "2BHK" AFTER `floor_number`', 
    'SELECT "Column flat_type already exists"'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add carpet_area_sq_ft column if not present
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'flats' 
      AND COLUMN_NAME = 'carpet_area_sq_ft'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `flats` ADD COLUMN `carpet_area_sq_ft` DOUBLE DEFAULT 900.0 AFTER `flat_type`', 
    'SELECT "Column carpet_area_sq_ft already exists"'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add resident_id column if not present
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'flats' 
      AND COLUMN_NAME = 'resident_id'
);
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `flats` ADD COLUMN `resident_id` BIGINT NULL AFTER `status`, ADD CONSTRAINT `fk_flats_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE SET NULL', 
    'SELECT "Column resident_id already exists"'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Synchronize flat_type with existing bhk_type and set standardized carpet areas:
-- 1 BHK -> 550 sqft
-- 2 BHK -> 900 sqft
-- 3 BHK -> 1450 sqft
-- 4 BHK -> 2200 sqft
-- PENTHOUSE -> 3200 sqft
UPDATE `flats` SET `flat_type` = `bhk_type` WHERE `flat_type` IS NULL OR `flat_type` = '';

UPDATE `flats` SET `carpet_area_sq_ft` = 550.0 WHERE `flat_type` IN ('1BHK', '1 BHK');
UPDATE `flats` SET `carpet_area_sq_ft` = 900.0 WHERE `flat_type` IN ('2BHK', '2 BHK');
UPDATE `flats` SET `carpet_area_sq_ft` = 1450.0 WHERE `flat_type` IN ('3BHK', '3 BHK');
UPDATE `flats` SET `carpet_area_sq_ft` = 2200.0 WHERE `flat_type` IN ('4BHK', '4 BHK');
UPDATE `flats` SET `carpet_area_sq_ft` = 3200.0 WHERE `flat_type` IN ('PENTHOUSE', '5BHK');

-- Insert or update a dedicated 1 BHK unit for testing billing differences if not present
INSERT INTO `flats` (`id`, `society_id`, `wing`, `flat_number`, `floor_number`, `bhk_type`, `flat_type`, `square_feet`, `carpet_area_sq_ft`, `status`)
VALUES (11, 1, 'A', '103', 1, '1BHK', '1BHK', 550.0, 550.0, 'OCCUPIED')
ON DUPLICATE KEY UPDATE `flat_type` = '1BHK', `carpet_area_sq_ft` = 550.0;

-- Link primary resident_id to flats
UPDATE `flats` f
JOIN `residents` r ON r.flat_id = f.id AND r.status = 'ACTIVE'
SET f.resident_id = r.id;


-- ----------------------------------------------------------------------------
-- 3. Update `maintenance_bills` Table (Itemized Billing Breakdown)
-- ----------------------------------------------------------------------------
-- Add itemized billing columns
ALTER TABLE `maintenance_bills`
    ADD COLUMN IF NOT EXISTS `flat_type` VARCHAR(20) DEFAULT '2BHK' AFTER `bill_month`,
    ADD COLUMN IF NOT EXISTS `carpet_area_sq_ft` DOUBLE DEFAULT 900.0 AFTER `flat_type`,
    ADD COLUMN IF NOT EXISTS `rate_per_sq_ft` DECIMAL(10,2) DEFAULT 3.50 AFTER `carpet_area_sq_ft`,
    ADD COLUMN IF NOT EXISTS `variable_area_charge` DECIMAL(10,2) DEFAULT 3150.00 AFTER `rate_per_sq_ft`,
    ADD COLUMN IF NOT EXISTS `security_charge` DECIMAL(10,2) DEFAULT 1000.00 AFTER `variable_area_charge`,
    ADD COLUMN IF NOT EXISTS `lift_electricity_charge` DECIMAL(10,2) DEFAULT 800.00 AFTER `security_charge`,
    ADD COLUMN IF NOT EXISTS `sinking_fund` DECIMAL(10,2) DEFAULT 500.00 AFTER `lift_electricity_charge`,
    ADD COLUMN IF NOT EXISTS `administrative_fee` DECIMAL(10,2) DEFAULT 200.00 AFTER `sinking_fund`,
    ADD COLUMN IF NOT EXISTS `total_fixed_charges` DECIMAL(10,2) DEFAULT 2500.00 AFTER `administrative_fee`;

-- Backfill existing bills with itemized values based on their flat's carpet area
UPDATE `maintenance_bills` b
JOIN `flats` f ON b.flat_id = f.id
SET 
    b.flat_type = COALESCE(f.flat_type, f.bhk_type, '2BHK'),
    b.carpet_area_sq_ft = COALESCE(f.carpet_area_sq_ft, f.square_feet, 900.0),
    b.rate_per_sq_ft = 3.50,
    b.variable_area_charge = ROUND(3.50 * COALESCE(f.carpet_area_sq_ft, f.square_feet, 900.0), 2),
    b.security_charge = 1000.00,
    b.lift_electricity_charge = 800.00,
    b.sinking_fund = 500.00,
    b.administrative_fee = 200.00,
    b.total_fixed_charges = 2500.00,
    b.total_amount = ROUND(3.50 * COALESCE(f.carpet_area_sq_ft, f.square_feet, 900.0), 2) + 2500.00;

SELECT "Migration V2 completed successfully!" AS status;
