package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"wing", "flat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String wing; // 'A', 'B', 'C'

    @Column(name = "flat_number", nullable = false, length = 30)
    private String flatNumber; // '101', '102', '201'

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "flat_type", length = 20)
    @Builder.Default
    private String flatType = "2BHK"; // '1BHK', '2BHK', '3BHK', '4BHK', 'PENTHOUSE'

    @Column(name = "bhk_type", length = 20)
    @Builder.Default
    private String bhkType = "2BHK"; // For backwards compatibility

    @Column(name = "carpet_area_sq_ft")
    @Builder.Default
    private Double carpetAreaSqFt = 900.0; // 550.0 for 1BHK, 900.0 for 2BHK, 1450.0 for 3BHK

    @Column(name = "square_feet")
    @Builder.Default
    private Double squareFeet = 900.0; // For backwards compatibility

    @Column(name = "resident_id")
    private Long residentId;

    @Column(length = 30)
    @Builder.Default
    private String status = "OCCUPIED"; // 'OCCUPIED', 'VACANT', 'UNDER_MAINTENANCE'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        syncFields();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        syncFields();
    }

    private void syncFields() {
        if (this.flatType == null && this.bhkType != null) {
            this.flatType = this.bhkType;
        } else if (this.bhkType == null && this.flatType != null) {
            this.bhkType = this.flatType;
        }
        if (this.carpetAreaSqFt == null && this.squareFeet != null) {
            this.carpetAreaSqFt = this.squareFeet;
        } else if (this.squareFeet == null && this.carpetAreaSqFt != null) {
            this.squareFeet = this.carpetAreaSqFt;
        }
    }
}
