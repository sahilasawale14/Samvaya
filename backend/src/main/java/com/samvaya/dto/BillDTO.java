package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {
    private Long id;
    private Long flatId;
    private String wing;
    private String flatNumber;
    private String flatType; // '1BHK', '2BHK', '3BHK', etc.
    private Double carpetAreaSqFt;
    private Long residentId;
    private String residentName;
    private String billMonth; // '2026-10' or 'October 2026'

    // Variable Area Charge breakdown
    private BigDecimal ratePerSqFt;
    private BigDecimal variableAreaCharge; // (ratePerSqFt * carpetAreaSqFt)

    // Standard Fixed Charges breakdown (identical across all flats)
    private BigDecimal securityCharge;          // Fixed: ₹1,000.00
    private BigDecimal liftElectricityCharge;  // Fixed: ₹800.00
    private BigDecimal sinkingFund;            // Fixed: ₹500.00
    private BigDecimal administrativeFee;       // Fixed: ₹200.00
    private BigDecimal totalFixedCharges;       // Fixed: ₹2,500.00

    // Legacy and additional charges
    private BigDecimal maintenanceCharge;
    private BigDecimal waterCharge;
    private BigDecimal parkingCharge;
    private BigDecimal penaltyCharge;

    // Total Bill = Variable Area Charge + Total Fixed Charges (+ any water/parking/penalties)
    private BigDecimal totalAmount;
    private LocalDate dueDate;
    private String status;
}
