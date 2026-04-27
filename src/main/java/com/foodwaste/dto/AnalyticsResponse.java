package com.foodwaste.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private Long totalDonations;
    private Long activeDonations;
    private Double totalFoodSaved;
    private Long totalRequests;
    private Long completedDeliveries;

    private List<DailyDonationPoint> dailyDonations;
    private List<MonthlyDonationPoint> monthlyDonations;
    private List<LocationDonationPoint> donationsByLocationList;
    private List<TypeDonationPoint> donationsByType;
    private List<DonationItemPoint> recentDonationItems;

    // Legacy map fields retained for compatibility
    private Map<String, Long> donationsByLocation;
    private Map<Integer, Long> monthlyStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DonationItemPoint {
        private Long id;
        private String foodName;
        private String foodType;
        private String location;
        private Double quantity;
        private String status;
        private String imageUrl;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyDonationPoint {
        private String day;
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyDonationPoint {
        private String month;
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDonationPoint {
        private String location;
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeDonationPoint {
        private String foodType;
        private Long count;
    }
}