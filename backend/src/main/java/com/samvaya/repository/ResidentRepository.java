package com.samvaya.repository;

import com.samvaya.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {
    Optional<Resident> findByUserId(Long userId);
    List<Resident> findByFlatIdAndStatus(Long flatId, String status);
    List<Resident> findByFlatId(Long flatId);
    List<Resident> findByResidentType(String residentType);
    List<Resident> findByStatus(String status);
    Long countByResidentType(String residentType);
    Long countByStatus(String status);
}
