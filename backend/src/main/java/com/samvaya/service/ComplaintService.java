package com.samvaya.service;

import com.samvaya.dto.ComplaintDTO;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.Complaint;
import com.samvaya.model.ComplaintHistory;
import com.samvaya.model.Resident;
import com.samvaya.model.StaffMember;
import com.samvaya.repository.ComplaintHistoryRepository;
import com.samvaya.repository.ComplaintRepository;
import com.samvaya.repository.ResidentRepository;
import com.samvaya.repository.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintHistoryRepository complaintHistoryRepository;
    private final ResidentRepository residentRepository;
    private final StaffMemberRepository staffMemberRepository;

    @Transactional(readOnly = true)
    public List<ComplaintDTO> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintDTO> getComplaintsForResident(Long residentId) {
        return complaintRepository.findByResidentId(residentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintDTO> getComplaintsByUserId(Long userId) {
        return complaintRepository.findByResidentUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintDTO> getComplaintsByResidentOrUser(Long residentId, Long userId) {
        if (userId != null) {
            return getComplaintsByUserId(userId);
        }
        if (residentId != null) {
            return getComplaintsForResident(residentId);
        }
        return getAllComplaints();
    }

    @Transactional
    public ComplaintDTO createComplaint(ComplaintDTO dto) {
        Resident resident = null;
        if (dto.getResidentId() != null) {
            resident = residentRepository.findById(dto.getResidentId()).orElse(null);
            if (resident == null) {
                resident = residentRepository.findByUserId(dto.getResidentId()).orElse(null);
            }
        }
        if (resident == null && dto.getUserId() != null) {
            resident = residentRepository.findByUserId(dto.getUserId()).orElse(null);
        }
        if (resident == null && dto.getFlatId() != null) {
            resident = residentRepository.findAll().stream()
                    .filter(r -> r.getFlat() != null && r.getFlat().getId().equals(dto.getFlatId()))
                    .findFirst().orElse(null);
        }
        if (resident == null) {
            resident = residentRepository.findAll().stream().findFirst().orElse(null);
        }
        if (resident == null) {
            throw new ResourceNotFoundException("No active resident found in society database");
        }

        Complaint complaint = Complaint.builder()
                .resident(resident)
                .flat(resident.getFlat())
                .category(dto.getCategory())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .priority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM")
                .status("NEW")
                .build();

        Complaint saved = complaintRepository.save(complaint);

        // Record history
        ComplaintHistory history = ComplaintHistory.builder()
                .complaint(saved)
                .status("NEW")
                .remarks("Complaint raised by resident " + (resident.getUser() != null ? resident.getUser().getFullName() : ""))
                .build();
        complaintHistoryRepository.save(history);

        return mapToDTO(saved);
    }

    @Transactional
    public ComplaintDTO updateStatus(Long complaintId, String status, Long staffId, String remarks) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        complaint.setStatus(status);
        if (staffId != null) {
            StaffMember staff = staffMemberRepository.findById(staffId).orElse(null);
            complaint.setAssignedStaff(staff);
        }
        if (remarks != null) {
            complaint.setAdminRemarks(remarks);
        }

        Complaint saved = complaintRepository.save(complaint);

        ComplaintHistory history = ComplaintHistory.builder()
                .complaint(saved)
                .status(status)
                .remarks(remarks != null ? remarks : "Status updated to " + status)
                .build();
        complaintHistoryRepository.save(history);

        return mapToDTO(saved);
    }

    @Transactional
    public void deleteComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));
        complaintHistoryRepository.findByComplaintId(complaintId).forEach(complaintHistoryRepository::delete);
        complaintRepository.delete(complaint);
    }

    private ComplaintDTO mapToDTO(Complaint c) {
        return ComplaintDTO.builder()
                .id(c.getId())
                .residentId(c.getResident() != null ? c.getResident().getId() : null)
                .userId(c.getResident() != null && c.getResident().getUser() != null ? c.getResident().getUser().getId() : null)
                .residentName(c.getResident() != null && c.getResident().getUser() != null ? c.getResident().getUser().getFullName() : "N/A")
                .flatId(c.getFlat() != null ? c.getFlat().getId() : null)
                .wing(c.getFlat() != null ? c.getFlat().getWing() : "")
                .flatNumber(c.getFlat() != null ? c.getFlat().getFlatNumber() : "")
                .category(c.getCategory())
                .title(c.getTitle())
                .description(c.getDescription())
                .priority(c.getPriority())
                .status(c.getStatus())
                .assignedStaffId(c.getAssignedStaff() != null ? c.getAssignedStaff().getId() : null)
                .assignedStaffName(c.getAssignedStaff() != null ? c.getAssignedStaff().getName() : "Unassigned")
                .adminRemarks(c.getAdminRemarks())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
