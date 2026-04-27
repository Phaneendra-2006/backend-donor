package com.foodwaste.controller;

import com.foodwaste.entity.Delivery;
import com.foodwaste.entity.User;
import com.foodwaste.repository.FoodDonationRepository;
import com.foodwaste.repository.RequestRepository;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private final UserRepository userRepository;
    private final FoodDonationRepository foodDonationRepository;
    private final RequestRepository requestRepository;
    private final DeliveryService deliveryService;

    public AdminController(UserRepository userRepository,
                          FoodDonationRepository foodDonationRepository,
                          RequestRepository requestRepository,
                          DeliveryService deliveryService) {
        this.userRepository = userRepository;
        this.foodDonationRepository = foodDonationRepository;
        this.requestRepository = requestRepository;
        this.deliveryService = deliveryService;
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @PutMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(User.UserStatus.valueOf(status));
        return ResponseEntity.ok(userRepository.save(user));
    }
    
    @DeleteMapping("/donations/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
        foodDonationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalDonations", foodDonationRepository.count());
        stats.put("totalRequests", requestRepository.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/deliveries")
    public ResponseEntity<List<Delivery>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @DeleteMapping("/deliveries")
    public ResponseEntity<Map<String, Object>> clearAllDeliveries() {
        long deletedCount = deliveryService.clearAllDeliveries();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All deliveries cleared successfully");
        response.put("deletedCount", deletedCount);
        response.put("nextDeliveryId", 1);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reset-data")
    public ResponseEntity<Map<String, Object>> resetOperationalData() {
        deliveryService.clearAllOperationalData();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All deliveries, requests, and food donations cleared successfully");
        response.put("nextDeliveryId", 1);
        response.put("nextRequestId", 1);
        response.put("nextFoodDonationId", 1);

        return ResponseEntity.ok(response);
    }
}
