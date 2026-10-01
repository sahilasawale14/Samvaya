package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityDTO {
    private Long id;
    private String name;
    private String description;
    private Integer capacity;
    private LocalTime openTime;
    private LocalTime closeTime;
    private BigDecimal hourlyRate;
    private Boolean isActive;
}
