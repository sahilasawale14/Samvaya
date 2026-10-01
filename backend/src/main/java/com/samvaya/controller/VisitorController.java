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
    public ResponseEntity<ApiResponse<List<VisitorDTO>>> getAllVisitors(@RequestParam(required = false) Long residentId) {
        List<VisitorDTO> list = (residentId != null)
                ? visitorService.getVisitorsForResident(residentId)
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
}
