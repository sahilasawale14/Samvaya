package com.samvaya.repository;

import com.samvaya.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByResidentId(Long residentId);
    List<Complaint> findByResidentUserId(Long userId);
    List<Complaint> findByFlatId(Long flatId);
    List<Complaint> findByStatus(String status);
    List<Complaint> findByAssignedStaffId(Long staffId);
    Long countByStatus(String status);
    Long countByStatusNotIn(List<String> statuses);
}
