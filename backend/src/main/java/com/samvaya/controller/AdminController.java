package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.BillDTO;
import com.samvaya.dto.DashboardDTOs.AdminDashboardDTO;
import com.samvaya.dto.FlatDTO;
import com.samvaya.dto.ResidentDTO;
import com.samvaya.service.AdminService;
import com.samvaya.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final PaymentService paymentService;
    private final com.samvaya.service.ResidentService residentService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getDashboard() {
        AdminDashboardDTO stats = adminService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/residents")
    public ResponseEntity<ApiResponse<List<ResidentDTO>>> getAllResidents(@RequestParam(required = false) String type) {
        List<ResidentDTO> residents = (type != null && !type.isEmpty())
                ? adminService.getResidentsByType(type)
                : adminService.getAllResidents();
        return ResponseEntity.ok(ApiResponse.success(residents));
    }

    @PostMapping("/residents")
    public ResponseEntity<ApiResponse<ResidentDTO>> createResident(@RequestBody ResidentDTO.CreateResidentRequest request) {
        ResidentDTO resident = residentService.createResident(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success("Resident onboarded successfully", resident));
    }

    @GetMapping("/owners")
    public ResponseEntity<ApiResponse<List<ResidentDTO>>> getAllOwners() {
        List<ResidentDTO> owners = adminService.getResidentsByType("OWNER");
        return ResponseEntity.ok(ApiResponse.success(owners));
    }

    @GetMapping("/flats")
    public ResponseEntity<ApiResponse<List<FlatDTO>>> getAllFlats() {
        List<FlatDTO> flats = adminService.getAllFlats();
        return ResponseEntity.ok(ApiResponse.success(flats));
    }

    @PostMapping("/flats")
    public ResponseEntity<ApiResponse<FlatDTO>> createFlat(@RequestBody FlatDTO dto) {
        FlatDTO flat = adminService.createFlat(dto);
        return ResponseEntity.ok(ApiResponse.success("Flat created successfully", flat));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<com.samvaya.dto.AuthDTOs.UserDTO>>> getAllUsers() {
        List<com.samvaya.dto.AuthDTOs.UserDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/bills/generate-monthly")
    public ResponseEntity<ApiResponse<List<BillDTO>>> generateMonthlyBills(
            @RequestParam(required = false) String month,
            @RequestParam(required = false, defaultValue = "3.50") BigDecimal ratePerSqFt) {
        
        List<BillDTO> generatedBills = paymentService.generateMonthlyBills(month, ratePerSqFt);
        return ResponseEntity.ok(ApiResponse.success(
                "Successfully generated " + generatedBills.size() + " monthly maintenance bills for " + month,
                generatedBills
        ));
    }
}
