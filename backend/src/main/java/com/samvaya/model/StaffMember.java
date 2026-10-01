package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "emergency_contact", length = 30)
    private String emergencyContact;

    @Column(nullable = false, length = 50)
    private String designation; // 'GARDENER', 'CLEANER', 'TRASH_COLLECTOR', 'PLUMBER', 'ELECTRICIAN', 'MAINTENANCE_WORKER', 'SECURITY_GUARD', 'OTHER'

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal salary = BigDecimal.ZERO;

    @Column(name = "assigned_area", length = 100)
    @Builder.Default
    private String assignedArea = "All Wings";

    @Column(length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // 'ACTIVE', 'INACTIVE', 'ON_LEAVE'

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
