package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.IncidentDTO;
import com.samvaya.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getIncidents() {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getAllIncidents()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IncidentDTO>> reportIncident(@RequestBody IncidentDTO dto) {
        IncidentDTO created = incidentService.reportIncident(dto);
        return ResponseEntity.ok(ApiResponse.success("Incident logged successfully", created));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<IncidentDTO>> updateStatus(@PathVariable Long id,
                                                                @RequestParam String status,
                                                                @RequestParam(required = false) String resolutionNotes) {
        IncidentDTO updated = incidentService.updateIncidentStatus(id, status, resolutionNotes);
        return ResponseEntity.ok(ApiResponse.success("Incident status updated", updated));
    }
}
