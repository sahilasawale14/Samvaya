package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.VisitorDTO;
import com.samvaya.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VisitorDTO>>> getAllVisitors(
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) Long flatId) {
        List<VisitorDTO> list = (residentId != null || flatId != null)
                ? visitorService.getVisitorsForResidentOrFlat(residentId, flatId)
                : visitorService.getAllVisitors();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/expected")
    public ResponseEntity<ApiResponse<List<VisitorDTO>>> getExpectedVisitors() {
        return ResponseEntity.ok(ApiResponse.success(visitorService.getVisitorsExpectedToday()));
    }

    @GetMapping("/inside")
    public ResponseEntity<ApiResponse<List<VisitorDTO>>> getVisitorsInside() {
        return ResponseEntity.ok(ApiResponse.success(visitorService.getVisitorsInside()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VisitorDTO>> createVisitorPass(@RequestBody VisitorDTO dto) {
        VisitorDTO created = visitorService.createVisitorPass(dto);
        return ResponseEntity.ok(ApiResponse.success("Visitor pass generated successfully", created));
    }

    @PostMapping("/{id}/arrive")
    public ResponseEntity<ApiResponse<VisitorDTO>> recordArrival(@PathVariable Long id) {
        VisitorDTO updated = visitorService.recordArrival(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor arrival recorded", updated));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<VisitorDTO>> approveVisitor(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean approved) {
        VisitorDTO updated = visitorService.approveVisitor(id, approved);
        return ResponseEntity.ok(ApiResponse.success("Visitor approval status updated", updated));
    }

    @PostMapping("/{id}/entry")
    public ResponseEntity<ApiResponse<VisitorDTO>> recordEntry(@PathVariable Long id,
                                                               @RequestParam(required = false) String gateNumber,
                                                               @RequestParam(required = false) String notes) {
        VisitorDTO updated = visitorService.recordEntry(id, gateNumber, notes);
        return ResponseEntity.ok(ApiResponse.success("Visitor entry logged into society", updated));
    }

    @PostMapping("/{id}/exit")
    public ResponseEntity<ApiResponse<VisitorDTO>> recordExit(@PathVariable Long id) {
        VisitorDTO updated = visitorService.recordExit(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor exit logged successfully", updated));
    }

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<VisitorDTO>> checkInVisitor(@RequestBody VisitorDTO.VisitorCheckInRequest request) {
        VisitorDTO checkedIn = visitorService.checkInVisitor(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success("Visitor gate entry logged successfully", checkedIn));
    }

    @RequestMapping(value = "/{id}/check-out", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<VisitorDTO>> checkOutVisitor(@PathVariable Long id) {
        VisitorDTO checkedOut = visitorService.checkOutVisitor(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor exit recorded successfully", checkedOut));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<VisitorDTO>>> getActiveVisitors() {
        return ResponseEntity.ok(ApiResponse.success(visitorService.getActiveVisitors()));
    }

    @PostMapping("/pre-approve")
    public ResponseEntity<ApiResponse<VisitorDTO>> preApproveVisitor(
            @RequestBody VisitorDTO.VisitorPreApprovalRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        VisitorDTO preApproved = visitorService.preApproveVisitor(request, headerUserId);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success("Visitor pre-approved successfully with face pass", preApproved));
    }

    @GetMapping("/pre-approved")
    public ResponseEntity<ApiResponse<List<VisitorDTO>>> getPreApprovedVisitors() {
        return ResponseEntity.ok(ApiResponse.success(visitorService.getPreApprovedVisitors()));
    }

    @RequestMapping(value = "/{id}/verify-entry", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<VisitorDTO>> verifyEntry(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Main Gate 1") String gateNumber,
            @RequestParam(required = false, defaultValue = "Face matched and verified by guard") String notes) {
        VisitorDTO verified = visitorService.verifyEntry(id, gateNumber, notes);
        return ResponseEntity.ok(ApiResponse.success("Visitor photo match verified. Entry allowed.", verified));
    }

    @RequestMapping(value = "/{id}/reject-entry", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<VisitorDTO>> rejectEntry(
            @PathVariable Long id,
            @RequestBody(required = false) VisitorDTO.VisitorRejectRequest body) {
        String remarks = body != null ? body.getRemarks() : "Photo mismatch or security concern";
        VisitorDTO rejected = visitorService.rejectEntry(id, remarks);
        return ResponseEntity.ok(ApiResponse.success("Visitor entry denied by security gate", rejected));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVisitor(@PathVariable Long id) {
        visitorService.deleteVisitor(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor record deleted", java.util.Map.of("id", id)));
    }
}
