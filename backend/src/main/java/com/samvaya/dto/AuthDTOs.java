package com.samvaya.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private Long id;
        private Long userId;
        private String username;
        private String fullName;
        private String email;
        private String role; // 'ADMIN', 'RESIDENT', 'SECURITY'
        private String residentType; // 'OWNER', 'TENANT', or null
        private Long residentId;
        private Long flatId;
        private String flatNumber;
        private String wing;
        private String token;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private String role;
        private String residentType;
        private Long flatId;
        private String wing;
        private String flatNumber;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Full Name is required")
        private String fullName;

        private String phone;

        @NotBlank(message = "Role is required")
        private String role; // 'ADMIN', 'RESIDENT', 'SECURITY_GUARD'

        // Optional fields if creating a resident user directly
        private Long flatId;
        private String residentType; // 'OWNER', 'TENANT'
    }
}
