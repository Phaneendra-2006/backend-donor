package com.foodwaste.controller;

import com.foodwaste.dto.FoodDonationRequest;
import com.foodwaste.dto.FoodDonationResponse;
import com.foodwaste.dto.RequestResponse;
import com.foodwaste.entity.Delivery;
import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.service.DeliveryService;
import com.foodwaste.service.FoodDonationService;
import com.foodwaste.service.RequestService;
import com.foodwaste.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/donor")
public class DonorController {
    
    private final FoodDonationService foodDonationService;
    private final RequestService requestService;
    private final DeliveryService deliveryService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public DonorController(
            FoodDonationService foodDonationService,
            RequestService requestService,
            DeliveryService deliveryService,
            UserRepository userRepository,
            FileStorageService fileStorageService) {
        this.foodDonationService = foodDonationService;
        this.requestService = requestService;
        this.deliveryService = deliveryService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }
    
    @PostMapping("/add-food")
    public ResponseEntity<FoodDonationResponse> addFood(
            @RequestParam("foodName") String foodName,
            @RequestParam("quantity") Double quantity,
            @RequestParam("foodType") String foodType,
            @RequestParam(value = "expiryTime", required = false) String expiryTime,
            @RequestParam("location") String location,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication) {
        
        User donor = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Store the image file and get the filename
        String imageFileName = fileStorageService.storeFile(image);
        
        // Create request DTO
        FoodDonationRequest request = new FoodDonationRequest();
        request.setFoodName(foodName);
        request.setQuantity(quantity);
        request.setFoodType(foodType);
        request.setLocation(location);
        request.setDescription(description);
        request.setImageUrl(imageFileName); // Store filename instead of URL
        
        // Parse expiryTime if provided
        if (expiryTime != null && !expiryTime.isEmpty()) {
            try {
                request.setExpiryTime(LocalDateTime.parse(expiryTime));
            } catch (Exception e) {
                // Handle parse error if needed
            }
        }
        
        return ResponseEntity.ok(foodDonationService.addDonation(request, donor));
    }
    
    @GetMapping("/my-donations")
    public ResponseEntity<List<FoodDonationResponse>> getMyDonations(Authentication authentication) {
        User donor = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(foodDonationService.getDonationsByDonor(donor.getId()));
    }
    
    @GetMapping("/requests")
    public ResponseEntity<List<RequestResponse>> getRequests(Authentication authentication) {
        User donor = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(requestService.getRequestsForDonor(donor.getId()));
    }

    @GetMapping("/deliveries")
    public ResponseEntity<List<Delivery>> getDeliveries(Authentication authentication) {
        User donor = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.getDeliveriesByDonor(donor.getId()));
    }
    
    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<RequestResponse> approveRequest(
            @PathVariable Long id,
            Authentication authentication) {
        User donor = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(requestService.approveRequest(id, donor.getId()));
    }
    
    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<RequestResponse> rejectRequest(
            @PathVariable Long id,
            Authentication authentication) {
        User donor = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(requestService.rejectRequest(id, donor.getId()));
    }
}
