package com.samvaya.repository;

import com.samvaya.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    List<Visitor> findByResidentId(Long residentId);
    List<Visitor> findByPreApprovedByResidentId(Long residentId);
    List<Visitor> findByResidentIdOrPreApprovedByResidentId(Long residentId, Long preApprovedByResidentId);
    List<Visitor> findByResidentIdOrPreApprovedByResidentIdOrFlatId(Long residentId, Long preApprovedByResidentId, Long flatId);
    List<Visitor> findByFlatId(Long flatId);
    List<Visitor> findByStatus(String status);
    List<Visitor> findByExpectedDate(LocalDate date);
    List<Visitor> findByExpectedDateAndStatus(LocalDate date, String status);
    Optional<Visitor> findByPassCode(String passCode);
    List<Visitor> findByVisitorNameContainingIgnoreCaseOrPhoneContainingOrVehicleNumberContainingIgnoreCase(String name, String phone, String vehicleNumber);
    Long countByStatus(String status);
    Long countByExpectedDateAndStatus(LocalDate date, String status);
    List<Visitor> findByApprovalStatus(String approvalStatus);
    List<Visitor> findByApprovalStatusOrderByExpectedDateDesc(String approvalStatus);
    List<Visitor> findByResidentIdAndStatusNot(Long residentId, String status);
    List<Visitor> findByStatusIn(List<String> statuses);
    List<Visitor> findByStatusInOrderByCreatedAtDesc(List<String> statuses);
}
