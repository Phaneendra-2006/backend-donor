package com.foodwaste.repository;

import com.foodwaste.entity.Request;
import com.foodwaste.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    
    List<Request> findByNgo(User ngo);
    
    List<Request> findByStatus(Request.RequestStatus status);
    
    List<Request> findByDonationId(Long donationId);
    
    @Query("SELECT r FROM Request r WHERE r.donation.donor.id = :donorId ORDER BY r.requestTime DESC")
    List<Request> findRequestsForDonor(@Param("donorId") Long donorId);
    
    @Query("SELECT r FROM Request r WHERE r.ngo.id = :ngoId ORDER BY r.requestTime DESC")
    List<Request> findByNgoIdOrderByRequestTimeDesc(@Param("ngoId") Long ngoId);
    
    @Query("SELECT COUNT(r) FROM Request r WHERE r.status = :status")
    Long countByStatus(Request.RequestStatus status);
    
    @Query("SELECT r FROM Request r WHERE r.donation.id = :donationId AND r.status = 'PENDING'")
    List<Request> findPendingRequestsForDonation(@Param("donationId") Long donationId);

    @Query("SELECT r FROM Request r LEFT JOIN r.delivery d WHERE r.status = 'APPROVED' AND r.ngo.id = :ngoId AND d IS NULL")
    List<Request> findApprovedWithoutDeliveryByNgoId(@Param("ngoId") Long ngoId);

    @Query("SELECT r FROM Request r LEFT JOIN r.delivery d WHERE r.status = 'APPROVED' AND r.donation.donor.id = :donorId AND d IS NULL")
    List<Request> findApprovedWithoutDeliveryByDonorId(@Param("donorId") Long donorId);

    @Query("SELECT r FROM Request r LEFT JOIN r.delivery d WHERE r.status = 'APPROVED' AND d IS NULL")
    List<Request> findAllApprovedWithoutDelivery();
}
