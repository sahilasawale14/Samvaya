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

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SecurityDashboardDTO>> getDashboard() {
        SecurityDashboardDTO stats = securityService.getSecurityDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
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

    @RequestMapping(value = "/visitors/{id}/check-out", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<com.samvaya.dto.VisitorDTO>> checkOutVisitor(@PathVariable Long id) {
        com.samvaya.dto.VisitorDTO checkedOut = visitorService.checkOutVisitor(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor exit recorded successfully", checkedOut));
    }

    @GetMapping("/visitors/active")
    public ResponseEntity<ApiResponse<java.util.List<com.samvaya.dto.VisitorDTO>>> getActiveVisitors() {
        java.util.List<com.samvaya.dto.VisitorDTO> active = visitorService.getActiveVisitors();
        return ResponseEntity.ok(ApiResponse.success(active));
    }
}
