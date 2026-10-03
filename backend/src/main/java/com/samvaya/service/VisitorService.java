package com.samvaya.service;

import com.samvaya.dto.VisitorDTO;
import com.samvaya.exception.BadRequestException;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.Flat;
import com.samvaya.model.Resident;
import com.samvaya.model.Visitor;
import com.samvaya.model.VisitorEntryExit;
import com.samvaya.repository.FlatRepository;
import com.samvaya.repository.ResidentRepository;
import com.samvaya.repository.VisitorEntryExitRepository;
import com.samvaya.repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorRepository visitorRepository;
    private final VisitorEntryExitRepository visitorEntryExitRepository;
    private final ResidentRepository residentRepository;
    private final FlatRepository flatRepository;

    @Transactional(readOnly = true)
    public List<VisitorDTO> getAllVisitors() {
        return visitorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getVisitorsByResidentId(Long residentId) {
        if (residentId == null) {
            return Collections.emptyList();
        }
        return visitorRepository.findByResidentIdAndStatusNot(residentId, "REVOKED").stream()
                .filter(v -> !"REVOKED".equalsIgnoreCase(v.getStatus()) && !"CANCELLED".equalsIgnoreCase(v.getStatus()))
                .distinct()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getVisitorsByFlatId(Long flatId) {
        if (flatId == null) {
            return Collections.emptyList();
        }
        return visitorRepository.findByFlatId(flatId).stream()
                .filter(v -> !"REVOKED".equalsIgnoreCase(v.getStatus()) && !"CANCELLED".equalsIgnoreCase(v.getStatus()))
                .distinct()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getVisitorsForResidentOrFlat(Long residentId, Long flatId) {
        if (residentId != null) {
            return getVisitorsByResidentId(residentId);
        }
        if (flatId != null) {
            return getVisitorsByFlatId(flatId);
        }
        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getVisitorsForResident(Long residentId) {
        return getVisitorsByResidentId(residentId);
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getExpectedVisitors() {
        return visitorRepository.findByStatusInOrderByCreatedAtDesc(List.of("EXPECTED", "PRE_APPROVED", "PENDING")).stream()
                .filter(v -> !"REVOKED".equalsIgnoreCase(v.getStatus()) && !"CHECKED_IN".equalsIgnoreCase(v.getStatus()) && !"INSIDE".equalsIgnoreCase(v.getStatus()) && !"CHECKED_OUT".equalsIgnoreCase(v.getStatus()) && !"EXITED".equalsIgnoreCase(v.getStatus()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getVisitorsExpectedToday() {
        return getExpectedVisitors();
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getVisitorsInside() {
        return visitorRepository.findByStatusInOrderByCreatedAtDesc(List.of("CHECKED_IN", "INSIDE")).stream()
                .filter(v -> !"CHECKED_OUT".equalsIgnoreCase(v.getStatus()) && !"EXITED".equalsIgnoreCase(v.getStatus()) && !"REVOKED".equalsIgnoreCase(v.getStatus()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getActiveVisitors() {
        return getVisitorsInside();
    }

    @Transactional
    public VisitorDTO checkInVisitor(VisitorDTO.VisitorCheckInRequest request) {
        if (request.getVisitorName() == null || request.getVisitorName().trim().isEmpty()) {
            throw new BadRequestException("Visitor name is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new BadRequestException("Visitor phone is required");
        }

        String wing = (request.getWing() != null && !request.getWing().trim().isEmpty()) ? request.getWing().trim().toUpperCase() : "A";
        String flatNum = (request.getFlatNumber() != null && !request.getFlatNumber().trim().isEmpty()) ? request.getFlatNumber().trim() : "101";

        Flat flat = flatRepository.findByWingAndFlatNumber(wing, flatNum).orElse(null);
        if (flat == null) {
            flat = flatRepository.findByFlatNumber(flatNum).orElse(null);
        }
        if (flat == null) {
            flat = flatRepository.findAll().stream().findFirst().orElse(null);
        }

        Resident resident = null;
        if (flat != null && flat.getResidentId() != null) {
            resident = residentRepository.findById(flat.getResidentId()).orElse(null);
        }
        if (resident == null) {
            resident = residentRepository.findAll().stream().findFirst().orElse(null);
        }

        String passCode = "VIS-GATE-" + (1000 + new Random().nextInt(9000));

        Visitor visitor = Visitor.builder()
                .visitorName(request.getVisitorName().trim())
                .phone(request.getPhone().trim())
                .flat(flat)
                .resident(resident)
                .purpose(request.getPurpose() != null && !request.getPurpose().trim().isEmpty() ? request.getPurpose().trim() : "Guest Visit")
                .expectedDate(LocalDate.now())
                .expectedTime(java.time.LocalTime.now())
                .vehicleNumber(request.getEffectiveVehicleNumber())
                .numberOfVisitors(request.getNumberOfVisitors() != null ? request.getNumberOfVisitors() : 1)
                .status("INSIDE")
                .approvalStatus("APPROVED")
                .passCode(passCode)
                .build();

        Visitor savedVisitor = visitorRepository.save(visitor);

        VisitorEntryExit entryExit = VisitorEntryExit.builder()
                .visitor(savedVisitor)
                .entryTime(LocalDateTime.now())
                .gateNumber(request.getGateNumber() != null && !request.getGateNumber().trim().isEmpty() ? request.getGateNumber().trim() : "Main Gate 1")
                .verificationNotes("Guard Gate Check-In")
                .build();

        visitorEntryExitRepository.save(entryExit);

        return mapToDTO(savedVisitor);
    }

    @Transactional
    public VisitorDTO createVisitorPass(VisitorDTO dto) {
        Resident resident = null;
        if (dto.getResidentId() != null) {
            resident = residentRepository.findById(dto.getResidentId()).orElse(null);
        }
        if (resident == null) {
            resident = residentRepository.findAll().stream().findFirst().orElse(null);
        }
        if (resident == null) {
            throw new ResourceNotFoundException("No active resident found");
        }

        Flat flat = resident.getFlat();
        if (flat == null && dto.getFlatId() != null) {
            flat = flatRepository.findById(dto.getFlatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Flat not found"));
        }

        String passCode = "VIS-SAM-" + (1000 + new Random().nextInt(9000));

        Visitor visitor = Visitor.builder()
                .visitorName(dto.getVisitorName())
                .phone(dto.getPhone())
                .resident(resident)
                .flat(flat)
                .purpose(dto.getPurpose())
                .expectedDate(dto.getExpectedDate() != null ? dto.getExpectedDate() : LocalDate.now())
                .expectedTime(dto.getExpectedTime() != null ? dto.getExpectedTime() : java.time.LocalTime.now())
                .vehicleNumber(dto.getVehicleNumber())
                .numberOfVisitors(dto.getNumberOfVisitors() != null ? dto.getNumberOfVisitors() : 1)
                .status("EXPECTED")
                .approvalStatus("APPROVED")
                .passCode(passCode)
                .build();

        Visitor saved = visitorRepository.save(visitor);
        return mapToDTO(saved);
    }

    @Transactional
    public VisitorDTO recordArrival(Long visitorId) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found"));

        visitor.setStatus("ARRIVED");
        return mapToDTO(visitorRepository.save(visitor));
    }

    @Transactional
    public VisitorDTO approveVisitor(Long visitorId, boolean approved) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found"));

        visitor.setApprovalStatus(approved ? "APPROVED" : "DENIED");
        if (!approved) {
            visitor.setStatus("CANCELLED");
        }
        return mapToDTO(visitorRepository.save(visitor));
    }

    @Transactional
    public VisitorDTO recordEntry(Long visitorId, String gateNumber, String notes) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found"));

        if ("DENIED".equals(visitor.getApprovalStatus())) {
            throw new BadRequestException("Cannot allow entry for a denied visitor");
        }

        visitor.setStatus("INSIDE");
        visitorRepository.save(visitor);

        VisitorEntryExit log = VisitorEntryExit.builder()
                .visitor(visitor)
                .entryTime(LocalDateTime.now())
                .gateNumber(gateNumber != null ? gateNumber : "Main Gate 1")
                .verificationNotes(notes != null ? notes : "Gate OTP / ID verified")
                .build();

        visitorEntryExitRepository.save(log);
        return mapToDTO(visitor);
    }

    @Transactional
    public VisitorDTO recordExit(Long visitorId) {
        return checkOutVisitor(visitorId);
    }

    @Transactional
    public VisitorDTO preApproveVisitor(VisitorDTO.VisitorPreApprovalRequest request, Long residentId) {
        if (request.getVisitorName() == null || request.getVisitorName().trim().isEmpty()) {
            throw new BadRequestException("Visitor name is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new BadRequestException("Visitor phone is required");
        }

        Long targetResidentId = request.getResidentId() != null ? request.getResidentId() : residentId;
        Resident resident = null;
        if (targetResidentId != null) {
            resident = residentRepository.findById(targetResidentId).orElse(null);
        }

        // 1. Resolve Flat entity by flatId first
        Flat flat = null;
        Long flatId = request.getFlatId();
        if (flatId != null) {
            flat = flatRepository.findById(flatId).orElse(null);
        }

        // 2. Resolve via Resident's assigned flat if flat is still null
        if (flat == null && resident != null && resident.getFlat() != null) {
            flat = resident.getFlat();
        }

        // 3. Resolve via wing and sanitized flatNumber if still null
        if (flat == null && request.getFlatNumber() != null && !request.getFlatNumber().trim().isEmpty()) {
            String rawFlat = request.getFlatNumber().trim();
            // Sanitize e.g. "Wing A-A - 101" or "A-101" -> "101"
            String cleanFlat = rawFlat.replaceAll("(?i)^.*?(?:wing\\s*[a-z0-9]*\\s*[-–—]?\\s*)+", "").replaceAll("[^0-9]", "");
            if (cleanFlat.isEmpty()) {
                cleanFlat = rawFlat;
            }
            String wing = (request.getWing() != null && !request.getWing().trim().isEmpty()) ? request.getWing().trim().toUpperCase() : "A";

            flat = flatRepository.findByWingAndFlatNumber(wing, cleanFlat).orElse(null);
            if (flat == null) {
                flat = flatRepository.findByFlatNumber(cleanFlat).orElse(null);
            }
            if (flat == null) {
                flat = flatRepository.findByFlatNumber(rawFlat).orElse(null);
            }
        }

        // 4. Fallback to any existing flat in the database
        if (flat == null) {
            flat = flatRepository.findAll().stream().findFirst().orElse(null);
        }

        // 5. Ultimate fallback if society database is fresh/unseeded
        if (flat == null) {
            String wing = (request.getWing() != null && !request.getWing().trim().isEmpty()) ? request.getWing().trim().toUpperCase() : "A";
            flat = Flat.builder()
                    .wing(wing)
                    .flatNumber("101")
                    .floorNumber(1)
                    .flatType("2BHK")
                    .bhkType("2BHK")
                    .status("OCCUPIED")
                    .occupancyStatus("OCCUPIED_OWNER")
                    .build();
            flat = flatRepository.save(flat);
        }

        // Resolve resident if still null
        if (resident == null && flat.getResidentId() != null) {
            resident = residentRepository.findById(flat.getResidentId()).orElse(null);
        }
        if (resident == null) {
            resident = residentRepository.findAll().stream().findFirst().orElse(null);
        }

        LocalDate expDate = LocalDate.now();
        if (request.getExpectedDate() != null && !request.getExpectedDate().trim().isEmpty()) {
            try {
                expDate = LocalDate.parse(request.getExpectedDate().trim());
            } catch (Exception ignored) {}
        }

        java.time.LocalTime expTime = java.time.LocalTime.now();
        if (request.getExpectedTime() != null && !request.getExpectedTime().trim().isEmpty()) {
            try {
                String tStr = request.getExpectedTime().trim();
                if (tStr.length() == 5) tStr += ":00";
                expTime = java.time.LocalTime.parse(tStr);
            } catch (Exception ignored) {}
        }

        String passCode = String.format("%06d", 100000 + new Random().nextInt(900000));
        Integer guestCount = request.getEffectiveGuestCount();

        Visitor visitor = Visitor.builder()
                .visitorName(request.getVisitorName().trim())
                .phone(request.getPhone().trim())
                .resident(resident)
                .flat(flat)
                .purpose(request.getPurpose() != null && !request.getPurpose().trim().isEmpty() ? request.getPurpose().trim() : "Personal")
                .expectedDate(expDate)
                .expectedTime(expTime)
                .vehicleNumber(request.getVehicleNumber())
                .numberOfVisitors(guestCount)
                .totalGuestCount(guestCount)
                .primaryGuestPhoto(request.getPrimaryGuestPhoto())
                .preApprovedByResidentId(resident != null ? resident.getId() : targetResidentId)
                .status("EXPECTED")
                .approvalStatus("PRE_APPROVED")
                .passCode(passCode)
                .build();

        Visitor saved = visitorRepository.save(visitor);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getPreApprovedVisitors() {
        return getExpectedVisitors();
    }

    @Transactional
    public VisitorDTO checkInPass(Long visitorId) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor record not found"));

        if ("REJECTED".equalsIgnoreCase(visitor.getApprovalStatus()) || "DENIED".equalsIgnoreCase(visitor.getApprovalStatus()) || "REVOKED".equalsIgnoreCase(visitor.getStatus())) {
            throw new BadRequestException("Cannot check in a rejected or revoked visitor pass");
        }

        visitor.setApprovalStatus("VERIFIED_ENTRY");
        visitor.setStatus("CHECKED_IN");
        visitor.setEntryTime(LocalDateTime.now());
        Visitor saved = visitorRepository.save(visitor);

        VisitorEntryExit log = VisitorEntryExit.builder()
                .visitor(saved)
                .entryTime(LocalDateTime.now())
                .gateNumber("Main Gate 1")
                .verificationNotes("Checked in by security gate")
                .build();
        try {
            visitorEntryExitRepository.save(log);
        } catch (Exception ignored) {}

        return mapToDTO(saved);
    }

    @Transactional
    public VisitorDTO verifyEntry(Long visitorId, String gateNumber, String notes) {
        return checkInPass(visitorId);
    }

    @Transactional
    public VisitorDTO checkOutVisitor(Long visitorId) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor record not found"));

        visitor.setStatus("CHECKED_OUT");
        visitor.setExitTime(LocalDateTime.now());
        Visitor saved = visitorRepository.save(visitor);

        try {
            visitorEntryExitRepository.findTopByVisitorIdOrderByEntryTimeDesc(visitorId).ifPresent(log -> {
                log.setExitTime(LocalDateTime.now());
                visitorEntryExitRepository.save(log);
            });
        } catch (Exception ignored) {}

        return mapToDTO(saved);
    }

    @Transactional
    public VisitorDTO rejectEntry(Long visitorId, String remarks) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor record not found"));

        visitor.setApprovalStatus("REJECTED");
        visitor.setStatus("CANCELLED");
        Visitor saved = visitorRepository.save(visitor);

        if (remarks != null && !remarks.trim().isEmpty()) {
            VisitorEntryExit log = VisitorEntryExit.builder()
                    .visitor(saved)
                    .entryTime(LocalDateTime.now())
                    .verificationNotes("Entry Rejected by Security: " + remarks.trim())
                    .build();
            try {
                visitorEntryExitRepository.save(log);
            } catch (Exception ignored) {}
        }

        return mapToDTO(saved);
    }

    @Transactional
    public void revokeOrDeleteVisitorByResident(Long id, Long residentId) {
        if (id == null) return;
        visitorRepository.findById(id).ifPresent(visitor -> {
            if (residentId == null || (visitor.getResident() != null && residentId.equals(visitor.getResident().getId()))
                    || (residentId.equals(visitor.getPreApprovedByResidentId()))) {
                deleteVisitor(id);
            }
        });
    }

    @Transactional
    public void deleteVisitor(Long id) {
        if (id != null) {
            try {
                visitorEntryExitRepository.deleteByVisitorId(id);
            } catch (Exception ignored) {}
            if (visitorRepository.existsById(id)) {
                visitorRepository.deleteById(id);
            }
        }
    }

    private VisitorDTO mapToDTO(Visitor v) {
        VisitorEntryExit log = null;
        try {
            log = visitorEntryExitRepository.findTopByVisitorIdOrderByEntryTimeDesc(v.getId()).orElse(null);
        } catch (Exception ignored) {}

        String entryStr = null;
        if (v.getEntryTime() != null) {
            entryStr = v.getEntryTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else if (log != null && log.getEntryTime() != null) {
            entryStr = log.getEntryTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        }

        String exitStr = null;
        if (v.getExitTime() != null) {
            exitStr = v.getExitTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else if (log != null && log.getExitTime() != null) {
            exitStr = log.getExitTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        }

        return VisitorDTO.builder()
                .id(v.getId())
                .visitorName(v.getVisitorName())
                .phone(v.getPhone())
                .flatId(v.getFlat() != null ? v.getFlat().getId() : null)
                .wing(v.getFlat() != null ? v.getFlat().getWing() : "")
                .flatNumber(v.getFlat() != null ? v.getFlat().getFlatNumber() : "")
                .residentId(v.getResident() != null ? v.getResident().getId() : null)
                .residentName(v.getResident() != null && v.getResident().getUser() != null ? v.getResident().getUser().getFullName() : "N/A")
                .purpose(v.getPurpose())
                .expectedDate(v.getExpectedDate())
                .expectedTime(v.getExpectedTime())
                .vehicleNumber(v.getVehicleNumber())
                .numberOfVisitors(v.getNumberOfVisitors())
                .totalGuestCount(v.getTotalGuestCount() != null ? v.getTotalGuestCount() : v.getNumberOfVisitors())
                .primaryGuestPhoto(v.getPrimaryGuestPhoto())
                .preApprovedByResidentId(v.getPreApprovedByResidentId())
                .status(v.getStatus())
                .approvalStatus(v.getApprovalStatus())
                .passCode(v.getPassCode())
                .entryTime(entryStr)
                .exitTime(exitStr)
                .build();
    }
}
