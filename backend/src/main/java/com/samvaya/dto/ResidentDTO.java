package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentDTO {
    private Long id;
    private Long userId;
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
}
