package com.samvaya.controller;

import com.samvaya.dto.AmenityBookingDTO;
import com.samvaya.dto.AmenityDTO;
import com.samvaya.dto.ApiResponse;
import com.samvaya.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AmenityDTO>>> getAllAmenities() {
        return ResponseEntity.ok(ApiResponse.success(amenityService.getAllAmenities()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AmenityDTO>> createAmenity(@RequestBody AmenityDTO dto) {
        AmenityDTO created = amenityService.createAmenity(dto);
        return ResponseEntity.ok(ApiResponse.success("Amenity added successfully", created));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<AmenityBookingDTO>>> getBookings(@RequestParam(required = false) Long residentId) {
        List<AmenityBookingDTO> bookings = (residentId != null)
                ? amenityService.getBookingsForResident(residentId)
                : amenityService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<AmenityBookingDTO>> bookAmenity(@RequestBody AmenityBookingDTO dto) {
        AmenityBookingDTO booking = amenityService.createBooking(dto);
        return ResponseEntity.ok(ApiResponse.success("Amenity booked successfully", booking));
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long id) {
        amenityService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", null));
    }
}
