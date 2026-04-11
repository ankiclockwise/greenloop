package com.greenloop.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Listing entities.
 *
 * Provides database access for listing-related queries, including status updates
 * and availability checks.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@Repository
public interface ListingRepository extends JpaRepository<Object, Long> {

    /**
     * Finds a listing by ID.
     *
     * @param id the listing ID
     * @return optional containing the listing, or empty if not found
     */
    Optional<?> findById(Long id);

    /**
     * Finds all available listings.
     *
     * @return list of listings with status AVAILABLE
     */
    @Query("SELECT l FROM Listing l WHERE l.status = 'AVAILABLE'")
    List<?> findAvailable();

    /**
     * Finds all listings by donor ID.
     *
     * @param donorId the donor user ID
     * @return list of listings donated by the user
     */
    @Query("SELECT l FROM Listing l WHERE l.donorId = :donorId")
    List<?> findByDonorId(@Param("donorId") Long donorId);

    /**
     * Finds listings by status.
     *
     * @param status the listing status
     * @return list of listings with the specified status
     */
    @Query("SELECT l FROM Listing l WHERE l.status = :status")
    List<?> findByStatus(@Param("status") String status);

    /**
     * Finds expired listings that should be archived.
     *
     * @param status the current listing status
     * @param deadline the expiration deadline
     * @return list of expired listings
     */
    @Query("SELECT l FROM Listing l WHERE l.status = :status AND l.pickupWindowEnd < :deadline")
    List<?> findExpiredListings(@Param("status") String status, @Param("deadline") LocalDateTime deadline);

    /**
     * Counts listings by status.
     *
     * @param status the listing status
     * @return the count of listings
     */
    @Query("SELECT COUNT(l) FROM Listing l WHERE l.status = :status")
    long countByStatus(@Param("status") String status);
}
