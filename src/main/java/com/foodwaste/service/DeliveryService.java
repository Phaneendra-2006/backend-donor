package com.foodwaste.service;

import com.foodwaste.exception.ResourceNotFoundException;
import com.foodwaste.exception.BadRequestException;
import com.foodwaste.entity.Delivery;
import com.foodwaste.entity.Request;
import com.foodwaste.repository.DeliveryRepository;
import com.foodwaste.repository.FoodDonationRepository;
import com.foodwaste.repository.RequestRepository;
import com.foodwaste.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RequestRepository requestRepository;
    private final FoodDonationRepository foodDonationRepository;
    private final EmailService emailService;
    private final JdbcTemplate jdbcTemplate;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           RequestRepository requestRepository,
                           FoodDonationRepository foodDonationRepository,
                           EmailService emailService,
                           JdbcTemplate jdbcTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.requestRepository = requestRepository;
        this.foodDonationRepository = foodDonationRepository;
        this.emailService = emailService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Delivery createDelivery(Request request) {
        return deliveryRepository.findByRequestId(request.getId())
                .orElseGet(() -> {
        Delivery delivery = new Delivery();
        delivery.setRequest(request);
        request.setDelivery(delivery);
        delivery.setPickupTime(LocalDateTime.now().plusHours(24));
        delivery.setDeliveryStatus(Delivery.DeliveryStatus.SCHEDULED);

        return deliveryRepository.save(delivery);
                });
    }

    @Transactional
    public void updateDeliveryStatus(Long deliveryId, Delivery.DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        applyStatusTransition(delivery, status);

        deliveryRepository.save(delivery);
    }

    public List<Delivery> getDeliveriesByNgo(Long ngoId) {
        ensureDeliveriesForApprovedRequestsByNgo(ngoId);
        return deliveryRepository.findByNgoId(ngoId);
    }

    public List<Delivery> getDeliveriesByDonor(Long donorId) {
        ensureDeliveriesForApprovedRequestsByDonor(donorId);
        return deliveryRepository.findByDonorId(donorId);
    }

    public List<Delivery> getAllDeliveries() {
        ensureDeliveriesForAllApprovedRequests();
        return deliveryRepository.findAll();
    }

    @Transactional
    public long clearAllDeliveries() {
        long deletedCount = deliveryRepository.count();
        deliveryRepository.deleteAllInBatch();
        jdbcTemplate.execute("ALTER TABLE deliveries AUTO_INCREMENT = 1");
        return deletedCount;
    }

    @Transactional
    public void clearAllOperationalData() {
        deliveryRepository.deleteAllInBatch();
        requestRepository.deleteAllInBatch();
        foodDonationRepository.deleteAllInBatch();

        jdbcTemplate.execute("ALTER TABLE deliveries AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE requests AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE food_donations AUTO_INCREMENT = 1");
    }

    @Transactional
    public void ensureDeliveriesForApprovedRequestsByNgo(Long ngoId) {
        List<Request> requests = requestRepository.findApprovedWithoutDeliveryByNgoId(ngoId);
        requests.forEach(this::createDelivery);
    }

    @Transactional
    public void ensureDeliveriesForApprovedRequestsByDonor(Long donorId) {
        List<Request> requests = requestRepository.findApprovedWithoutDeliveryByDonorId(donorId);
        requests.forEach(this::createDelivery);
    }

    @Transactional
    public void ensureDeliveriesForAllApprovedRequests() {
        List<Request> requests = requestRepository.findAllApprovedWithoutDelivery();
        requests.forEach(this::createDelivery);
    }

    @Transactional
    public Delivery acceptApprovedRequest(Long requestId, Long ngoId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        if (!request.getNgo().getId().equals(ngoId)) {
            throw new BadRequestException("You are not authorized to accept this request");
        }

        if (request.getStatus() != Request.RequestStatus.APPROVED) {
            throw new BadRequestException("Only approved requests can be accepted for pickup");
        }

        Delivery delivery = createDelivery(request);
        if (delivery.getDeliveryStatus() == Delivery.DeliveryStatus.SCHEDULED) {
            applyStatusTransition(delivery, Delivery.DeliveryStatus.IN_TRANSIT);
            delivery = deliveryRepository.save(delivery);
        }
        return delivery;
    }

    @Transactional
    public Delivery rejectApprovedRequest(Long requestId, Long ngoId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        if (!request.getNgo().getId().equals(ngoId)) {
            throw new BadRequestException("You are not authorized to reject this request");
        }

        if (request.getStatus() != Request.RequestStatus.APPROVED) {
            throw new BadRequestException("Only approved requests can be rejected here");
        }

        Delivery delivery = createDelivery(request);
        applyStatusTransition(delivery, Delivery.DeliveryStatus.CANCELLED);

        requestRepository.save(request);
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery acceptDelivery(Long deliveryId, Long ngoId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        if (!delivery.getRequest().getNgo().getId().equals(ngoId)) {
            throw new BadRequestException("You are not authorized to accept this delivery");
        }
        
        if (!delivery.getDeliveryStatus().equals(Delivery.DeliveryStatus.SCHEDULED)) {
            throw new BadRequestException("Can only accept scheduled deliveries");
        }

        applyStatusTransition(delivery, Delivery.DeliveryStatus.IN_TRANSIT);
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery rejectDelivery(Long deliveryId, Long ngoId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        if (!delivery.getRequest().getNgo().getId().equals(ngoId)) {
            throw new BadRequestException("You are not authorized to reject this delivery");
        }
        
        if (!delivery.getDeliveryStatus().equals(Delivery.DeliveryStatus.SCHEDULED)) {
            throw new BadRequestException("Can only reject scheduled deliveries");
        }

        applyStatusTransition(delivery, Delivery.DeliveryStatus.CANCELLED);

        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery completeDelivery(Long deliveryId, Long ngoId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        if (!delivery.getRequest().getNgo().getId().equals(ngoId)) {
            throw new BadRequestException("You are not authorized to complete this delivery");
        }

        if (!delivery.getDeliveryStatus().equals(Delivery.DeliveryStatus.IN_TRANSIT)) {
            throw new BadRequestException("Only in-transit deliveries can be marked as delivered");
        }

        applyStatusTransition(delivery, Delivery.DeliveryStatus.DELIVERED);
        Delivery savedDelivery = deliveryRepository.save(delivery);

        String donorEmail = savedDelivery.getRequest().getDonation().getDonor().getEmail();
        String ngoEmail = savedDelivery.getRequest().getNgo().getEmail();
        String foodName = savedDelivery.getRequest().getDonation().getFoodName();
        emailService.sendDeliveryConfirmation(donorEmail, ngoEmail, foodName);

        return savedDelivery;
    }

    private void applyStatusTransition(Delivery delivery, Delivery.DeliveryStatus status) {
        delivery.setDeliveryStatus(status);

        com.foodwaste.entity.FoodDonation donation = delivery.getRequest().getDonation();
        Double currentQuantity = donation.getQuantity() == null ? 0.0 : donation.getQuantity();
        Double requestedQuantity = delivery.getRequest().getRequiredQuantity() == null
                ? 0.0
                : delivery.getRequest().getRequiredQuantity();

        if (Delivery.DeliveryStatus.IN_TRANSIT.equals(status)) {
            delivery.getRequest().setStatus(Request.RequestStatus.APPROVED);
            donation.setStatus(currentQuantity > 0
                    ? com.foodwaste.entity.FoodDonation.DonationStatus.AVAILABLE
                    : com.foodwaste.entity.FoodDonation.DonationStatus.REQUESTED);
            return;
        }

        if (Delivery.DeliveryStatus.DELIVERED.equals(status)) {
            delivery.setDeliveryTime(LocalDateTime.now());
            delivery.getRequest().setStatus(Request.RequestStatus.COMPLETED);
            donation.setStatus(currentQuantity > 0
                    ? com.foodwaste.entity.FoodDonation.DonationStatus.AVAILABLE
                    : com.foodwaste.entity.FoodDonation.DonationStatus.PICKED);
            return;
        }

        if (Delivery.DeliveryStatus.CANCELLED.equals(status)) {
            delivery.setDeliveryTime(null);
            delivery.getRequest().setStatus(Request.RequestStatus.REJECTED);
            donation.setQuantity(currentQuantity + requestedQuantity);
            donation.setStatus(com.foodwaste.entity.FoodDonation.DonationStatus.AVAILABLE);
            return;
        }

        delivery.getRequest().setStatus(Request.RequestStatus.APPROVED);
        donation.setStatus(currentQuantity > 0
                ? com.foodwaste.entity.FoodDonation.DonationStatus.AVAILABLE
                : com.foodwaste.entity.FoodDonation.DonationStatus.REQUESTED);
    }
}
