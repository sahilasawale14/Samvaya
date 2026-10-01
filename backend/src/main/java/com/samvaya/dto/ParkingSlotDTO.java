package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSlotDTO {
    private Long id;
    private String slotNumber;
    private String slotType; // '2_WHEELER', '4_WHEELER'
    private Boolean isOccupied;
    private Long assignedFlatId;
    private String wing;
    private String flatNumber;
    private String residentName;
    private String status;
    private String basementLevel;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParkingStatsDTO {
        private Long totalSlots;
        private Long occupiedSlots;
        private Long availableSlots;
        private Long availableTwoWheeler;
        private Long availableFourWheeler;
        private Long occupiedTwoWheeler;
        private Long occupiedFourWheeler;
    }
}
