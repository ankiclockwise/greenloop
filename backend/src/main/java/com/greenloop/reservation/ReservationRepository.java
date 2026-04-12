package com.greenloop.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Reservation entities.
 *
 * Provides database access for CRUD operations and custom queries related to reservations.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@Repository
public interface ReservationRepository extends JpaRepository<Object, Long> {

    /**
     * Finds all reservations with expired pickup windows.
     *
     * Queries for reservations in a specific status where pickupWindowEnd is in the past.
     *
     * @param status the reservation status to filter by (e.g., "RESERVED")
     * @param now the current timestamp
     * @return list of expired reservations
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.pickupWindowEnd < :now")
    List<?> findExpiredReservations(@Param("status") String status, @Param("now") LocalDateTime now);

    /**
     * Counts reservations with a specific status.
     *
     * @param status the reservation status
     * @return the count of reservations
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * Counts no-show reservations for a user.
     *
     * @param userId the user ID
     * @param status the reservation status (typically "NO_SHOW")
     * @return the count of matching reservations
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId AND r.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * Finds all reservations for a user with a specific status.
     *
     * @param userId the user ID
     * @param status the reservation status
     * @return list of matching reservations
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status = :status")
    List<?> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * Finds all active reservations for a user.
     *
     * @param userId the user ID
     * @return list of active reservations
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status IN ('RESERVED', 'COLLECTED')")
    List<?> findActiveByUserId(@Param("userId") Long userId);

    /**
     * Finds a reservation by ID if it exists.
     *
     * @param id the reservation ID
     * @return optional containing the reservation, or empty if not found
     */
    Optional<Object> findById(Long id);

    /**
     * Finds all reservations for a listing.
     *
     * @param listingId the listing ID
     * @return list of reservations for the listing
     */
    @Query("SELECT r FROM Reservation r WHERE r.listingId = :listingId")
    List<?> findByListingId(@Param("listingId") Long listingId);
}
