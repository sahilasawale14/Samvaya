package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.DashboardDTOs.SecurityDashboardDTO;
import com.samvaya.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityService securityService;
    private final com.samvaya.service.VisitorService visitorService;
    private final com.samvaya.service.ParkingService parkingService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SecurityDashboardDTO>> getDashboard() {
        SecurityDashboardDTO stats = securityService.getSecurityDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/parking/metrics")
    public ResponseEntity<ApiResponse<com.samvaya.dto.ParkingSlotDTO.ParkingDetailedMetricsDTO>> getParkingMetrics() {
        return ResponseEntity.ok(ApiResponse.success(parkingService.getDetailedParkingMetrics()));
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> globalSearch(@RequestParam(required = false) String q) {
        Map<String, Object> results = securityService.globalSecuritySearch(q);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping("/visitors/check-in")
    public ResponseEntity<ApiResponse<com.samvaya.dto.VisitorDTO>> checkInVisitor(@RequestBody com.samvaya.dto.VisitorDTO.VisitorCheckInRequest request) {
        com.samvaya.dto.VisitorDTO checkedIn = visitorService.checkInVisitor(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success("Visitor gate entry logged successfully", checkedIn));
    }

    @RequestMapping(value = "/visitors/{id}/check-in", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<com.samvaya.dto.VisitorDTO>> checkInPass(@PathVariable Long id) {
        com.samvaya.dto.VisitorDTO checkedIn = visitorService.checkInPass(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor checked in successfully", checkedIn));
    }

    @RequestMapping(value = "/visitors/{id}/check-out", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<com.samvaya.dto.VisitorDTO>> checkOutVisitor(@PathVariable Long id) {
        com.samvaya.dto.VisitorDTO checkedOut = visitorService.checkOutVisitor(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor exit recorded successfully", checkedOut));
    }

    @GetMapping("/visitors/expected")
    public ResponseEntity<ApiResponse<java.util.List<com.samvaya.dto.VisitorDTO>>> getExpectedVisitors() {
        return ResponseEntity.ok(ApiResponse.success(visitorService.getExpectedVisitors()));
    }

    @GetMapping("/visitors/inside")
    public ResponseEntity<ApiResponse<java.util.List<com.samvaya.dto.VisitorDTO>>> getInsideVisitors() {
        return ResponseEntity.ok(ApiResponse.success(visitorService.getVisitorsInside()));
    }

    @GetMapping("/visitors/active")
    public ResponseEntity<ApiResponse<java.util.List<com.samvaya.dto.VisitorDTO>>> getActiveVisitors() {
        java.util.List<com.samvaya.dto.VisitorDTO> active = visitorService.getActiveVisitors();
        return ResponseEntity.ok(ApiResponse.success(active));
    }

    @GetMapping("/visitors/pre-approved")
    public ResponseEntity<ApiResponse<java.util.List<com.samvaya.dto.VisitorDTO>>> getPreApprovedVisitors() {
        return ResponseEntity.ok(ApiResponse.success(visitorService.getPreApprovedVisitors()));
    }

    @RequestMapping(value = "/visitors/{id}/verify-entry", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<com.samvaya.dto.VisitorDTO>> verifyEntry(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Main Gate 1") String gateNumber,
            @RequestParam(required = false, defaultValue = "Face matched and verified by guard") String notes) {
        com.samvaya.dto.VisitorDTO verified = visitorService.verifyEntry(id, gateNumber, notes);
        return ResponseEntity.ok(ApiResponse.success("Visitor photo match verified. Entry allowed.", verified));
    }

    @RequestMapping(value = "/visitors/{id}/reject-entry", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<com.samvaya.dto.VisitorDTO>> rejectEntry(
            @PathVariable Long id,
            @RequestBody(required = false) com.samvaya.dto.VisitorDTO.VisitorRejectRequest body) {
        String remarks = body != null ? body.getRemarks() : "Photo mismatch or security concern";
        com.samvaya.dto.VisitorDTO rejected = visitorService.rejectEntry(id, remarks);
        return ResponseEntity.ok(ApiResponse.success("Visitor entry denied by security gate", rejected));
    }

    @DeleteMapping("/visitors/{id}")
    public ResponseEntity<?> deleteVisitor(@PathVariable Long id) {
        visitorService.deleteVisitor(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor record deleted", java.util.Map.of("id", id)));
    }
}
