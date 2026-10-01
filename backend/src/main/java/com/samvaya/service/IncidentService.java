package com.samvaya.service;

import com.samvaya.dto.IncidentDTO;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.Incident;
import com.samvaya.repository.FlatRepository;
import com.samvaya.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final FlatRepository flatRepository;

    @Transactional(readOnly = true)
    public List<IncidentDTO> getAllIncidents() {
        return incidentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public IncidentDTO reportIncident(IncidentDTO dto) {
        Incident incident = Incident.builder()
                .incidentType(dto.getIncidentType())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .priority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM")
                .status("REPORTED")
                .resolutionNotes(dto.getResolutionNotes())
                .build();

        return mapToDTO(incidentRepository.save(incident));
    }

    @Transactional
    public IncidentDTO updateIncidentStatus(Long id, String status, String resolutionNotes) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

        incident.setStatus(status);
        if (resolutionNotes != null) {
            incident.setResolutionNotes(resolutionNotes);
        }
        return mapToDTO(incidentRepository.save(incident));
    }

    private IncidentDTO mapToDTO(Incident i) {
        return IncidentDTO.builder()
                .id(i.getId())
                .incidentType(i.getIncidentType())
                .description(i.getDescription())
                .location(i.getLocation())
                .priority(i.getPriority())
                .status(i.getStatus())
                .reportedByGuardName(i.getReportedByGuard() != null ? i.getReportedByGuard().getFullName() : "Security Gate 1")
                .relatedFlatNumber(i.getRelatedFlat() != null ? "Wing " + i.getRelatedFlat().getWing() + "-" + i.getRelatedFlat().getFlatNumber() : "-")
                .resolutionNotes(i.getResolutionNotes())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
