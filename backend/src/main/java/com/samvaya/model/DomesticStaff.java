package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "domestic_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomesticStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "role_type", nullable = false, length = 50)
    private String roleType; // 'MAID', 'COOK', 'DRIVER', 'CLEANER', 'OTHER'

    @Column(name = "pass_code", unique = true, length = 50)
    private String passCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @Column(length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // 'ACTIVE', 'INACTIVE'

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
