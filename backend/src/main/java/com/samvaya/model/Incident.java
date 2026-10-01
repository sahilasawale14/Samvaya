package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_type", nullable = false, length = 100)
    private String incidentType; // 'Suspicious Activity', 'Theft', 'Property Damage', 'Unauthorized Entry', 'Fire Hazard', 'Water Leakage', 'Vehicle Incident', 'Other'

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(length = 20)
    @Builder.Default
    private String priority = "MEDIUM"; // 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'

    @Column(length = 30)
    @Builder.Default
    private String status = "REPORTED"; // 'REPORTED', 'INVESTIGATING', 'RESOLVED', 'CLOSED'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_guard_id")
    private User reportedByGuard;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "related_flat_id")
    private Flat relatedFlat;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

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
