package com.greenloop.listing;

import com.greenloop.model.Listing;
import com.greenloop.model.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Query("SELECT l FROM Listing l WHERE l.status = com.greenloop.model.ListingStatus.AVAILABLE")
    List<Listing> findAvailable();

    @Query("SELECT l FROM Listing l WHERE l.owner.id = :ownerId")
    List<Listing> findByOwnerId(@Param("ownerId") Long ownerId);

    List<Listing> findByStatus(ListingStatus status);

    @Query("SELECT l FROM Listing l WHERE l.status = :status AND l.pickupWindowEnd < :deadline")
    List<Listing> findExpiredListings(@Param("status") ListingStatus status,
            @Param("deadline") LocalDateTime deadline);

    long countByStatus(ListingStatus status);
}