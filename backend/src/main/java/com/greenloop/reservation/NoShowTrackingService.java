package com.greenloop.reservation;

import com.greenloop.listing.ListingRepository;
import com.greenloop.realtime.ListingEventPublisher;
import com.greenloop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for tracking no-show reservations and enforcing account restrictions.
 *
 * Monitors reservations with expired pickup windows and marks them as NO_SHOW.
 * Increments user no-show counts and flags accounts for review after 3 no-shows.
 *
 * This service is called by NoShowScheduler on a recurring schedule.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
// @Service
public class NoShowTrackingService {

    private static final Logger log = LoggerFactory.getLogger(NoShowTrackingService.class);

    private static final int NO_SHOW_THRESHOLD = 3;
    private static final String ACCOUNT_STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    private static final String RESERVATION_STATUS_NO_SHOW = "NO_SHOW";
    private static final String RESERVATION_STATUS_RESERVED = "RESERVED";
    private static final String LISTING_STATUS_AVAILABLE = "AVAILABLE";

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ListingEventPublisher listingEventPublisher;

    /**
     * Constructs the NoShowTrackingService.
     *
     * @param reservationRepository the reservation data repository
     * @param userRepository the user data repository
     * @param listingRepository the listing data repository
     * @param listingEventPublisher the event publisher for WebSocket notifications
     */
    public NoShowTrackingService(ReservationRepository reservationRepository,
                                 UserRepository userRepository,
                                 ListingRepository listingRepository,
                                 ListingEventPublisher listingEventPublisher) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.listingEventPublisher = listingEventPublisher;
    }

    /**
     * Checks if a reservation has expired and marks it as NO_SHOW if applicable.
     *
     * Logic:
     * 1. If reservation status is RESERVED and pickup window has ended
     * 2. Mark reservation as NO_SHOW
     * 3. Increment user's no-show count
     * 4. If count >= 3, flag user account as UNDER_REVIEW
     * 5. Return listing to AVAILABLE status
     * 6. Publish listing update to feed for real-time visibility
     *
     * @param reservationId the ID of the reservation to check
     * @return true if reservation was marked as no-show, false otherwise
     */
    @Transactional
    public boolean checkAndRecordNoShow(Long reservationId) {
        try {
            var reservation = reservationRepository.findById(reservationId);

            if (reservation.isEmpty()) {
                log.warn("Reservation not found: {}", reservationId);
                return false;
            }

            var res = reservation.get();
            LocalDateTime now = LocalDateTime.now();

            // Check if reservation is still RESERVED and pickup window has expired
            if (!RESERVATION_STATUS_RESERVED.equals(getStatus(res))) {
                log.debug("Reservation {} is not in RESERVED status. Current status: {}", reservationId, getStatus(res));
                return false;
            }

            // Get pickup window end time (assuming reservation has this field)
            LocalDateTime pickupWindowEnd = getPickupWindowEnd(res);

            if (pickupWindowEnd == null || now.isBefore(pickupWindowEnd)) {
                log.debug("Pickup window not yet expired for reservation {}", reservationId);
                return false;
            }

            // Mark reservation as NO_SHOW
            log.info("Marking reservation {} as NO_SHOW (pickup window expired at {})", reservationId, pickupWindowEnd);

            setStatus(res, RESERVATION_STATUS_NO_SHOW);
            setUpdatedAt(res, now);
            reservationRepository.save(res);

            // Get associated user
            var userId = getUserIdFromReservation(res);
            var user = userRepository.findById(userId);

            if (user.isPresent()) {
                var userEntity = user.get();

                // Increment no-show count
                int currentNoShowCount = getNoShowCount(userEntity);
                setNoShowCount(userEntity, currentNoShowCount + 1);

                log.info("Incremented no-show count for user {}: {} -> {}", userId, currentNoShowCount, currentNoShowCount + 1);

                // Check if threshold exceeded
                if (currentNoShowCount + 1 >= NO_SHOW_THRESHOLD) {
                    setAccountStatus(userEntity, ACCOUNT_STATUS_UNDER_REVIEW);
                    log.warn("User {} flagged for account review due to {} no-shows", userId, currentNoShowCount + 1);
                }

                userRepository.save(userEntity);
            }

            // Return listing to AVAILABLE status
            var listingId = getListingIdFromReservation(res);
            var listing = listingRepository.findById(listingId);

            if (listing.isPresent()) {
                var listingEntity = listing.get();
                setStatus(listingEntity, LISTING_STATUS_AVAILABLE);
                setUpdatedAt(listingEntity, now);
                listingRepository.save(listingEntity);

                log.info("Listing {} returned to AVAILABLE status after no-show", listingId);

                // Publish listing update to feed
                try {
                    listingEventPublisher.publishListingUpdate(listingEntity);
                } catch (Exception e) {
                    log.error("Failed to publish listing update after no-show recording", e);
                }
            }

            return true;

        } catch (Exception e) {
            log.error("Error processing no-show for reservation {}", reservationId, e);
            return false;
        }
    }

    /**
     * Retrieves no-show statistics for a user.
     *
     * @param userId the user ID
     * @return a map containing:
     *         - "totalNoShows": integer count of no-show reservations
     *         - "accountStatus": string account status (e.g., "ACTIVE", "UNDER_REVIEW")
     */
    public Map<String, Object> getNoShowStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            var user = userRepository.findById(userId);

            if (user.isPresent()) {
                int noShowCount = getNoShowCount(user.get());
                String accountStatus = getAccountStatus(user.get());

                stats.put("totalNoShows", noShowCount);
                stats.put("accountStatus", accountStatus);
                stats.put("underReview", ACCOUNT_STATUS_UNDER_REVIEW.equals(accountStatus));

                log.debug("Retrieved no-show stats for user {}: {} no-shows, status={}", userId, noShowCount, accountStatus);
            } else {
                log.warn("User not found: {}", userId);
                stats.put("totalNoShows", 0);
                stats.put("accountStatus", "UNKNOWN");
            }

        } catch (Exception e) {
            log.error("Error retrieving no-show stats for user {}", userId, e);
            stats.put("totalNoShows", 0);
            stats.put("accountStatus", "ERROR");
        }

        return stats;
    }

    /**
     * Counts total no-show reservations for a user.
     *
     * @param userId the user ID
     * @return the count of NO_SHOW reservations
     */
    public long countUserNoShows(Long userId) {
        return reservationRepository.countByUserIdAndStatus(userId, RESERVATION_STATUS_NO_SHOW);
    }

    // Helper methods for accessing entity fields using reflection

    /**
     * Extracts pickup window end time from a reservation.
     *
     * @param reservation the reservation entity
     * @return the pickup window end time, or null if not available
     */
    private LocalDateTime getPickupWindowEnd(Object reservation) {
        try {
            var method = reservation.getClass().getMethod("getPickupWindowEnd");
            return (LocalDateTime) method.invoke(reservation);
        } catch (Exception e) {
            log.warn("Could not extract pickupWindowEnd from reservation", e);
            return null;
        }
    }

    /**
     * Extracts user ID from a reservation.
     *
     * @param reservation the reservation entity
     * @return the user ID
     */
    private Long getUserIdFromReservation(Object reservation) {
        try {
            var method = reservation.getClass().getMethod("getUserId");
            return (Long) method.invoke(reservation);
        } catch (Exception e) {
            log.error("Could not extract userId from reservation", e);
            return null;
        }
    }

    /**
     * Extracts listing ID from a reservation.
     *
     * @param reservation the reservation entity
     * @return the listing ID
     */
    private Long getListingIdFromReservation(Object reservation) {
        try {
            var method = reservation.getClass().getMethod("getListingId");
            return (Long) method.invoke(reservation);
        } catch (Exception e) {
            log.error("Could not extract listingId from reservation", e);
            return null;
        }
    }

    /**
     * Retrieves the no-show count from a user entity.
     *
     * @param user the user entity
     * @return the no-show count
     */
    private int getNoShowCount(Object user) {
        try {
            var method = user.getClass().getMethod("getNoShowCount");
            return (Integer) method.invoke(user);
        } catch (Exception e) {
            log.warn("Could not extract noShowCount from user, defaulting to 0", e);
            return 0;
        }
    }

    /**
     * Sets the no-show count on a user entity.
     *
     * @param user the user entity
     * @param count the count to set
     */
    private void setNoShowCount(Object user, int count) {
        try {
            var method = user.getClass().getMethod("setNoShowCount", int.class);
            method.invoke(user, count);
        } catch (Exception e) {
            log.error("Could not set noShowCount on user", e);
        }
    }

    /**
     * Retrieves the account status from a user entity.
     *
     * @param user the user entity
     * @return the account status
     */
    private String getAccountStatus(Object user) {
        try {
            var method = user.getClass().getMethod("getAccountStatus");
            return (String) method.invoke(user);
        } catch (Exception e) {
            log.warn("Could not extract accountStatus from user", e);
            return "UNKNOWN";
        }
    }

    /**
     * Sets the account status on a user entity.
     *
     * @param user the user entity
     * @param status the status to set
     */
    private void setAccountStatus(Object user, String status) {
        try {
            var method = user.getClass().getMethod("setAccountStatus", String.class);
            method.invoke(user, status);
        } catch (Exception e) {
            log.error("Could not set accountStatus on user", e);
        }
    }

    private String getStatus(Object entity) {
        try {
            var method = entity.getClass().getMethod("getStatus");
            return (String) method.invoke(entity);
        } catch (Exception e) {
            log.warn("Could not extract status from {}", entity.getClass().getSimpleName(), e);
            return null;
        }
    }

    private void setStatus(Object entity, String status) {
        try {
            var method = entity.getClass().getMethod("setStatus", String.class);
            method.invoke(entity, status);
        } catch (Exception e) {
            log.error("Could not set status on {}", entity.getClass().getSimpleName(), e);
        }
    }

    private void setUpdatedAt(Object entity, LocalDateTime time) {
        try {
            var method = entity.getClass().getMethod("setUpdatedAt", LocalDateTime.class);
            method.invoke(entity, time);
        } catch (Exception e) {
            log.error("Could not set updatedAt on {}", entity.getClass().getSimpleName(), e);
        }
    }
}
