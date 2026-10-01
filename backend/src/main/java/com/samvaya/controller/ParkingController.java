package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.ParkingSlotDTO;
import com.samvaya.dto.ParkingSlotDTO.ParkingStatsDTO;
import com.samvaya.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingSlotDTO>>> getAllSlots() {
        List<ParkingSlotDTO> slots = parkingService.getAllSlots();
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ParkingSlotDTO>>> getAvailableSlots() {
        List<ParkingSlotDTO> slots = parkingService.listAvailableSlots();
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ParkingStatsDTO>> getParkingStats() {
        ParkingStatsDTO stats = parkingService.getParkingStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<ParkingSlotDTO>> assignSlot(
            @RequestParam(required = false) Long slotId,
            @RequestParam(required = false) Long flatId,
            @RequestBody(required = false) Map<String, Object> body) {
        
        Long sId = slotId;
        Long fId = flatId;
        if (sId == null && body != null && body.get("slotId") != null) {
            sId = Long.valueOf(body.get("slotId").toString());
        }
        if (fId == null && body != null && body.get("flatId") != null) {
            fId = Long.valueOf(body.get("flatId").toString());
        }

        if (sId == null || fId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Both slotId and flatId are required"));
        }

        ParkingSlotDTO assigned = parkingService.assignSlot(sId, fId);
        return ResponseEntity.ok(ApiResponse.success("Parking slot assigned successfully", assigned));
    }

    @PostMapping("/vacate")
    public ResponseEntity<ApiResponse<ParkingSlotDTO>> vacateSlot(
            @RequestParam(required = false) Long slotId,
            @RequestBody(required = false) Map<String, Object> body) {
        
        Long sId = slotId;
        if (sId == null && body != null && body.get("slotId") != null) {
            sId = Long.valueOf(body.get("slotId").toString());
        }

        if (sId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("slotId is required"));
        }

        ParkingSlotDTO vacated = parkingService.vacateSlot(sId);
        return ResponseEntity.ok(ApiResponse.success("Parking slot vacated successfully", vacated));
    }

    @PostMapping("/vacate-flat/{flatId}")
    public ResponseEntity<ApiResponse<String>> vacateSlotsForFlat(@PathVariable Long flatId) {
        parkingService.vacateSlotByFlatId(flatId);
        return ResponseEntity.ok(ApiResponse.success("All parking slots for flat vacated successfully", "OK"));
    }
}
