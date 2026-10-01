package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 50)
    private String vehicleNumber;

    @Column(name = "vehicle_type", nullable = false, length = 30)
    private String vehicleType; // 'CAR', 'BIKE', 'SCOOTER', 'OTHER'

    @Column(name = "make_model", length = 100)
    private String makeModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_slot_id")
    private ParkingSlot parkingSlot;

    @Column(length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // 'ACTIVE', 'INACTIVE'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
