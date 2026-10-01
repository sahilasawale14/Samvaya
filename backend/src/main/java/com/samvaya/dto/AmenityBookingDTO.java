package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityBookingDTO {
    private Long id;
    private Long amenityId;
    private String amenityName;
    private Long residentId;
    private String residentName;
    private Long flatId;
    private String wing;
    private String flatNumber;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer numberOfGuests;
    private BigDecimal totalAmount;
    private String status;
}
