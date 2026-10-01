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
public class ComplaintDTO {
    private Long id;
    private Long residentId;
    private Long userId;
    private String residentName;
    private Long flatId;
    private String wing;
    private String flatNumber;
    private String category;
    private String title;
    private String description;
    private String priority;
    private String status;
    private Long assignedStaffId;
    private String assignedStaffName;
    private String adminRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
