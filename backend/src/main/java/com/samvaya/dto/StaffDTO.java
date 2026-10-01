package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {
    private Long id;
    private String name;
    private String phone;
    private String emergencyContact;
    private String designation;
    private LocalDate joiningDate;
    private BigDecimal salary;
    private String assignedArea;
    private String status;
    private String todayAttendance;
}
