package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDTO {
    private Long id;
    private String company;
    private String deliveryPersonName;
    private String phone;
    private Long flatId;
    private String wing;
    private String flatNumber;
    private Long residentId;
    private String residentName;
    private String referenceNumber;
    private String vehicleNumber;
    private Boolean isExpected;
    private String status; // 'EXPECTED', 'ARRIVED', 'VERIFIED', 'COMPLETED', 'CANCELLED'
    private String approvalStatus;
    private String arrivedAt;
    private String completedAt;
    private String notes;
}
