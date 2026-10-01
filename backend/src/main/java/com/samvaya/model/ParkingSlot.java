package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_number", nullable = false, unique = true, length = 50)
    private String slotNumber;

    @Column(name = "slot_type", length = 30)
    @Builder.Default
    private String slotType = "4_WHEELER"; // '2_WHEELER', '4_WHEELER'

    @Column(name = "is_occupied", nullable = false)
    @Builder.Default
    private Boolean isOccupied = false;

    @Column(name = "assigned_flat_id")
    private Long assignedFlatId;

    @Column(name = "basement_level", length = 20)
    @Builder.Default
    private String basementLevel = "B1";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @Column(length = 30)
    @Builder.Default
    private String status = "AVAILABLE"; // 'ASSIGNED', 'AVAILABLE', 'RESERVED'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        syncOccupancy();
    }

    @PreUpdate
    protected void onUpdate() {
        syncOccupancy();
    }

    public void syncOccupancy() {
        if (this.isOccupied == null) {
            this.isOccupied = Boolean.FALSE;
        }
        if (this.assignedFlatId != null || this.flat != null || "ASSIGNED".equalsIgnoreCase(this.status)) {
            this.isOccupied = true;
            this.status = "ASSIGNED";
            if (this.assignedFlatId == null && this.flat != null) {
                this.assignedFlatId = this.flat.getId();
            }
        } else {
            this.isOccupied = false;
            if ("ASSIGNED".equalsIgnoreCase(this.status)) {
                this.status = "AVAILABLE";
            }
        }
    }
}
