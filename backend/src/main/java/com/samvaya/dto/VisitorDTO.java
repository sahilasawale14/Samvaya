package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorDTO {
    private Long id;
    private String visitorName;
    private String phone;
    private Long flatId;
    private String wing;
    private String flatNumber;
    private Long residentId;
    private String residentName;
    private String purpose;
    private LocalDate expectedDate;
    private LocalTime expectedTime;
    private String vehicleNumber;
    private Integer numberOfVisitors;
    private Integer totalGuestCount;
    private String primaryGuestPhoto;
    private Long preApprovedByResidentId;
    private String status; // 'EXPECTED', 'ARRIVED', 'INSIDE', 'EXITED', 'CANCELLED'
    private String approvalStatus; // 'PENDING', 'APPROVED', 'DENIED', 'PRE_APPROVED', 'VERIFIED_ENTRY', 'REJECTED'
    private String passCode;
    private String entryTime;
    private String exitTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VisitorCheckInRequest {
        private String visitorName;
        private String phone;
        private String flatNumber;
        private String wing;
        private String vehicleNo;
        private String vehicleNumber;
        private String purpose;
        private Integer numberOfVisitors;
        private Integer totalGuestCount;
        private String primaryGuestPhoto;
        private String gateNumber;

        public String getEffectiveVehicleNumber() {
            if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
                return vehicleNumber.trim();
            }
            return vehicleNo != null ? vehicleNo.trim() : null;
        }

        public Integer getEffectiveGuestCount() {
            if (totalGuestCount != null && totalGuestCount > 0) return totalGuestCount;
            if (numberOfVisitors != null && numberOfVisitors > 0) return numberOfVisitors;
            return 1;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VisitorPreApprovalRequest {
        private String visitorName;
        private String phone;
        private String flatNumber;
        private String wing;
        private Integer totalGuestCount;
        private Integer numberOfVisitors;
        private String primaryGuestPhoto;
        private String expectedDate;
        private String expectedTime;
        private String purpose;
        private String vehicleNumber;
        private Long residentId;

        public Integer getEffectiveGuestCount() {
            if (totalGuestCount != null && totalGuestCount > 0) return totalGuestCount;
            if (numberOfVisitors != null && numberOfVisitors > 0) return numberOfVisitors;
            return 1;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VisitorRejectRequest {
        private String remarks;
    }
}
