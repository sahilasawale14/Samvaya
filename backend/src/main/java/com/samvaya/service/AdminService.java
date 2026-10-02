package com.samvaya.service;

import com.samvaya.dto.DashboardDTOs.ActivityLogDTO;
import com.samvaya.dto.DashboardDTOs.AdminDashboardDTO;
import com.samvaya.dto.FlatDTO;
import com.samvaya.dto.ParkingSlotDTO.ParkingStatsDTO;
import com.samvaya.dto.ResidentDTO;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.AdminActivityLog;
import com.samvaya.model.Flat;
import com.samvaya.model.Resident;
import com.samvaya.model.User;
import com.samvaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FlatRepository flatRepository;
    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final ComplaintRepository complaintRepository;
    private final AmenityBookingRepository amenityBookingRepository;
    private final IncidentRepository incidentRepository;
    private final MaintenanceBillRepository maintenanceBillRepository;
    private final AdminActivityLogRepository adminActivityLogRepository;
    private final NoticeService noticeService;
    private final ComplaintService complaintService;
    private final ParkingService parkingService;

    @Transactional(readOnly = true)
    public AdminDashboardDTO getAdminDashboardStats() {
        Long totalFlats = flatRepository.count();
        Long occupiedFlats = flatRepository.countByStatus("OCCUPIED");
        Long vacantFlats = flatRepository.countByStatus("VACANT");

        Long totalResidents = residentRepository.countByStatus("ACTIVE");
        Long totalOwners = residentRepository.countByResidentType("OWNER");
        Long totalTenants = residentRepository.countByResidentType("TENANT");

        Long totalStaff = staffMemberRepository.count();
        Long activeSecurityStaff = (long) staffMemberRepository.findByDesignation("SECURITY_GUARD").size();

        Long pendingComplaints = complaintRepository.countByStatusNotIn(List.of("RESOLVED", "CLOSED"));
        Long resolvedComplaints = complaintRepository.countByStatus("RESOLVED");

        Long upcomingAmenityBookings = (long) amenityBookingRepository.findByBookingDate(LocalDate.now()).size();
        Long activeIncidents = incidentRepository.countByStatus("REPORTED") + incidentRepository.countByStatus("INVESTIGATING");

        BigDecimal collectedMaint = maintenanceBillRepository.sumCollectedMaintenance();
        BigDecimal pendingMaint = maintenanceBillRepository.sumPendingMaintenance();

        ParkingStatsDTO parkingStats = parkingService.getParkingStats();

        List<ActivityLogDTO> recentLogs = adminActivityLogRepository.findAllByOrderByCreatedAtDesc()
                .stream().limit(6)
                .map(log -> ActivityLogDTO.builder()
                        .id(log.getId())
                        .adminName(log.getUser() != null ? log.getUser().getFullName() : "Admin")
                        .action(log.getAction())
                        .module(log.getModule())
                        .description(log.getDescription())
                        .timestamp(log.getCreatedAt() != null ? log.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : "")
                        .build())
                .collect(Collectors.toList());

        return AdminDashboardDTO.builder()
                .totalFlats(totalFlats)
                .occupiedFlats(occupiedFlats)
                .vacantFlats(vacantFlats)
                .totalResidents(totalResidents)
                .totalOwners(totalOwners)
                .totalTenants(totalTenants)
                .totalStaff(totalStaff)
                .activeSecurityStaff(activeSecurityStaff)
                .pendingComplaints(pendingComplaints)
                .resolvedComplaints(resolvedComplaints)
                .upcomingAmenityBookings(upcomingAmenityBookings)
                .activeIncidents(activeIncidents)
                .collectedMaintenance(collectedMaint != null ? collectedMaint : BigDecimal.ZERO)
                .pendingMaintenance(pendingMaint != null ? pendingMaint : BigDecimal.ZERO)
                .totalParkingSlots(parkingStats.getTotalSlots())
                .occupiedParkingSlots(parkingStats.getOccupiedSlots())
                .availableParkingSlots(parkingStats.getAvailableSlots())
                .availableTwoWheelerSlots(parkingStats.getAvailableTwoWheeler())
                .availableFourWheelerSlots(parkingStats.getAvailableFourWheeler())
                .occupiedTwoWheelerSlots(parkingStats.getOccupiedTwoWheeler())
                .occupiedFourWheelerSlots(parkingStats.getOccupiedFourWheeler())
                .recentNotices(noticeService.getActiveNotices().stream().limit(4).collect(Collectors.toList()))
                .recentComplaints(complaintService.getAllComplaints().stream().limit(5).collect(Collectors.toList()))
                .recentActivities(recentLogs)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ResidentDTO> getAllResidents() {
        return residentRepository.findAll().stream()
                .map(this::mapToResidentDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResidentDTO> getResidentsByType(String type) {
        return residentRepository.findByResidentType(type.toUpperCase()).stream()
                .map(this::mapToResidentDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FlatDTO> getAllFlats() {
        return flatRepository.findAll().stream()
                .map(flat -> {
                    Resident resident = null;
                    if (flat.getResidentId() != null) {
                        resident = residentRepository.findById(flat.getResidentId()).orElse(null);
                    }
                    if (resident == null) {
                        List<Resident> residents = residentRepository.findByFlatIdAndStatus(flat.getId(), "ACTIVE");
                        resident = residents.isEmpty() ? null : residents.get(0);
                    }
                    
                    String fType = flat.getFlatType() != null ? flat.getFlatType() : flat.getBhkType();
                    Double carpet = flat.getCarpetAreaSqFt() != null ? flat.getCarpetAreaSqFt() : flat.getSquareFeet();

                    String occStatus = flat.getOccupancyStatus();
                    if (occStatus == null || occStatus.trim().isEmpty()) {
                        if (resident == null || "VACANT".equalsIgnoreCase(flat.getStatus())) {
                            occStatus = "VACANT";
                        } else if ("TENANT".equalsIgnoreCase(resident.getResidentType())) {
                            occStatus = "OCCUPIED_TENANT";
                        } else {
                            occStatus = "OCCUPIED_OWNER";
                        }
                    }

                    return FlatDTO.builder()
                            .id(flat.getId())
                            .wing(flat.getWing())
                            .flatNumber(flat.getFlatNumber())
                            .floorNumber(flat.getFloorNumber())
                            .flatType(fType)
                            .bhkType(fType)
                            .carpetAreaSqFt(carpet)
                            .squareFeet(carpet)
                            .residentId(resident != null ? resident.getId() : null)
                            .currentResidentId(resident != null ? resident.getId() : null)
                            .status(flat.getStatus())
                            .occupancyStatus(occStatus)
                            .currentResidentName(resident != null && resident.getUser() != null ? resident.getUser().getFullName() : "None")
                            .residentType(resident != null ? resident.getResidentType() : "-")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public FlatDTO createFlat(FlatDTO dto) {
        String fType = dto.getFlatType() != null ? dto.getFlatType() : (dto.getBhkType() != null ? dto.getBhkType() : "2BHK");
        Double carpet = dto.getCarpetAreaSqFt() != null ? dto.getCarpetAreaSqFt() : (dto.getSquareFeet() != null ? dto.getSquareFeet() : 900.0);

        Flat flat = Flat.builder()
                .wing(dto.getWing())
                .flatNumber(dto.getFlatNumber())
                .floorNumber(dto.getFloorNumber() != null ? dto.getFloorNumber() : 1)
                .flatType(fType)
                .bhkType(fType)
                .carpetAreaSqFt(carpet)
                .squareFeet(carpet)
                .residentId(dto.getResidentId())
                .status("VACANT")
                .build();
        Flat saved = flatRepository.save(flat);
        return FlatDTO.builder()
                .id(saved.getId())
                .wing(saved.getWing())
                .flatNumber(saved.getFlatNumber())
                .floorNumber(saved.getFloorNumber())
                .flatType(saved.getFlatType())
                .bhkType(saved.getBhkType())
                .carpetAreaSqFt(saved.getCarpetAreaSqFt())
                .squareFeet(saved.getSquareFeet())
                .residentId(saved.getResidentId())
                .status(saved.getStatus())
                .currentResidentName("None")
                .residentType("-")
                .build();
    }

    @Transactional(readOnly = true)
    public List<com.samvaya.dto.AuthDTOs.UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> {
                    Resident r = residentRepository.findByUserId(u.getId()).orElse(null);
                    return com.samvaya.dto.AuthDTOs.UserDTO.builder()
                            .id(u.getId())
                            .username(u.getUsername())
                            .fullName(u.getFullName())
                            .email(u.getEmail())
                            .phone(u.getPhone())
                            .role(u.getRole() != null ? u.getRole().getName() : "")
                            .residentType(r != null ? r.getResidentType() : null)
                            .flatId(r != null && r.getFlat() != null ? r.getFlat().getId() : null)
                            .wing(r != null && r.getFlat() != null ? r.getFlat().getWing() : null)
                            .flatNumber(r != null && r.getFlat() != null ? r.getFlat().getFlatNumber() : null)
                            .isActive(u.getIsActive())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void logAdminActivity(Long userId, String action, String module, String description, Long targetId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            AdminActivityLog log = AdminActivityLog.builder()
                    .user(user)
                    .action(action)
                    .module(module)
                    .description(description)
                    .targetId(targetId)
                    .ipAddress("127.0.0.1")
                    .build();
            adminActivityLogRepository.save(log);
        }
    }

    private ResidentDTO mapToResidentDTO(Resident r) {
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
