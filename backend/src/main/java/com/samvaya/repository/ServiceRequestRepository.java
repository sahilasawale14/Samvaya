package com.samvaya.repository;

import com.samvaya.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByResidentId(Long residentId);
    List<ServiceRequest> findByFlatId(Long flatId);
    List<ServiceRequest> findByStatus(String status);
    Long countByStatus(String status);
}
