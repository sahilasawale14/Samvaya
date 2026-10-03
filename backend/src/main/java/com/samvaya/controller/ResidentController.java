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
    private final com.samvaya.service.VisitorService visitorService;

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

    @GetMapping("/visitors")
    public ResponseEntity<ApiResponse<java.util.List<com.samvaya.dto.VisitorDTO>>> getMyVisitors(
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) Long flatId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long targetResidentId = residentId != null ? residentId : headerUserId;
        java.util.List<com.samvaya.dto.VisitorDTO> visitors = visitorService.getVisitorsForResidentOrFlat(targetResidentId, flatId);
        return ResponseEntity.ok(ApiResponse.success(visitors));
    }

    @PostMapping("/visitors/pre-approve")
    public ResponseEntity<ApiResponse<com.samvaya.dto.VisitorDTO>> preApproveVisitor(
            @RequestBody com.samvaya.dto.VisitorDTO.VisitorPreApprovalRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        com.samvaya.dto.VisitorDTO preApproved = visitorService.preApproveVisitor(request, headerUserId);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success("Visitor pre-approved successfully with face pass", preApproved));
    }
}
