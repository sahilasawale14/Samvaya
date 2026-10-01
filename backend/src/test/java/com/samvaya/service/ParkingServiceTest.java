package com.samvaya.service;

import com.samvaya.dto.ParkingSlotDTO;
import com.samvaya.dto.ParkingSlotDTO.ParkingStatsDTO;
import com.samvaya.exception.BadRequestException;
import com.samvaya.model.Flat;
import com.samvaya.model.ParkingSlot;
import com.samvaya.repository.FlatRepository;
import com.samvaya.repository.ParkingSlotRepository;
import com.samvaya.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private ParkingSlotRepository parkingSlotRepository;

    @Mock
    private FlatRepository flatRepository;

    @Mock
    private ResidentRepository residentRepository;

    @InjectMocks
    private ParkingService parkingService;

    private ParkingSlot slot4WAvailable;
    private ParkingSlot slot2WAvailable;
    private ParkingSlot slotOccupied;
    private Flat flat;

    @BeforeEach
    void setUp() {
        flat = Flat.builder().id(10L).wing("B").flatNumber("201").build();

        slot4WAvailable = ParkingSlot.builder()
                .id(1L)
                .slotNumber("P-4W-01")
                .slotType("4_WHEELER")
                .isOccupied(false)
                .status("AVAILABLE")
                .build();

        slot2WAvailable = ParkingSlot.builder()
                .id(2L)
                .slotNumber("P-2W-01")
                .slotType("2_WHEELER")
                .isOccupied(false)
                .status("AVAILABLE")
                .build();

        slotOccupied = ParkingSlot.builder()
                .id(3L)
                .slotNumber("P-4W-02")
                .slotType("4_WHEELER")
                .isOccupied(true)
                .assignedFlatId(5L)
                .status("ASSIGNED")
                .build();
    }

    @Test
    @DisplayName("Verify dynamic parking slot calculation: total, occupied, available (2W & 4W)")
    void testParkingStats() {
        when(parkingSlotRepository.count()).thenReturn(10L);
        when(parkingSlotRepository.countByIsOccupiedFalse()).thenReturn(6L);
        when(parkingSlotRepository.countByIsOccupiedTrue()).thenReturn(4L);

        when(parkingSlotRepository.countBySlotTypesAndIsOccupiedFalse("2_WHEELER", "TWO_WHEELER")).thenReturn(3L);
        when(parkingSlotRepository.countBySlotTypesAndIsOccupiedFalse("4_WHEELER", "FOUR_WHEELER")).thenReturn(3L);
        when(parkingSlotRepository.countBySlotTypesAndIsOccupiedTrue("2_WHEELER", "TWO_WHEELER")).thenReturn(1L);
        when(parkingSlotRepository.countBySlotTypesAndIsOccupiedTrue("4_WHEELER", "FOUR_WHEELER")).thenReturn(3L);

        ParkingStatsDTO stats = parkingService.getParkingStats();

        assertEquals(10L, stats.getTotalSlots());
        assertEquals(4L, stats.getOccupiedSlots());
        assertEquals(6L, stats.getAvailableSlots());
        assertEquals(3L, stats.getAvailableTwoWheeler());
        assertEquals(3L, stats.getAvailableFourWheeler());
        assertEquals(1L, stats.getOccupiedTwoWheeler());
        assertEquals(3L, stats.getOccupiedFourWheeler());
    }

    @Test
    @DisplayName("Verify assigning available slot to a flat")
    void testAssignSlotSuccess() {
        when(parkingSlotRepository.findById(1L)).thenReturn(Optional.of(slot4WAvailable));
        when(flatRepository.findById(10L)).thenReturn(Optional.of(flat));
        when(parkingSlotRepository.save(any(ParkingSlot.class))).thenAnswer(i -> i.getArgument(0));

        ParkingSlotDTO assigned = parkingService.assignSlot(1L, 10L);

        assertTrue(assigned.getIsOccupied());
        assertEquals("ASSIGNED", assigned.getStatus());
        assertEquals(10L, assigned.getAssignedFlatId());
    }

    @Test
    @DisplayName("Verify cannot assign an already occupied slot")
    void testAssignAlreadyOccupiedSlotThrowsException() {
        when(parkingSlotRepository.findById(3L)).thenReturn(Optional.of(slotOccupied));

        assertThrows(BadRequestException.class, () -> parkingService.assignSlot(3L, 10L));
    }

    @Test
    @DisplayName("Verify vacating an occupied slot")
    void testVacateSlotSuccess() {
        when(parkingSlotRepository.findById(3L)).thenReturn(Optional.of(slotOccupied));
        when(parkingSlotRepository.save(any(ParkingSlot.class))).thenAnswer(i -> i.getArgument(0));

        ParkingSlotDTO vacated = parkingService.vacateSlot(3L);

        assertFalse(vacated.getIsOccupied());
        assertEquals("AVAILABLE", vacated.getStatus());
        assertNull(vacated.getAssignedFlatId());
    }
}
