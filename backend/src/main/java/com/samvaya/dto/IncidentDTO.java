package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {
    private Long id;
    private String incidentType;
    private String description;
    private String location;
    private String priority;
    private String status;
    private String reportedByGuardName;
    private String relatedFlatNumber;
    private String resolutionNotes;
    private LocalDateTime createdAt;
}
