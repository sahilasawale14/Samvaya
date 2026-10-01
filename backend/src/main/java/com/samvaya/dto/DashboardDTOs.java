package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

public class DashboardDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminDashboardDTO {
        private Long totalFlats;
        private Long occupiedFlats;
        private Long vacantFlats;
        private Long totalResidents;
        private Long totalOwners;
        private Long totalTenants;
        private Long totalStaff;
        private Long activeSecurityStaff;
        private Long pendingComplaints;
        private Long resolvedComplaints;
        private Long upcomingAmenityBookings;
        private Long activeIncidents;
        private BigDecimal collectedMaintenance;
        private BigDecimal pendingMaintenance;

        // Dynamic Parking Metrics
        private Long totalParkingSlots;
        private Long occupiedParkingSlots;
        private Long availableParkingSlots;
        private Long availableTwoWheelerSlots;
        private Long availableFourWheelerSlots;
        private Long occupiedTwoWheelerSlots;
        private Long occupiedFourWheelerSlots;

        private List<NoticeDTO> recentNotices;
        private List<ComplaintDTO> recentComplaints;
        private List<ActivityLogDTO> recentActivities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResidentDashboardDTO {
        private Long residentId;
        private String residentName;
        private String residentType; // 'OWNER' or 'TENANT'
        private String flatNumber;
        private String wing;
        private String bhkType;
        private Long expectedVisitorsCount;
        private Long activeDeliveriesCount;
        private Long pendingComplaintsCount;
        private BigDecimal pendingMaintenanceAmount;
        private Long upcomingBookingsCount;
        private List<VisitorDTO> upcomingVisitors;
        private List<DeliveryDTO> activeDeliveries;
        private List<NoticeDTO> recentNotices;
        private List<NotificationDTO> recentNotifications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityDashboardDTO {
        private Long expectedVisitorsToday;
        private Long visitorsCurrentlyInside;
        private Long expectedDeliveriesToday;
        private Long deliveriesPendingAtGate;
        private Long temporaryWorkersInside;
        private Long activeIncidentsCount;
        private Long staffPresentToday;

        // Dynamic Parking Metrics for Security Command
        private Long totalParkingSlots;
        private Long occupiedParkingSlots;
        private Long availableParkingSlots;
        private Long availableTwoWheelerSlots;
        private Long availableFourWheelerSlots;
        private Long occupiedTwoWheelerSlots;
        private Long occupiedFourWheelerSlots;

        private List<VisitorDTO> recentGateVisitors;
        private List<DeliveryDTO> recentGateDeliveries;
        private List<IncidentDTO> recentIncidents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLogDTO {
        private Long id;
        private String adminName;
        private String action;
        private String module;
        private String description;
        private String timestamp;
    }
}
