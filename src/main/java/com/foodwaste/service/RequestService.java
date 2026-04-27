package com.foodwaste.service;

import com.foodwaste.dto.RequestFoodDto;
import com.foodwaste.dto.RequestResponse;
import com.foodwaste.entity.FoodDonation;
import com.foodwaste.entity.Request;
import com.foodwaste.entity.User;
import com.foodwaste.exception.BadRequestException;
import com.foodwaste.exception.ResourceNotFoundException;
import com.foodwaste.repository.FoodDonationRepository;
import com.foodwaste.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final FoodDonationRepository foodDonationRepository;
    private final DeliveryService deliveryService;
    private final EmailService emailService;

    public RequestService(RequestRepository requestRepository,
                         FoodDonationRepository foodDonationRepository,
                         DeliveryService deliveryService,
                         EmailService emailService) {
        this.requestRepository = requestRepository;
        this.foodDonationRepository = foodDonationRepository;
        this.deliveryService = deliveryService;
        this.emailService = emailService;
    }

    @Transactional
    public RequestResponse createRequest(RequestFoodDto dto, User ngo) {
        FoodDonation donation = foodDonationRepository.findById(dto.getDonationId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + dto.getDonationId()));

        if (donation.getStatus() != FoodDonation.DonationStatus.AVAILABLE) {
            throw new BadRequestException("This donation is no longer available");
        }

        Double availableQuantity = donation.getQuantity();
        Double requestedQuantity = dto.getRequiredQuantity();

        if (availableQuantity == null || availableQuantity <= 0) {
            throw new BadRequestException("This donation has no available quantity");
        }

        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new BadRequestException("Requested quantity must be greater than 0");
        }

        if (requestedQuantity > availableQuantity) {
            throw new BadRequestException("Requested quantity cannot be greater than available quantity (" + availableQuantity + ")");
        }

        // Reserve quantity immediately so other NGOs see updated availability.
        double remainingQuantity = availableQuantity - requestedQuantity;
        donation.setQuantity(remainingQuantity);
        donation.setStatus(remainingQuantity > 0
            ? FoodDonation.DonationStatus.AVAILABLE
            : FoodDonation.DonationStatus.REQUESTED);
        foodDonationRepository.save(donation);

        Request request = new Request();
        request.setDonation(donation);
        request.setNgo(ngo);
        request.setRequiredQuantity(requestedQuantity);
        request.setNotes(dto.getNotes());
        request.setStatus(Request.RequestStatus.PENDING);

        Request savedRequest = requestRepository.save(request);
        
        // Send notification
        emailService.sendRequestNotification(
            donation.getDonor().getEmail(),
            ngo.getName(),
            donation.getFoodName()
        );
        
        return convertToResponse(savedRequest);
    }

    public List<RequestResponse> getRequestsByNgo(Long ngoId) {
        return requestRepository.findByNgoIdOrderByRequestTimeDesc(ngoId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<RequestResponse> getRequestsForDonor(Long donorId) {
        return requestRepository.findRequestsForDonor(donorId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RequestResponse approveRequest(Long requestId, Long donorId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        if (!request.getDonation().getDonor().getId().equals(donorId)) {
            throw new BadRequestException("You are not authorized to approve this request");
        }

        if (request.getStatus() != Request.RequestStatus.PENDING) {
            throw new BadRequestException("This request has already been processed");
        }

        request.setStatus(Request.RequestStatus.APPROVED);

        FoodDonation donation = request.getDonation();
        donation.setStatus(donation.getQuantity() != null && donation.getQuantity() > 0
                ? FoodDonation.DonationStatus.AVAILABLE
                : FoodDonation.DonationStatus.REQUESTED);
        foodDonationRepository.save(donation);

        deliveryService.createDelivery(request);

        emailService.sendApprovalNotification(
            request.getNgo().getEmail(),
            donation.getDonor().getName(),
            donation.getFoodName()
        );

        Request updatedRequest = requestRepository.save(request);
        return convertToResponse(updatedRequest);
    }

    @Transactional
    public RequestResponse rejectRequest(Long requestId, Long donorId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        if (!request.getDonation().getDonor().getId().equals(donorId)) {
            throw new BadRequestException("You are not authorized to reject this request");
        }

        if (request.getStatus() != Request.RequestStatus.PENDING) {
            throw new BadRequestException("This request has already been processed");
        }

        // Release previously reserved quantity when request is rejected.
        FoodDonation donation = request.getDonation();
        double currentQuantity = donation.getQuantity() == null ? 0.0 : donation.getQuantity();
        double requestQuantity = request.getRequiredQuantity() == null ? 0.0 : request.getRequiredQuantity();
        donation.setQuantity(currentQuantity + requestQuantity);
        donation.setStatus(FoodDonation.DonationStatus.AVAILABLE);
        foodDonationRepository.save(donation);

        request.setStatus(Request.RequestStatus.REJECTED);
        Request updatedRequest = requestRepository.save(request);
        return convertToResponse(updatedRequest);
    }

    private RequestResponse convertToResponse(Request request) {
        RequestResponse response = new RequestResponse();
        response.setId(request.getId());
        response.setDonationId(request.getDonation().getId());
        response.setFoodName(request.getDonation().getFoodName());
        String imageUrl = request.getDonation().getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http") && !imageUrl.startsWith("/")) {
            imageUrl = "/uploads/" + imageUrl;
        }
        response.setImageUrl(imageUrl);
        response.setNgoName(request.getNgo().getName());
        response.setNgoId(request.getNgo().getId());
        response.setStatus(request.getStatus().name());
        response.setRequestTime(request.getRequestTime());
        response.setAvailableQuantity(request.getDonation().getQuantity());
        response.setRequiredQuantity(request.getRequiredQuantity());
        response.setNotes(request.getNotes());
        return response;
    }
}
