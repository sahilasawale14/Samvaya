package com.samvaya.repository;

import com.samvaya.model.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {
    List<EmergencyAlert> findByStatusOrderByCreatedAtDesc(String status);
    List<EmergencyAlert> findAllByOrderByCreatedAtDesc();
}
