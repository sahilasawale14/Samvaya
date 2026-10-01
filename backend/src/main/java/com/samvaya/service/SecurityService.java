package com.samvaya.service;

import com.samvaya.dto.DashboardDTOs.SecurityDashboardDTO;
import com.samvaya.dto.DeliveryDTO;
import com.samvaya.dto.IncidentDTO;
import com.samvaya.dto.ParkingSlotDTO.ParkingStatsDTO;
import com.samvaya.dto.VisitorDTO;
import com.samvaya.model.TemporaryWorker;
import com.samvaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final VisitorRepository visitorRepository;
    private final DeliveryRepository deliveryRepository;
    private final TemporaryWorkerRepository temporaryWorkerRepository;
    private final IncidentRepository incidentRepository;
    private final StaffAttendanceRepository staffAttendanceRepository;
    private final VisitorService visitorService;
    private final DeliveryService deliveryService;
    private final IncidentService incidentService;
    private final FlatRepository flatRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingService parkingService;

    @Transactional(readOnly = true)
    public SecurityDashboardDTO getSecurityDashboardStats() {
        Long expectedVisitorsToday = visitorRepository.countByExpectedDateAndStatus(LocalDate.now(), "EXPECTED")
                + visitorRepository.countByExpectedDateAndStatus(LocalDate.now(), "ARRIVED");
        Long visitorsInside = visitorRepository.countByStatus("INSIDE");

        Long expectedDeliveriesToday = deliveryRepository.countByStatus("EXPECTED");
        Long deliveriesPendingAtGate = deliveryRepository.countByStatus("ARRIVED");

        Long workersInside = temporaryWorkerRepository.countByStatus("INSIDE");
        Long activeIncidents = incidentRepository.countByStatus("REPORTED") + incidentRepository.countByStatus("INVESTIGATING");
        Long staffPresent = staffAttendanceRepository.countByAttendanceDateAndStatus(LocalDate.now(), "PRESENT");

        ParkingStatsDTO parkingStats = parkingService.getParkingStats();

        return SecurityDashboardDTO.builder()
                .expectedVisitorsToday(expectedVisitorsToday)
                .visitorsCurrentlyInside(visitorsInside)
                .expectedDeliveriesToday(expectedDeliveriesToday)
                .deliveriesPendingAtGate(deliveriesPendingAtGate)
                .temporaryWorkersInside(workersInside)
                .activeIncidentsCount(activeIncidents)
                .staffPresentToday(staffPresent != null ? staffPresent : 0L)
                .totalParkingSlots(parkingStats.getTotalSlots())
                .occupiedParkingSlots(parkingStats.getOccupiedSlots())
                .availableParkingSlots(parkingStats.getAvailableSlots())
                .availableTwoWheelerSlots(parkingStats.getAvailableTwoWheeler())
                .availableFourWheelerSlots(parkingStats.getAvailableFourWheeler())
                .occupiedTwoWheelerSlots(parkingStats.getOccupiedTwoWheeler())
                .occupiedFourWheelerSlots(parkingStats.getOccupiedFourWheeler())
                .recentGateVisitors(visitorService.getAllVisitors().stream().limit(5).collect(Collectors.toList()))
                .recentGateDeliveries(deliveryService.getAllDeliveries().stream().limit(5).collect(Collectors.toList()))
                .recentIncidents(incidentService.getAllIncidents().stream().limit(4).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> globalSecuritySearch(String query) {
        Map<String, Object> results = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String q = query.trim();
        results.put("visitors", visitorRepository.findByVisitorNameContainingIgnoreCaseOrPhoneContainingOrVehicleNumberContainingIgnoreCase(q, q, q));
        results.put("vehicles", vehicleRepository.findByVehicleNumberContainingIgnoreCase(q));
        return results;
    }

    @Transactional(readOnly = true)
    public List<TemporaryWorker> getWorkersInside() {
        return temporaryWorkerRepository.findByStatus("INSIDE");
    }
}
