package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.DashboardDTOs.ResidentDashboardDTO;
import com.samvaya.dto.ResidentDTO;
import com.samvaya.service.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resident")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;
    private final com.samvaya.service.PaymentService paymentService;

    @GetMapping("/dashboard/{residentId}")
    public ResponseEntity<ApiResponse<ResidentDashboardDTO>> getDashboard(@PathVariable Long residentId) {
        ResidentDashboardDTO stats = residentService.getResidentDashboardStats(residentId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/profile/{residentId}")
    public ResponseEntity<ApiResponse<ResidentDTO>> getProfile(@PathVariable Long residentId) {
        ResidentDTO profile = residentService.getResidentProfile(residentId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/bills/my-bills")
    public ResponseEntity<ApiResponse<java.util.List<com.samvaya.dto.BillDTO>>> getMyBills(
            @RequestParam(required = false) Long flatId,
            @RequestParam(required = false) Long residentId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        java.util.List<com.samvaya.dto.BillDTO> bills = paymentService.getMyBills(flatId, residentId, headerUserId);
        return ResponseEntity.ok(ApiResponse.success(bills));
    }
}
