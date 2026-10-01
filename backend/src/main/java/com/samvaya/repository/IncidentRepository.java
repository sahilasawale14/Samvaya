package com.samvaya.repository;

import com.samvaya.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByStatusOrderByCreatedAtDesc(String status);
    List<Incident> findAllByOrderByCreatedAtDesc();
    Long countByStatus(String status);
}
