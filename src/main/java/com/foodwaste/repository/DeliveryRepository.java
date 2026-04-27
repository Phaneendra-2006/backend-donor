package com.foodwaste.repository;

import com.foodwaste.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
    Optional<Delivery> findByRequestId(Long requestId);
    
    List<Delivery> findByDeliveryStatus(Delivery.DeliveryStatus deliveryStatus);
    
    @Query("SELECT d FROM Delivery d WHERE d.request.ngo.id = :ngoId ORDER BY d.pickupTime DESC")
    List<Delivery> findByNgoId(@Param("ngoId") Long ngoId);
    
    @Query("SELECT d FROM Delivery d WHERE d.request.donation.donor.id = :donorId ORDER BY d.pickupTime DESC")
    List<Delivery> findByDonorId(@Param("donorId") Long donorId);
    
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.deliveryStatus = 'DELIVERED'")
    Long countCompletedDeliveries();
}
