package com.samvaya.repository;

import com.samvaya.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByResidentId(Long residentId);
    Optional<Payment> findByBillId(Long billId);
    Optional<Payment> findByTransactionReference(String transactionReference);
}
