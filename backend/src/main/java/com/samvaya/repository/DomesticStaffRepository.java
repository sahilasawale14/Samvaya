package com.samvaya.repository;

import com.samvaya.model.DomesticStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DomesticStaffRepository extends JpaRepository<DomesticStaff, Long> {
    List<DomesticStaff> findByResidentId(Long residentId);
    List<DomesticStaff> findByFlatId(Long flatId);
    Optional<DomesticStaff> findByPassCode(String passCode);
}
