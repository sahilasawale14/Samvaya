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
    private Long currentResidentId;
    private String status;
    private String occupancyStatus;
    private String currentResidentName;
    private String residentType;

    public Long getCurrentResidentId() {
        return currentResidentId != null ? currentResidentId : residentId;
    }

    public void setCurrentResidentId(Long currentResidentId) {
        this.currentResidentId = currentResidentId;
        if (this.residentId == null) {
            this.residentId = currentResidentId;
        }
    }
}
