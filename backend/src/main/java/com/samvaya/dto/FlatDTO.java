package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatDTO {
    private Long id;
    private String wing;
    private String flatNumber;
    private Integer floorNumber;
    private String flatType; // '1BHK', '2BHK', '3BHK', etc.
    private String bhkType;
    private Double carpetAreaSqFt;
    private Double squareFeet;
    private Long residentId;
    private String status;
    private String currentResidentName;
    private String residentType;
}
