-- ============================================================================
-- SAMVAYA SOCIETY MANAGEMENT SYSTEM - MYSQL WORKBENCH DIAGNOSTIC QUERIES
-- ============================================================================

USE `samvaya`;

-- 1. View All Users and Their System Roles
SELECT u.id, u.username, u.full_name, u.email, u.phone, r.name AS role, u.is_active, u.created_at
FROM users u
JOIN roles r ON u.role_id = r.id
ORDER BY u.id;

-- 2. View All Residents with Flat Details & Occupancy Type (Owner / Tenant)
SELECT r.id AS resident_id, u.full_name, r.resident_type, CONCAT('Wing ', f.wing, '-', f.flat_number) AS flat,
       f.bhk_type, f.square_feet, r.move_in_date, r.emergency_contact_name, r.emergency_contact_phone, r.status
FROM residents r
JOIN users u ON r.user_id = u.id
JOIN flats f ON r.flat_id = f.id
ORDER BY f.wing, f.flat_number;

-- 3. View Flat Occupancy Overview
SELECT f.wing, f.flat_number, f.floor_number, f.bhk_type, f.status,
       u.full_name AS resident_name, r.resident_type
FROM flats f
LEFT JOIN residents r ON f.id = r.flat_id AND r.status = 'ACTIVE'
LEFT JOIN users u ON r.user_id = u.id
ORDER BY f.wing, f.flat_number;

-- 4. View Visitors Currently Inside or Expected Today
SELECT v.id, v.visitor_name, v.phone, v.purpose, CONCAT('Wing ', f.wing, '-', f.flat_number) AS destination_flat,
       u.full_name AS resident_host, v.expected_date, v.expected_time, v.status, v.pass_code,
       e.entry_time, e.exit_time
FROM visitors v
JOIN flats f ON v.flat_id = f.id
JOIN residents r ON v.resident_id = r.id
JOIN users u ON r.user_id = u.id
LEFT JOIN visitor_entry_exits e ON v.id = e.visitor_id
WHERE v.expected_date = CURDATE() OR v.status = 'INSIDE'
ORDER BY v.status, v.expected_time DESC;

-- 5. View Deliveries Status
SELECT d.id, d.company, d.delivery_person_name, d.phone, d.reference_number,
       CONCAT('Wing ', f.wing, '-', f.flat_number) AS flat, u.full_name AS resident,
       d.status, d.is_expected, d.arrived_at, d.completed_at
FROM deliveries d
JOIN flats f ON d.flat_id = f.id
JOIN residents r ON d.resident_id = r.id
JOIN users u ON r.user_id = u.id
ORDER BY d.created_at DESC;

-- 6. View Maintenance Bills & Payment Collections
SELECT b.id AS bill_id, b.bill_month, CONCAT('Wing ', f.wing, '-', f.flat_number) AS flat,
       u.full_name AS resident, b.total_amount, b.due_date, b.status AS bill_status,
       p.amount_paid, p.payment_mode, p.transaction_reference, p.payment_date
FROM maintenance_bills b
JOIN flats f ON b.flat_id = f.id
JOIN residents r ON b.resident_id = r.id
JOIN users u ON r.user_id = u.id
LEFT JOIN payments p ON b.id = p.bill_id
ORDER BY b.due_date DESC;

-- 7. View Open & Active Complaints
SELECT c.id, c.category, c.title, c.priority, c.status,
       CONCAT('Wing ', f.wing, '-', f.flat_number) AS flat, u.full_name AS complainant,
       sm.name AS assigned_staff, sm.designation, c.created_at
FROM complaints c
JOIN flats f ON c.flat_id = f.id
JOIN residents r ON c.resident_id = r.id
JOIN users u ON r.user_id = u.id
LEFT JOIN staff_members sm ON c.assigned_staff_id = sm.id
ORDER BY FIELD(c.priority, 'EMERGENCY', 'HIGH', 'MEDIUM', 'LOW'), c.created_at DESC;

-- 8. View Amenity Bookings & Schedules
SELECT ab.id AS booking_id, a.name AS amenity, CONCAT('Wing ', f.wing, '-', f.flat_number) AS flat,
       u.full_name AS booked_by, ab.booking_date, ab.start_time, ab.end_time,
       ab.number_of_guests, ab.total_amount, ab.status
FROM amenity_bookings ab
JOIN amenities a ON ab.amenity_id = a.id
JOIN flats f ON ab.flat_id = f.id
JOIN residents r ON ab.resident_id = r.id
JOIN users u ON r.user_id = u.id
ORDER BY ab.booking_date, ab.start_time;

-- 9. View Society Staff On-Duty & Today's Attendance
SELECT sm.id, sm.name, sm.designation, sm.phone, sm.assigned_area,
       COALESCE(sa.status, 'NOT_MARKED') AS today_attendance,
       sa.check_in_time, sa.check_out_time, sa.remarks
FROM staff_members sm
LEFT JOIN staff_attendance sa ON sm.id = sa.staff_member_id AND sa.attendance_date = CURDATE()
WHERE sm.status = 'ACTIVE'
ORDER BY sm.designation, sm.name;

-- 10. View Security Incidents
SELECT i.id, i.incident_type, i.description, i.location, i.priority, i.status,
       i.resolution_notes, i.created_at
FROM incidents i
ORDER BY i.created_at DESC;
