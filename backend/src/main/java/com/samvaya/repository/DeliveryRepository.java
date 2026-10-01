package com.samvaya.repository;

import com.samvaya.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByResidentId(Long residentId);
    List<Delivery> findByFlatId(Long flatId);
    List<Delivery> findByStatus(String status);
    Optional<Delivery> findByReferenceNumber(String referenceNumber);
    Long countByStatus(String status);
}
