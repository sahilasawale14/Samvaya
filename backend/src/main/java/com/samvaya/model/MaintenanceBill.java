package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @Column(name = "bill_month", nullable = false, length = 20)
    private String billMonth; // '2026-10' or 'October 2026'

    @Column(name = "flat_type", length = 20)
    @Builder.Default
    private String flatType = "2BHK";

    @Column(name = "carpet_area_sq_ft")
    @Builder.Default
    private Double carpetAreaSqFt = 900.0;

    @Column(name = "rate_per_sq_ft", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal ratePerSqFt = BigDecimal.valueOf(3.50);

    @Column(name = "variable_area_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal variableAreaCharge = BigDecimal.valueOf(3150.00);

    // Fixed Charges (identical across all flats)
    @Column(name = "security_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal securityCharge = BigDecimal.valueOf(1000.00);

    @Column(name = "lift_electricity_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal liftElectricityCharge = BigDecimal.valueOf(800.00);

    @Column(name = "sinking_fund", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal sinkingFund = BigDecimal.valueOf(500.00);

    @Column(name = "administrative_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal administrativeFee = BigDecimal.valueOf(200.00);

    @Column(name = "total_fixed_charges", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalFixedCharges = BigDecimal.valueOf(2500.00);

    // Legacy / optional charge fields
    @Column(name = "maintenance_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal maintenanceCharge = BigDecimal.valueOf(3150.00);

    @Column(name = "water_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal waterCharge = BigDecimal.ZERO;

    @Column(name = "parking_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal parkingCharge = BigDecimal.ZERO;

    @Column(name = "penalty_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal penaltyCharge = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(length = 30)
    @Builder.Default
    private String status = "PENDING"; // 'PAID', 'PENDING', 'PARTIAL', 'OVERDUE'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateTotalsIfEmpty();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotalsIfEmpty();
    }

    public void calculateTotalsIfEmpty() {
        if (this.securityCharge == null) this.securityCharge = BigDecimal.valueOf(1000.00);
        if (this.liftElectricityCharge == null) this.liftElectricityCharge = BigDecimal.valueOf(800.00);
        if (this.sinkingFund == null) this.sinkingFund = BigDecimal.valueOf(500.00);
        if (this.administrativeFee == null) this.administrativeFee = BigDecimal.valueOf(200.00);
        
        this.totalFixedCharges = this.securityCharge
                .add(this.liftElectricityCharge)
                .add(this.sinkingFund)
                .add(this.administrativeFee);

        if (this.ratePerSqFt == null) this.ratePerSqFt = BigDecimal.valueOf(3.50);
        if (this.carpetAreaSqFt == null) {
            this.carpetAreaSqFt = (this.flat != null && this.flat.getCarpetAreaSqFt() != null)
                    ? this.flat.getCarpetAreaSqFt()
                    : 900.0;
        }

        if (this.variableAreaCharge == null || this.variableAreaCharge.compareTo(BigDecimal.ZERO) == 0) {
            this.variableAreaCharge = this.ratePerSqFt.multiply(BigDecimal.valueOf(this.carpetAreaSqFt));
        }

        this.maintenanceCharge = this.variableAreaCharge;

        if (this.waterCharge == null) this.waterCharge = BigDecimal.ZERO;
        if (this.parkingCharge == null) this.parkingCharge = BigDecimal.ZERO;
        if (this.penaltyCharge == null) this.penaltyCharge = BigDecimal.ZERO;

        if (this.totalAmount == null) {
            this.totalAmount = this.variableAreaCharge
                    .add(this.totalFixedCharges)
                    .add(this.waterCharge)
                    .add(this.parkingCharge)
                    .add(this.penaltyCharge);
        }
    }
}
