package com.greenloop.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Object, Long> {

    Optional<Object> findById(Long id);

    @Query("SELECT l FROM Listing l WHERE l.status = 'AVAILABLE'")
    List<?> findAvailable();

    @Query("SELECT l FROM Listing l WHERE l.donorId = :donorId")
    List<?> findByDonorId(@Param("donorId") Long donorId);

    @Query("SELECT l FROM Listing l WHERE l.status = :status")
    List<?> findByStatus(@Param("status") String status);

    @Query("SELECT l FROM Listing l WHERE l.status = :status AND l.pickupWindowEnd < :deadline")
    List<?> findExpiredListings(@Param("status") String status, @Param("deadline") LocalDateTime deadline);

    @Query("SELECT COUNT(l) FROM Listing l WHERE l.status = :status")
    long countByStatus(@Param("status") String status);
}
