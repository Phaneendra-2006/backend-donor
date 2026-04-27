package com.foodwaste.service;

import com.foodwaste.dto.FoodDonationRequest;
import com.foodwaste.dto.FoodDonationResponse;
import com.foodwaste.exception.ResourceNotFoundException;
import com.foodwaste.entity.FoodDonation;
import com.foodwaste.entity.User;
import com.foodwaste.repository.FoodDonationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FoodDonationService {

    private final FoodDonationRepository foodDonationRepository;

    public FoodDonationService(FoodDonationRepository foodDonationRepository) {
        this.foodDonationRepository = foodDonationRepository;
    }

    @Transactional
    public FoodDonationResponse addDonation(FoodDonationRequest request, User donor) {
        FoodDonation donation = new FoodDonation();
        donation.setFoodName(request.getFoodName());
        donation.setQuantity(request.getQuantity());
        donation.setFoodType(request.getFoodType());
        donation.setExpiryTime(request.getExpiryTime());
        donation.setLocation(request.getLocation());
        donation.setDescription(request.getDescription());
        donation.setImageUrl(request.getImageUrl());
        donation.setStatus(FoodDonation.DonationStatus.AVAILABLE);
        donation.setDonor(donor);

        FoodDonation savedDonation = foodDonationRepository.save(donation);
        return convertToResponse(savedDonation);
    }

    public List<FoodDonationResponse> getAllAvailableDonations() {
        return foodDonationRepository.findByStatus(FoodDonation.DonationStatus.AVAILABLE)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<FoodDonationResponse> getDonationsByDonor(Long donorId) {
        return foodDonationRepository.findByDonorIdOrderByCreatedAtDesc(donorId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public FoodDonationResponse getDonationById(Long id) {
        FoodDonation donation = foodDonationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + id));
        return convertToResponse(donation);
    }

    @Transactional
    public FoodDonationResponse updateDonationStatus(Long id, FoodDonation.DonationStatus status) {
        FoodDonation donation = foodDonationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + id));
        
        donation.setStatus(status);
        FoodDonation updatedDonation = foodDonationRepository.save(donation);
        return convertToResponse(updatedDonation);
    }

    private FoodDonationResponse convertToResponse(FoodDonation donation) {
        FoodDonationResponse response = new FoodDonationResponse();
        response.setId(donation.getId());
        response.setFoodName(donation.getFoodName());
        response.setFoodType(donation.getFoodType());
        response.setQuantity(donation.getQuantity());
        response.setExpiryTime(donation.getExpiryTime());
        response.setLocation(donation.getLocation());
        response.setDescription(donation.getDescription());
        
        // Convert filename to accessible URL
        String imageUrl = donation.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
            imageUrl = "/uploads/" + imageUrl;
        }
        response.setImageUrl(imageUrl);
        
        response.setStatus(donation.getStatus().name());
        response.setDonorId(donation.getDonor().getId());
        response.setDonorName(donation.getDonor().getName());
        response.setDonorEmail(donation.getDonor().getEmail());
        response.setCreatedAt(donation.getCreatedAt());
        return response;
    }
}
