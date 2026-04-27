package com.foodwaste.controller;

import com.foodwaste.dto.FoodDonationResponse;
import com.foodwaste.dto.RequestFoodDto;
import com.foodwaste.dto.RequestResponse;
import com.foodwaste.entity.Delivery;
import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.service.DeliveryService;
import com.foodwaste.service.FoodDonationService;
import com.foodwaste.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ngo")
public class NgoController {

    private final FoodDonationService foodDonationService;
    private final RequestService requestService;
    private final DeliveryService deliveryService;
    private final UserRepository userRepository;

    public NgoController(FoodDonationService foodDonationService,
                       RequestService requestService,
                       DeliveryService deliveryService,
                       UserRepository userRepository) {
        this.foodDonationService = foodDonationService;
        this.requestService = requestService;
        this.deliveryService = deliveryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/available-food")
    public ResponseEntity<List<FoodDonationResponse>> getAvailableFood() {      
        return ResponseEntity.ok(foodDonationService.getAllAvailableDonations());
    }

    @PostMapping("/request-food")
    public ResponseEntity<RequestResponse> requestFood(
            @Valid @RequestBody RequestFoodDto request,
            Authentication authentication) {
        User ngo = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));     
        return ResponseEntity.ok(requestService.createRequest(request, ngo));   
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<RequestResponse>> getMyRequests(Authentication authentication) {
        User ngo = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));     
        return ResponseEntity.ok(requestService.getRequestsByNgo(ngo.getId())); 
    }

        @PutMapping("/requests/{id}/accept")
        public ResponseEntity<Delivery> acceptApprovedRequest(
            @PathVariable Long id,
            Authentication authentication) {
        User ngo = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.acceptApprovedRequest(id, ngo.getId()));
        }

        @PutMapping("/requests/{id}/reject")
        public ResponseEntity<Delivery> rejectApprovedRequest(
            @PathVariable Long id,
            Authentication authentication) {
        User ngo = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.rejectApprovedRequest(id, ngo.getId()));
        }

    @GetMapping("/deliveries")
    public ResponseEntity<List<Delivery>> getDeliveries(Authentication authentication) {
        User ngo = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));     
        return ResponseEntity.ok(deliveryService.getDeliveriesByNgo(ngo.getId()));
    }

    @PutMapping("/deliveries/{id}/accept")
    public ResponseEntity<Delivery> acceptDelivery(
            @PathVariable Long id,
            Authentication authentication) {
        User ngo = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));     
        return ResponseEntity.ok(deliveryService.acceptDelivery(id, ngo.getId()));
    }

    @PutMapping("/deliveries/{id}/reject")
    public ResponseEntity<Delivery> rejectDelivery(
            @PathVariable Long id,
            Authentication authentication) {
        User ngo = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));     
        return ResponseEntity.ok(deliveryService.rejectDelivery(id, ngo.getId()));
    }

    @PutMapping("/deliveries/{id}/complete")
    public ResponseEntity<Delivery> completeDelivery(
            @PathVariable Long id,
            Authentication authentication) {
        User ngo = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(deliveryService.completeDelivery(id, ngo.getId()));
    }
}
