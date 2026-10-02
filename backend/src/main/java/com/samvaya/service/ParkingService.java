package com.samvaya.service;

import com.samvaya.dto.ParkingSlotDTO;
import com.samvaya.dto.ParkingSlotDTO.ParkingStatsDTO;
import com.samvaya.exception.BadRequestException;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.Flat;
import com.samvaya.model.ParkingSlot;
import com.samvaya.model.Resident;
import com.samvaya.repository.FlatRepository;
import com.samvaya.repository.ParkingSlotRepository;
import com.samvaya.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final FlatRepository flatRepository;
    private final ResidentRepository residentRepository;

    @Transactional(readOnly = true)
    public Long countByIsOccupiedFalse() {
        return parkingSlotRepository.countByIsOccupiedFalse();
    }

    @Transactional(readOnly = true)
    public Long countBySlotTypeAndIsOccupiedFalse(String slotType) {
        if ("2_WHEELER".equalsIgnoreCase(slotType) || "TWO_WHEELER".equalsIgnoreCase(slotType)) {
            return parkingSlotRepository.countBySlotTypesAndIsOccupiedFalse("2_WHEELER", "TWO_WHEELER");
        }
        return parkingSlotRepository.countBySlotTypesAndIsOccupiedFalse("4_WHEELER", "FOUR_WHEELER");
    }

    @Transactional(readOnly = true)
    public List<ParkingSlotDTO> listAvailableSlots() {
        return parkingSlotRepository.listAvailableSlots().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingSlotDTO> getAllSlots() {
        return parkingSlotRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParkingStatsDTO getParkingStats() {
        long total = parkingSlotRepository.count();
        long available = parkingSlotRepository.countByIsOccupiedFalse();
        long occupied = parkingSlotRepository.countByIsOccupiedTrue();

        long available2W = parkingSlotRepository.countBySlotTypesAndIsOccupiedFalse("2_WHEELER", "TWO_WHEELER");
        long available4W = parkingSlotRepository.countBySlotTypesAndIsOccupiedFalse("4_WHEELER", "FOUR_WHEELER");

        long occupied2W = parkingSlotRepository.countBySlotTypesAndIsOccupiedTrue("2_WHEELER", "TWO_WHEELER");
        long occupied4W = parkingSlotRepository.countBySlotTypesAndIsOccupiedTrue("4_WHEELER", "FOUR_WHEELER");

        return ParkingStatsDTO.builder()
                .totalSlots(total)
                .occupiedSlots(occupied)
                .availableSlots(available)
                .availableTwoWheeler(available2W)
                .availableFourWheeler(available4W)
                .occupiedTwoWheeler(occupied2W)
                .occupiedFourWheeler(occupied4W)
                .build();
    }

    @Transactional(readOnly = true)
    public ParkingSlotDTO.ParkingDetailedMetricsDTO getDetailedParkingMetrics() {
        long total = parkingSlotRepository.count();
        long available = parkingSlotRepository.countByIsOccupiedFalse();
        long occupied = parkingSlotRepository.countByIsOccupiedTrue();

        long availableCars = parkingSlotRepository.countBySlotTypesAndIsOccupiedFalse("4_WHEELER", "FOUR_WHEELER");
        long occupiedCars = parkingSlotRepository.countBySlotTypesAndIsOccupiedTrue("4_WHEELER", "FOUR_WHEELER");
        long totalCars = availableCars + occupiedCars;

        long availableBikes = parkingSlotRepository.countBySlotTypesAndIsOccupiedFalse("2_WHEELER", "TWO_WHEELER");
        long occupiedBikes = parkingSlotRepository.countBySlotTypesAndIsOccupiedTrue("2_WHEELER", "TWO_WHEELER");
        long totalBikes = availableBikes + occupiedBikes;

        return ParkingSlotDTO.ParkingDetailedMetricsDTO.builder()
                .totalSlots(total)
                .totalOccupied(occupied)
                .totalAvailable(available)
                .cars(ParkingSlotDTO.CategoryMetrics.builder()
                        .total(totalCars)
                        .occupied(occupiedCars)
                        .available(availableCars)
                        .build())
                .bikes(ParkingSlotDTO.CategoryMetrics.builder()
                        .total(totalBikes)
                        .occupied(occupiedBikes)
                        .available(availableBikes)
                        .build())
                .build();
    }


    @Transactional
    public ParkingSlotDTO assignSlot(Long slotId, Long flatId) {
        ParkingSlot slot = parkingSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + slotId));

        if (Boolean.TRUE.equals(slot.getIsOccupied()) && "ASSIGNED".equalsIgnoreCase(slot.getStatus())) {
            throw new BadRequestException("Parking slot " + slot.getSlotNumber() + " is already occupied!");
        }

        Flat flat = flatRepository.findById(flatId)
                .orElseThrow(() -> new ResourceNotFoundException("Flat not found with id: " + flatId));

        slot.setFlat(flat);
        slot.setAssignedFlatId(flat.getId());
        slot.setIsOccupied(true);
        slot.setStatus("ASSIGNED");

        ParkingSlot saved = parkingSlotRepository.save(slot);
        return mapToDTO(saved);
    }

    @Transactional
    public ParkingSlotDTO vacateSlot(Long slotId) {
        ParkingSlot slot = parkingSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + slotId));

        slot.setFlat(null);
        slot.setAssignedFlatId(null);
        slot.setIsOccupied(false);
        slot.setStatus("AVAILABLE");

        ParkingSlot saved = parkingSlotRepository.save(slot);
        return mapToDTO(saved);
    }

    @Transactional
    public void vacateSlotByFlatId(Long flatId) {
        List<ParkingSlot> slots = parkingSlotRepository.findByAssignedFlatId(flatId);
        if (slots.isEmpty()) {
            slots = parkingSlotRepository.findByFlatId(flatId);
        }
        for (ParkingSlot s : slots) {
            s.setFlat(null);
            s.setAssignedFlatId(null);
            s.setIsOccupied(false);
            s.setStatus("AVAILABLE");
            parkingSlotRepository.save(s);
        }
    }

    public ParkingSlotDTO mapToDTO(ParkingSlot slot) {
        String wing = null;
        String flatNum = null;
        String residentName = "None";

        Flat flat = slot.getFlat();
        if (flat == null && slot.getAssignedFlatId() != null) {
            flat = flatRepository.findById(slot.getAssignedFlatId()).orElse(null);
        }

        if (flat != null) {
            wing = flat.getWing();
            flatNum = flat.getFlatNumber();
            List<Resident> residents = residentRepository.findByFlatIdAndStatus(flat.getId(), "ACTIVE");
            if (!residents.isEmpty() && residents.get(0).getUser() != null) {
                residentName = residents.get(0).getUser().getFullName();
            }
        }

        String normalizedType = slot.getSlotType();
        if ("TWO_WHEELER".equalsIgnoreCase(normalizedType)) normalizedType = "2_WHEELER";
        if ("FOUR_WHEELER".equalsIgnoreCase(normalizedType)) normalizedType = "4_WHEELER";

        return ParkingSlotDTO.builder()
                .id(slot.getId())
                .slotNumber(slot.getSlotNumber())
                .slotType(normalizedType)
                .isOccupied(Boolean.TRUE.equals(slot.getIsOccupied()))
                .assignedFlatId(slot.getAssignedFlatId() != null ? slot.getAssignedFlatId() : (slot.getFlat() != null ? slot.getFlat().getId() : null))
                .wing(wing)
                .flatNumber(flatNum)
                .residentName(residentName)
                .status(slot.getStatus())
                .basementLevel(slot.getBasementLevel())
                .build();
    }
}
