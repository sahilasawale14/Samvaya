package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.DeliveryDTO;
import com.samvaya.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getAllDeliveries(@RequestParam(required = false) Long residentId) {
        List<DeliveryDTO> list = (residentId != null)
                ? deliveryService.getDeliveriesForResident(residentId)
                : deliveryService.getAllDeliveries();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryDTO>> createExpectedDelivery(@RequestBody DeliveryDTO dto) {
        DeliveryDTO created = deliveryService.createExpectedDelivery(dto);
        return ResponseEntity.ok(ApiResponse.success("Expected delivery registered successfully", created));
    }

    @PostMapping("/{id}/arrive")
    public ResponseEntity<ApiResponse<DeliveryDTO>> recordArrival(@PathVariable Long id) {
        DeliveryDTO updated = deliveryService.recordArrival(id);
        return ResponseEntity.ok(ApiResponse.success("Delivery arrival logged at security gate", updated));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<DeliveryDTO>> completeDelivery(@PathVariable Long id) {
        DeliveryDTO updated = deliveryService.completeDelivery(id);
        return ResponseEntity.ok(ApiResponse.success("Delivery completed and handed over", updated));
    }
}
