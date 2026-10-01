package com.samvaya.repository;

import com.samvaya.model.StaffAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffAttendanceRepository extends JpaRepository<StaffAttendance, Long> {
    List<StaffAttendance> findByAttendanceDate(LocalDate date);
    Optional<StaffAttendance> findByStaffMemberIdAndAttendanceDate(Long staffMemberId, LocalDate date);
    Long countByAttendanceDateAndStatus(LocalDate date, String status);
}
