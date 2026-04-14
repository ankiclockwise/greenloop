package com.greenloop.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.pickupWindowEnd < :now")
    List<Reservation> findExpiredReservations(@Param("status") String status, @Param("now") LocalDateTime now);

    long countByStatus(String status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId AND r.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.status = :status")
    List<Reservation> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.status IN ('RESERVED', 'COLLECTED')")
    List<Reservation> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Reservation r WHERE r.listing.id = :listingId")
    List<Reservation> findByListingId(@Param("listingId") Long listingId);
}