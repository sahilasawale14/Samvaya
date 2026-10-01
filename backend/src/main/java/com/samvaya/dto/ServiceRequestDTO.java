package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestDTO {
    private Long id;
    private Long residentId;
    private String residentName;
    private Long flatId;
    private String wing;
    private String flatNumber;
    private String serviceType;
    private LocalDate preferredDate;
    private String preferredSlot;
    private String description;
    private String status;
    private Long assignedStaffId;
    private String assignedStaffName;
    private LocalDateTime createdAt;
}
