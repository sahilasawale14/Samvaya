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
    private final ParkingSlotRepository parkingSlotRepository;
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
        flat.setCurrentResidentId(savedResident.getId());
        flat.setStatus("OCCUPIED");
        flat.setOccupancyStatus("TENANT".equalsIgnoreCase(resType) ? "OCCUPIED_TENANT" : "OCCUPIED_OWNER");
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

    @Transactional
    public ResidentDTO offboardResident(Long residentId, ResidentDTO.OffboardResidentRequest request) {
        Resident resident = residentRepository.findById(residentId)
                .orElseGet(() -> residentRepository.findByUserId(residentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + residentId)));

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // 1. Revoke User login and deactivate
        com.samvaya.model.User user = resident.getUser();
        if (user != null) {
            user.setIsActive(false);
            user.setAccountStatus("INACTIVE");
            user.setMovedOutAt(now);
            userRepository.save(user);
        }

        // 2. Mark Resident record as INACTIVE
        resident.setStatus("INACTIVE");
        resident.setAccountStatus("INACTIVE");
        resident.setMovedOutAt(now);

        // 3. Mark Flat as VACANT and remove resident association
        com.samvaya.model.Flat flat = resident.getFlat();
        if (flat != null) {
            flat.setStatus("VACANT");
            flat.setOccupancyStatus("VACANT");
            flat.setResidentId(null);
            flat.setCurrentResidentId(null);
            flatRepository.save(flat);
            resident.setMoveOutDate(java.time.LocalDate.now());
        }

        // Also check if any other flat points to this resident
        java.util.List<com.samvaya.model.Flat> flatsWithResident = flatRepository.findAll().stream()
                .filter(f -> resident.getId().equals(f.getResidentId()) || resident.getId().equals(f.getCurrentResidentId()))
                .collect(Collectors.toList());
        for (com.samvaya.model.Flat f : flatsWithResident) {
            f.setStatus("VACANT");
            f.setOccupancyStatus("VACANT");
            f.setResidentId(null);
            f.setCurrentResidentId(null);
            flatRepository.save(f);
        }

        Resident saved = residentRepository.save(resident);

        // 4. Vacate Parking Slots
        boolean vacateParking = request == null || request.getVacateParking() == null || Boolean.TRUE.equals(request.getVacateParking());
        if (vacateParking && flat != null) {
            java.util.List<com.samvaya.model.ParkingSlot> slots = parkingSlotRepository.findByFlatId(flat.getId());
            slots.addAll(parkingSlotRepository.findByAssignedFlatId(flat.getId()));
            for (com.samvaya.model.ParkingSlot slot : slots) {
                slot.setFlat(null);
                slot.setAssignedFlatId(null);
                slot.setIsOccupied(false);
                slot.setStatus("AVAILABLE");
                parkingSlotRepository.save(slot);
            }
        }

        // 5. Cancel pending / pre-approved visitor passes
        boolean cancelVisitors = request == null || request.getCancelPendingVisitors() == null || Boolean.TRUE.equals(request.getCancelPendingVisitors());
        if (cancelVisitors) {
            java.util.Set<com.samvaya.model.Visitor> visitors = new java.util.HashSet<>(visitorRepository.findByResidentId(resident.getId()));
            if (flat != null) {
                visitors.addAll(visitorRepository.findByFlatId(flat.getId()));
            }
            for (com.samvaya.model.Visitor v : visitors) {
                if ("EXPECTED".equalsIgnoreCase(v.getStatus()) || "PENDING".equalsIgnoreCase(v.getStatus()) || "PRE_APPROVED".equalsIgnoreCase(v.getApprovalStatus())) {
                    v.setStatus("CANCELLED");
                    v.setApprovalStatus("REJECTED");
                    visitorRepository.save(v);
                }
            }
        }

        return mapToDTO(saved);
    }

    @Transactional
    public ResidentDTO updateResident(Long residentId, ResidentDTO request) {
        Resident resident = residentRepository.findById(residentId)
                .orElseGet(() -> residentRepository.findByUserId(residentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + residentId)));

        com.samvaya.model.User user = resident.getUser();
        if (user != null) {
            if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
                user.setFullName(request.getFullName().trim());
            }
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                user.setEmail(request.getEmail().trim());
            }
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                user.setPhone(request.getPhone().trim());
            }
            if (request.getAccountStatus() != null && !request.getAccountStatus().trim().isEmpty()) {
                user.setAccountStatus(request.getAccountStatus().trim().toUpperCase());
                if ("ACTIVE".equalsIgnoreCase(request.getAccountStatus())) {
                    user.setIsActive(true);
                } else if ("INACTIVE".equalsIgnoreCase(request.getAccountStatus()) || "OFFBOARDED".equalsIgnoreCase(request.getAccountStatus())) {
                    user.setIsActive(false);
                }
            }
            userRepository.save(user);
        }

        if (request.getResidentType() != null && !request.getResidentType().trim().isEmpty()) {
            resident.setResidentType(request.getResidentType().trim().toUpperCase());
        }
        if (request.getEmergencyContactName() != null) {
            resident.setEmergencyContactName(request.getEmergencyContactName().trim());
        }
        if (request.getEmergencyContactPhone() != null) {
            resident.setEmergencyContactPhone(request.getEmergencyContactPhone().trim());
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            resident.setStatus(request.getStatus().trim().toUpperCase());
        }
        if (request.getAccountStatus() != null && !request.getAccountStatus().trim().isEmpty()) {
            resident.setAccountStatus(request.getAccountStatus().trim().toUpperCase());
        }

        Resident saved = residentRepository.save(resident);
        return mapToDTO(saved);
    }

    public ResidentDTO mapToDTO(Resident r) {
        String accountStatus = r.getAccountStatus();
        if (accountStatus == null) {
            accountStatus = (r.getUser() != null && r.getUser().getAccountStatus() != null)
                    ? r.getUser().getAccountStatus()
                    : ("INACTIVE".equalsIgnoreCase(r.getStatus()) ? "INACTIVE" : "ACTIVE");
        }
        java.time.LocalDateTime movedOutAt = r.getMovedOutAt();
        if (movedOutAt == null && r.getUser() != null) {
            movedOutAt = r.getUser().getMovedOutAt();
        }

        return ResidentDTO.builder()
                .id(r.getId())
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .username(r.getUser() != null ? r.getUser().getUsername() : null)
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
                .accountStatus(accountStatus)
                .movedOutAt(movedOutAt)
                .build();
    }
}
