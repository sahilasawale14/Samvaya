package com.samvaya.service;

import com.samvaya.dto.AmenityBookingDTO;
import com.samvaya.dto.AmenityDTO;
import com.samvaya.exception.BadRequestException;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.Amenity;
import com.samvaya.model.AmenityBooking;
import com.samvaya.model.Resident;
import com.samvaya.repository.AmenityBookingRepository;
import com.samvaya.repository.AmenityRepository;
import com.samvaya.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;
    private final AmenityBookingRepository amenityBookingRepository;
    private final ResidentRepository residentRepository;

    @Transactional(readOnly = true)
    public List<AmenityDTO> getAllAmenities() {
        return amenityRepository.findByIsActiveTrue().stream()
                .map(this::mapToAmenityDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AmenityBookingDTO> getAllBookings() {
        return amenityBookingRepository.findAll().stream()
                .map(this::mapToBookingDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AmenityDTO createAmenity(AmenityDTO dto) {
        Amenity amenity = Amenity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .capacity(dto.getCapacity() != null ? dto.getCapacity() : 50)
                .openTime(dto.getOpenTime() != null ? dto.getOpenTime() : LocalTime.of(6, 0))
                .closeTime(dto.getCloseTime() != null ? dto.getCloseTime() : LocalTime.of(22, 0))
                .hourlyRate(dto.getHourlyRate() != null ? dto.getHourlyRate() : BigDecimal.ZERO)
                .isActive(true)
                .build();
        return mapToAmenityDTO(amenityRepository.save(amenity));
    }

    @Transactional(readOnly = true)
    public List<AmenityBookingDTO> getBookingsForResident(Long residentId) {
        return amenityBookingRepository.findByResidentId(residentId).stream()
                .map(this::mapToBookingDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AmenityBookingDTO createBooking(AmenityBookingDTO dto) {
        Amenity amenity = amenityRepository.findById(dto.getAmenityId())
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found"));

        Resident resident = residentRepository.findById(dto.getResidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

        LocalDate date = dto.getBookingDate();
        LocalTime start = dto.getStartTime();
        LocalTime end = dto.getEndTime();

        if (end.isBefore(start) || end.equals(start)) {
            throw new BadRequestException("End time must be after start time");
        }

        // Conflict check
        List<AmenityBooking> conflicts = amenityBookingRepository.findConflictingBookings(amenity.getId(), date, start, end);
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Selected time slot conflicts with an existing booking for " + amenity.getName());
        }

        // Calculate amount
        long hours = Math.max(1, Duration.between(start, end).toHours());
        BigDecimal total = amenity.getHourlyRate().multiply(BigDecimal.valueOf(hours));

        AmenityBooking booking = AmenityBooking.builder()
                .amenity(amenity)
                .resident(resident)
                .flat(resident.getFlat())
                .bookingDate(date)
                .startTime(start)
                .endTime(end)
                .numberOfGuests(dto.getNumberOfGuests() != null ? dto.getNumberOfGuests() : 1)
                .totalAmount(total)
                .status("CONFIRMED")
                .build();

        return mapToBookingDTO(amenityBookingRepository.save(booking));
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        AmenityBooking booking = amenityBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus("CANCELLED");
        amenityBookingRepository.save(booking);
    }

    private AmenityDTO mapToAmenityDTO(Amenity a) {
        return AmenityDTO.builder()
                .id(a.getId())
                .name(a.getName())
                .description(a.getDescription())
                .capacity(a.getCapacity())
                .openTime(a.getOpenTime())
                .closeTime(a.getCloseTime())
                .hourlyRate(a.getHourlyRate())
                .isActive(a.getIsActive())
                .build();
    }

    private AmenityBookingDTO mapToBookingDTO(AmenityBooking b) {
        return AmenityBookingDTO.builder()
                .id(b.getId())
                .amenityId(b.getAmenity() != null ? b.getAmenity().getId() : null)
                .amenityName(b.getAmenity() != null ? b.getAmenity().getName() : "")
                .residentId(b.getResident() != null ? b.getResident().getId() : null)
                .residentName(b.getResident() != null && b.getResident().getUser() != null ? b.getResident().getUser().getFullName() : "N/A")
                .flatId(b.getFlat() != null ? b.getFlat().getId() : null)
                .wing(b.getFlat() != null ? b.getFlat().getWing() : "")
                .flatNumber(b.getFlat() != null ? b.getFlat().getFlatNumber() : "")
                .bookingDate(b.getBookingDate())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .numberOfGuests(b.getNumberOfGuests())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus())
                .build();
    }
}
