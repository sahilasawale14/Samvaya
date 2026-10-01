package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String company; // 'Amazon', 'Flipkart', 'Swiggy', 'Zomato', 'Blinkit', 'Other'

    @Column(name = "delivery_person_name", length = 150)
    private String deliveryPersonName;

    @Column(length = 30)
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "is_expected")
    @Builder.Default
    private Boolean isExpected = true;

    @Column(length = 30)
    @Builder.Default
    private String status = "EXPECTED"; // 'EXPECTED', 'ARRIVED', 'VERIFIED', 'COMPLETED', 'CANCELLED'

    @Column(name = "approval_status", length = 30)
    @Builder.Default
    private String approvalStatus = "APPROVED"; // 'PENDING', 'APPROVED', 'DENIED'

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id")
    private User guard;

    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
