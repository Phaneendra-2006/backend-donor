package com.foodwaste.service;

import com.foodwaste.dto.AnalyticsResponse;
import com.foodwaste.entity.Delivery;
import com.foodwaste.entity.FoodDonation;
import com.foodwaste.repository.DeliveryRepository;
import com.foodwaste.repository.FoodDonationRepository;
import com.foodwaste.repository.RequestRepository;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    
    private final FoodDonationRepository foodDonationRepository;
    private final RequestRepository requestRepository;
    private final DeliveryRepository deliveryRepository;
    
    public AnalyticsService(FoodDonationRepository foodDonationRepository, 
                           RequestRepository requestRepository,
                           DeliveryRepository deliveryRepository) {
        this.foodDonationRepository = foodDonationRepository;
        this.requestRepository = requestRepository;
        this.deliveryRepository = deliveryRepository;
    }
    
    public AnalyticsResponse getAnalytics() {
        AnalyticsResponse response = new AnalyticsResponse();
        
        // Get all donations
        List<FoodDonation> allDonations = foodDonationRepository.findAll();
        response.setTotalDonations((long) allDonations.size());

        Long activeDonations = allDonations.stream()
                .filter(d -> d.getStatus() == FoodDonation.DonationStatus.AVAILABLE
                        || d.getStatus() == FoodDonation.DonationStatus.REQUESTED)
                .count();
        response.setActiveDonations(activeDonations);
        
        // Calculate total food saved (sum of all quantities)
        Double totalFoodSaved = allDonations.stream()
                .mapToDouble(FoodDonation::getQuantity)
                .sum();
        response.setTotalFoodSaved(totalFoodSaved);
        
        // Get total requests
        Long totalRequests = requestRepository.count();
        response.setTotalRequests(totalRequests);
        
        // Get completed deliveries
        Long completedDeliveries = deliveryRepository.findAll().stream()
                .filter(d -> d.getDeliveryStatus() == Delivery.DeliveryStatus.DELIVERED)
                .count();
        response.setCompletedDeliveries(completedDeliveries);
        
        // Group donations by location
        Map<String, Long> donationsByLocation = allDonations.stream()
                .filter(donation -> donation.getLocation() != null && !donation.getLocation().isBlank())
                .collect(Collectors.groupingBy(
                        FoodDonation::getLocation,
                        Collectors.counting()
                ));
        response.setDonationsByLocation(donationsByLocation);

        Map<String, Long> dailyStats = allDonations.stream()
                .filter(donation -> donation.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        donation -> donation.getCreatedAt().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<AnalyticsResponse.DailyDonationPoint> dailyDonations = dailyStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AnalyticsResponse.DailyDonationPoint(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        response.setDailyDonations(dailyDonations);

        List<AnalyticsResponse.LocationDonationPoint> donationsByLocationList = donationsByLocation.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new AnalyticsResponse.LocationDonationPoint(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        response.setDonationsByLocationList(donationsByLocationList);
        
        // Group donations by month
        Map<Integer, Long> monthlyStats = allDonations.stream()
                .filter(donation -> donation.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        donation -> donation.getCreatedAt().getMonthValue(),
                        Collectors.counting()
                ));
        response.setMonthlyStats(monthlyStats);

        List<AnalyticsResponse.MonthlyDonationPoint> monthlyDonations = monthlyStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AnalyticsResponse.MonthlyDonationPoint(
                        Month.of(entry.getKey()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        entry.getValue()))
                .collect(Collectors.toList());
        response.setMonthlyDonations(monthlyDonations);

        Map<String, Long> donationsByTypeMap = allDonations.stream()
                .collect(Collectors.groupingBy(
                        donation -> donation.getFoodType() == null || donation.getFoodType().isBlank()
                                ? "Unknown"
                                : donation.getFoodType(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<AnalyticsResponse.TypeDonationPoint> donationsByType = donationsByTypeMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new AnalyticsResponse.TypeDonationPoint(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        response.setDonationsByType(donationsByType);

        List<AnalyticsResponse.DonationItemPoint> recentDonationItems = allDonations.stream()
                .sorted(Comparator.comparing(FoodDonation::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .map(donation -> {
                    String imageUrl = donation.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http") && !imageUrl.startsWith("/")) {
                        imageUrl = "/uploads/" + imageUrl;
                    }
                    return new AnalyticsResponse.DonationItemPoint(
                            donation.getId(),
                            donation.getFoodName(),
                            donation.getFoodType(),
                            donation.getLocation(),
                            donation.getQuantity(),
                            donation.getStatus() != null ? donation.getStatus().name() : "UNKNOWN",
                            imageUrl,
                            donation.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
        response.setRecentDonationItems(recentDonationItems);
        
        return response;
    }
}
