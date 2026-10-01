package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bill_id", nullable = false)
    private MaintenanceBill bill;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @Column(name = "amount_paid", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "payment_mode", nullable = false, length = 50)
    private String paymentMode; // 'UPI', 'NET_BANKING', 'CREDIT_CARD', 'CASH', 'CHEQUE'

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 100)
    private String transactionReference;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(length = 30)
    @Builder.Default
    private String status = "COMPLETED"; // 'COMPLETED', 'FAILED', 'PENDING'

    @PrePersist
    protected void onCreate() {
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
    }
}
