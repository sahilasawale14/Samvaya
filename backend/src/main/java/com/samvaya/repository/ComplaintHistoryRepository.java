package com.samvaya.repository;

import com.samvaya.model.ComplaintHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Long> {
    List<ComplaintHistory> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
    List<ComplaintHistory> findByComplaintId(Long complaintId);
    void deleteByComplaintId(Long complaintId);
}
