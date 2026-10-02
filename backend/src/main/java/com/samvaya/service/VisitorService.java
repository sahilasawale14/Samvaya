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
    public List<VisitorDTO> getVisitorsForResident(Long residentId) {
        return visitorRepository.findByResidentId(residentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getVisitorsExpectedToday() {
        return visitorRepository.findByExpectedDate(LocalDate.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisitorDTO> getVisitorsInside() {
        return visitorRepository.findByStatus("INSIDE").stream()
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
    public VisitorDTO checkOutVisitor(Long visitorId) {
        return recordExit(visitorId);
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
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found"));

        visitor.setStatus("EXITED");
        visitorRepository.save(visitor);

        VisitorEntryExit log = visitorEntryExitRepository.findTopByVisitorIdOrderByEntryTimeDesc(visitorId).orElse(null);
        if (log != null) {
            log.setExitTime(LocalDateTime.now());
            visitorEntryExitRepository.save(log);
        }

        return mapToDTO(visitor);
    }

    @Transactional
    public VisitorDTO preApproveVisitor(VisitorDTO.VisitorPreApprovalRequest request, Long residentId) {
        if (request.getVisitorName() == null || request.getVisitorName().trim().isEmpty()) {
            throw new BadRequestException("Visitor name is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new BadRequestException("Visitor phone is required");
        }

        Long targetResidentId = residentId != null ? residentId : request.getResidentId();
        Resident resident = null;
        if (targetResidentId != null) {
            resident = residentRepository.findById(targetResidentId).orElse(null);
        }

        String wing = (request.getWing() != null && !request.getWing().trim().isEmpty()) ? request.getWing().trim().toUpperCase() : "A";
        String flatNum = (request.getFlatNumber() != null && !request.getFlatNumber().trim().isEmpty()) ? request.getFlatNumber().trim() : "101";

        Flat flat = null;
        if (resident != null && resident.getFlat() != null) {
            flat = resident.getFlat();
        } else {
            flat = flatRepository.findByWingAndFlatNumber(wing, flatNum).orElse(null);
            if (flat == null) {
                flat = flatRepository.findByFlatNumber(flatNum).orElse(null);
            }
        }

        if (resident == null && flat != null && flat.getResidentId() != null) {
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

        String passCode = "VIS-PASS-" + (1000 + new Random().nextInt(9000));
        Integer guestCount = request.getEffectiveGuestCount();

        Visitor visitor = Visitor.builder()
                .visitorName(request.getVisitorName().trim())
                .phone(request.getPhone().trim())
                .resident(resident)
                .flat(flat)
                .purpose(request.getPurpose() != null && !request.getPurpose().trim().isEmpty() ? request.getPurpose().trim() : "Pre-Approved Guest")
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
        return visitorRepository.findByApprovalStatusOrderByExpectedDateDesc("PRE_APPROVED").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VisitorDTO verifyEntry(Long visitorId, String gateNumber, String notes) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor record not found"));

        if ("REJECTED".equalsIgnoreCase(visitor.getApprovalStatus()) || "DENIED".equalsIgnoreCase(visitor.getApprovalStatus())) {
            throw new BadRequestException("Cannot verify entry for a rejected visitor pass");
        }

        visitor.setApprovalStatus("VERIFIED_ENTRY");
        visitor.setStatus("INSIDE");
        Visitor saved = visitorRepository.save(visitor);

        VisitorEntryExit log = VisitorEntryExit.builder()
                .visitor(saved)
                .entryTime(LocalDateTime.now())
                .gateNumber(gateNumber != null && !gateNumber.trim().isEmpty() ? gateNumber.trim() : "Main Gate 1")
                .verificationNotes(notes != null && !notes.trim().isEmpty() ? notes.trim() : "Face verified primary guest gate pass")
                .build();
        visitorEntryExitRepository.save(log);

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
            visitorEntryExitRepository.save(log);
        }

        return mapToDTO(saved);
    }

    private VisitorDTO mapToDTO(Visitor v) {
        VisitorEntryExit log = visitorEntryExitRepository.findTopByVisitorIdOrderByEntryTimeDesc(v.getId()).orElse(null);

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
                .entryTime(log != null && log.getEntryTime() != null ? log.getEntryTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : null)
                .exitTime(log != null && log.getExitTime() != null ? log.getExitTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : null)
                .build();
    }
}
