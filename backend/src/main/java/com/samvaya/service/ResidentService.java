package com.samvaya.service;

import com.samvaya.dto.DashboardDTOs.ResidentDashboardDTO;
import com.samvaya.dto.ResidentDTO;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.Resident;
import com.samvaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FlatRepository flatRepository;
    private final VisitorRepository visitorRepository;
    private final DeliveryRepository deliveryRepository;
    private final ComplaintRepository complaintRepository;
    private final AmenityBookingRepository amenityBookingRepository;
    private final MaintenanceBillRepository maintenanceBillRepository;
    private final NoticeService noticeService;
    private final NotificationRepository notificationRepository;
    private final VisitorService visitorService;
    private final DeliveryService deliveryService;

    @Transactional
    public ResidentDTO createResident(ResidentDTO.CreateResidentRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new com.samvaya.exception.BadRequestException("Username is required");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new com.samvaya.exception.BadRequestException("Username is already taken: " + request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && userRepository.existsByEmail(request.getEmail())) {
            throw new com.samvaya.exception.BadRequestException("Email is already registered: " + request.getEmail());
        }

        // 1. Resolve or create Flat
        String wing = (request.getWing() != null && !request.getWing().trim().isEmpty()) ? request.getWing().trim().toUpperCase() : "A";
        String flatNum = (request.getFlatNumber() != null && !request.getFlatNumber().trim().isEmpty()) ? request.getFlatNumber().trim() : "101";

        com.samvaya.model.Flat flat = null;
        if (request.getFlatId() != null) {
            flat = flatRepository.findById(request.getFlatId()).orElse(null);
        }
        if (flat == null) {
            flat = flatRepository.findByWingAndFlatNumber(wing, flatNum).orElse(null);
        }
        if (flat == null) {
            flat = flatRepository.findByFlatNumber(flatNum).orElse(null);
        }
        if (flat == null) {
            // Create flat dynamically if absent
            flat = com.samvaya.model.Flat.builder()
                    .wing(wing)
                    .flatNumber(flatNum)
                    .floorNumber(1)
                    .bhkType("2BHK")
                    .flatType("2BHK")
                    .squareFeet(900.0)
                    .carpetAreaSqFt(900.0)
                    .status("OCCUPIED")
                    .build();
            flat = flatRepository.save(flat);
        }

        // 2. Resolve Role
        com.samvaya.model.Role role = roleRepository.findByName("RESIDENT")
                .orElseGet(() -> roleRepository.save(com.samvaya.model.Role.builder().name("RESIDENT").build()));

        // 3. Create and save User
        String password = (request.getPassword() != null && !request.getPassword().trim().isEmpty()) ? request.getPassword() : "password123";
        com.samvaya.model.User user = com.samvaya.model.User.builder()
                .username(request.getUsername().trim())
                .password(password)
                .email(request.getEmail() != null && !request.getEmail().trim().isEmpty() ? request.getEmail().trim() : request.getUsername() + "@samvaya.com")
                .fullName(request.getFullName() != null && !request.getFullName().trim().isEmpty() ? request.getFullName().trim() : request.getUsername())
                .phone(request.getPhone() != null ? request.getPhone().trim() : "+91 98000 00000")
                .role(role)
                .isActive(true)
                .build();
        com.samvaya.model.User savedUser = userRepository.save(user);

        // 4. Create and save Resident
        String resType = (request.getResidentType() != null && !request.getResidentType().trim().isEmpty())
                ? request.getResidentType().trim().toUpperCase()
                : "OWNER";

        Resident resident = Resident.builder()
                .user(savedUser)
                .flat(flat)
                .residentType(resType)
                .moveInDate(java.time.LocalDate.now())
                .status("ACTIVE")
                .build();
        Resident savedResident = residentRepository.save(resident);

        // 5. Update flat association
        flat.setResidentId(savedResident.getId());
        flat.setStatus("OCCUPIED");
        flatRepository.save(flat);

        return mapToDTO(savedResident);
    }

    @Transactional(readOnly = true)
    public ResidentDashboardDTO getResidentDashboardStats(Long residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseGet(() -> residentRepository.findByUserId(residentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Resident profile not found for id: " + residentId)));

        Long expectedVisitors = (long) visitorRepository.findByResidentId(resident.getId()).stream()
                .filter(v -> "EXPECTED".equals(v.getStatus()) || "ARRIVED".equals(v.getStatus()))
                .count();

        Long activeDeliveries = (long) deliveryRepository.findByResidentId(resident.getId()).stream()
                .filter(d -> "EXPECTED".equals(d.getStatus()) || "ARRIVED".equals(d.getStatus()) || "VERIFIED".equals(d.getStatus()))
                .count();

        Long pendingComplaints = (long) complaintRepository.findByResidentId(resident.getId()).stream()
                .filter(c -> !"RESOLVED".equals(c.getStatus()) && !"CLOSED".equals(c.getStatus()))
                .count();

        BigDecimal pendingMaint = maintenanceBillRepository.findByResidentId(resident.getId()).stream()
                .filter(b -> !"PAID".equals(b.getStatus()))
                .map(b -> b.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long upcomingBookings = (long) amenityBookingRepository.findByResidentId(resident.getId()).stream()
                .filter(b -> b.getBookingDate().isEqual(LocalDate.now()) || b.getBookingDate().isAfter(LocalDate.now()))
                .count();

        return ResidentDashboardDTO.builder()
                .residentId(resident.getId())
                .residentName(resident.getUser() != null ? resident.getUser().getFullName() : "Resident")
                .residentType(resident.getResidentType())
                .flatNumber(resident.getFlat() != null ? resident.getFlat().getFlatNumber() : "")
                .wing(resident.getFlat() != null ? resident.getFlat().getWing() : "")
                .bhkType(resident.getFlat() != null ? resident.getFlat().getBhkType() : "")
                .expectedVisitorsCount(expectedVisitors)
                .activeDeliveriesCount(activeDeliveries)
                .pendingComplaintsCount(pendingComplaints)
                .pendingMaintenanceAmount(pendingMaint)
                .upcomingBookingsCount(upcomingBookings)
                .upcomingVisitors(visitorService.getVisitorsForResident(resident.getId()).stream().limit(5).collect(Collectors.toList()))
                .activeDeliveries(deliveryService.getDeliveriesForResident(resident.getId()).stream().limit(5).collect(Collectors.toList()))
                .recentNotices(noticeService.getActiveNotices().stream().limit(4).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public ResidentDTO getResidentProfile(Long residentId) {
        Resident r = residentRepository.findById(residentId)
                .orElseGet(() -> residentRepository.findByUserId(residentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Resident not found for id: " + residentId)));

        return mapToDTO(r);
    }

    public ResidentDTO mapToDTO(Resident r) {
        return ResidentDTO.builder()
                .id(r.getId())
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .fullName(r.getUser() != null ? r.getUser().getFullName() : "N/A")
                .email(r.getUser() != null ? r.getUser().getEmail() : "N/A")
                .phone(r.getUser() != null ? r.getUser().getPhone() : "N/A")
                .residentType(r.getResidentType())
                .flatId(r.getFlat() != null ? r.getFlat().getId() : null)
                .wing(r.getFlat() != null ? r.getFlat().getWing() : "")
                .flatNumber(r.getFlat() != null ? r.getFlat().getFlatNumber() : "")
                .bhkType(r.getFlat() != null ? r.getFlat().getBhkType() : "")
                .emergencyContactName(r.getEmergencyContactName())
                .emergencyContactPhone(r.getEmergencyContactPhone())
                .moveInDate(r.getMoveInDate())
                .status(r.getStatus())
                .build();
    }
}
