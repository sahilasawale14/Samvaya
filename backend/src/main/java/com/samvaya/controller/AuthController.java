package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.AuthDTOs.CreateUserRequest;
import com.samvaya.dto.AuthDTOs.LoginRequest;
import com.samvaya.dto.AuthDTOs.LoginResponse;
import com.samvaya.dto.AuthDTOs.UserDTO;
import com.samvaya.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.samvaya.service.ResidentService residentService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO user = authService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", user));
    }

    @PostMapping("/register-resident")
    public ResponseEntity<ApiResponse<com.samvaya.dto.ResidentDTO>> registerResident(@RequestBody com.samvaya.dto.ResidentDTO.CreateResidentRequest request) {
        com.samvaya.dto.ResidentDTO resident = residentService.createResident(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success("Resident registered successfully", resident));
    }
}
