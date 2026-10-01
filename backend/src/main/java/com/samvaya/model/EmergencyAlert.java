package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String category; // 'FIRE', 'MEDICAL', 'SECURITY', 'WATER', 'POWER', 'MAINTENANCE', 'OTHER'

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(length = 20)
    @Builder.Default
    private String priority = "HIGH"; // 'HIGH', 'CRITICAL'

    @Column(length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // 'ACTIVE', 'RESOLVED', 'CLOSED'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_user_id")
    private User issuedByUser;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
