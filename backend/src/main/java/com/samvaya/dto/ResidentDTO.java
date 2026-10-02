package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentDTO {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String residentType; // 'OWNER' or 'TENANT'
    private Long flatId;
    private String wing;
    private String flatNumber;
    private String bhkType;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDate moveInDate;
    private String status;
    private String accountStatus; // 'ACTIVE', 'INACTIVE', 'OFFBOARDED'
    private LocalDateTime movedOutAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateResidentRequest {
        private String fullName;
        private String email;
        private String phone;
        private String flatNumber;
        private String wing;
        private String username;
        private String password;
        private String residentType;
        private Long flatId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OffboardResidentRequest {
        private String reason;
        private Boolean vacateParking;
        private Boolean cancelPendingVisitors;
        private Boolean clearDuesConfirmed;
    }
}
