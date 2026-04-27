package com.foodwaste.repository;

import com.foodwaste.entity.FoodDonation;
import com.foodwaste.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodDonationRepository extends JpaRepository<FoodDonation, Long> {
    
    List<FoodDonation> findByDonor(User donor);
    
    List<FoodDonation> findByStatus(FoodDonation.DonationStatus status);
    
    List<FoodDonation> findByLocation(String location);
    
    List<FoodDonation> findByFoodType(String foodType);
    
    @Query("SELECT f FROM FoodDonation f WHERE f.status = 'AVAILABLE' AND f.expiryTime > :currentTime")
    List<FoodDonation> findAvailableFoodNotExpired(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(f) FROM FoodDonation f")
    Long countTotalDonations();
    
    @Query("SELECT SUM(f.quantity) FROM FoodDonation f WHERE f.status = 'PICKED'")
    Double totalFoodSaved();
    
    @Query("SELECT f.location, COUNT(f) FROM FoodDonation f GROUP BY f.location")
    List<Object[]> countDonationsByLocation();
    
    @Query("SELECT MONTH(f.createdAt), COUNT(f) FROM FoodDonation f WHERE YEAR(f.createdAt) = :year GROUP BY MONTH(f.createdAt)")
    List<Object[]> monthlyDonationStats(@Param("year") int year);
    
    @Query("SELECT f FROM FoodDonation f WHERE f.donor.id = :donorId ORDER BY f.createdAt DESC")
    List<FoodDonation> findByDonorIdOrderByCreatedAtDesc(@Param("donorId") Long donorId);
}
