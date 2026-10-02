package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "visitors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visitor_name", nullable = false, length = 150)
    private String visitorName;

    @Column(nullable = false, length = 30)
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @Column(nullable = false)
    private String purpose;

    @Column(name = "expected_date", nullable = false)
    private LocalDate expectedDate;

    @Column(name = "expected_time", nullable = false)
    private LocalTime expectedTime;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "number_of_visitors")
    @Builder.Default
    private Integer numberOfVisitors = 1;

    @Column(name = "total_guest_count")
    @Builder.Default
    private Integer totalGuestCount = 1;

    @Lob
    @Column(name = "primary_guest_photo", columnDefinition = "LONGTEXT")
    private String primaryGuestPhoto;

    @Column(name = "pre_approved_by_resident_id")
    private Long preApprovedByResidentId;

    @Column(length = 30)
    @Builder.Default
    private String status = "EXPECTED"; // 'EXPECTED', 'ARRIVED', 'INSIDE', 'EXITED', 'CANCELLED'

    @Column(name = "approval_status", length = 30)
    @Builder.Default
    private String approvalStatus = "APPROVED"; // 'PENDING', 'APPROVED', 'DENIED', 'PRE_APPROVED', 'VERIFIED_ENTRY', 'REJECTED'

    @Column(name = "pass_code", unique = true, length = 50)
    private String passCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.totalGuestCount == null) {
            this.totalGuestCount = this.numberOfVisitors != null ? this.numberOfVisitors : 1;
        }
        if (this.numberOfVisitors == null) {
            this.numberOfVisitors = this.totalGuestCount;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.totalGuestCount == null) {
            this.totalGuestCount = this.numberOfVisitors != null ? this.numberOfVisitors : 1;
        }
        if (this.numberOfVisitors == null) {
            this.numberOfVisitors = this.totalGuestCount;
        }
    }
}
