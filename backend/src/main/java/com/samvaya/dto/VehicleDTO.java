package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private Long id;
    private Long residentId;
    private String residentName;
    private Long flatId;
    private String wing;
    private String flatNumber;
    private String vehicleNumber;
    private String vehicleType; // 'CAR', 'BIKE', 'SCOOTER', 'OTHER'
    private String makeModel;
    private String parkingSlotNumber;
    private String status;
}
