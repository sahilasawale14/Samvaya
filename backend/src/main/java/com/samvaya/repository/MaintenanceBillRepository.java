package com.samvaya.repository;

import com.samvaya.model.MaintenanceBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceBillRepository extends JpaRepository<MaintenanceBill, Long> {
    List<MaintenanceBill> findByResidentId(Long residentId);
    List<MaintenanceBill> findByFlatId(Long flatId);
    List<MaintenanceBill> findByStatus(String status);
    List<MaintenanceBill> findByBillMonth(String billMonth);
    Optional<MaintenanceBill> findByFlatIdAndBillMonth(Long flatId, String billMonth);
    
    @Query("SELECT SUM(b.totalAmount) FROM MaintenanceBill b WHERE b.status = 'PAID'")
    BigDecimal sumCollectedMaintenance();

    @Query("SELECT SUM(b.totalAmount) FROM MaintenanceBill b WHERE b.status <> 'PAID'")
    BigDecimal sumPendingMaintenance();

    Long countByStatus(String status);
}
