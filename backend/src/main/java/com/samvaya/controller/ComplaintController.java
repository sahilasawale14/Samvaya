package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.ComplaintDTO;
import com.samvaya.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/complaints", "/api/resident/complaints"})
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintDTO>>> getAllComplaints(
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long effectiveUserId = userId != null ? userId : headerUserId;
        List<ComplaintDTO> list = (effectiveUserId != null || residentId != null)
                ? complaintService.getComplaintsByResidentOrUser(residentId, effectiveUserId)
                : complaintService.getAllComplaints();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintDTO>> createComplaint(
            @RequestBody ComplaintDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        if (dto.getUserId() == null && headerUserId != null) {
            dto.setUserId(headerUserId);
        }
        ComplaintDTO created = complaintService.createComplaint(dto);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint registered successfully", created));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ComplaintDTO>> updateStatus(@PathVariable Long id,
                                                                 @RequestParam String status,
                                                                 @RequestParam(required = false) Long staffId,
                                                                 @RequestParam(required = false) String remarks) {
        ComplaintDTO updated = complaintService.updateStatus(id, status, staffId, remarks);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint deleted successfully", null));
    }
}
