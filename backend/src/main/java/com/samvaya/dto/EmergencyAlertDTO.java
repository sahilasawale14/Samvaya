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
public class EmergencyAlertDTO {
    private Long id;
    private String category;
    private String title;
    private String message;
    private String location;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
}
