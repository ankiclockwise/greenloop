package com.greenloop.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * REST API controller for no-show tracking and user statistics.
 *
 * Provides endpoints for querying no-show records and account status information.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/no-shows")
public class NoShowTrackingController {

    private static final Logger log = LoggerFactory.getLogger(NoShowTrackingController.class);

    private final NoShowTrackingService noShowTrackingService;

    /**
     * Constructs the NoShowTrackingController.
     *
     * @param noShowTrackingService the no-show tracking service
     */
    public NoShowTrackingController(NoShowTrackingService noShowTrackingService) {
        this.noShowTrackingService = noShowTrackingService;
    }

    /**
     * Gets no-show statistics for a user.
     *
     * Returns the total number of no-shows and current account status.
     *
     * @param userId the user ID
     * @return response containing no-show stats
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserNoShowStats(@PathVariable Long userId) {
        log.info("Retrieving no-show statistics for user {}", userId);

        try {
            Map<String, Object> stats = noShowTrackingService.getNoShowStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving no-show stats for user {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to retrieve statistics",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Gets the no-show count for a user.
     *
     * @param userId the user ID
     * @return response containing the count
     */
    @GetMapping("/users/{userId}/count")
    public ResponseEntity<Map<String, Object>> getUserNoShowCount(@PathVariable Long userId) {
        log.info("Retrieving no-show count for user {}", userId);

        try {
            long count = noShowTrackingService.countUserNoShows(userId);
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "totalNoShows", count
            ));
        } catch (Exception e) {
            log.error("Error retrieving no-show count for user {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to retrieve count",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Manually triggers no-show check for a specific reservation.
     *
     * Typically called by admin operations or system tasks.
     *
     * @param reservationId the reservation ID
     * @return response indicating success or failure
     */
    @PostMapping("/reservations/{reservationId}/check")
    public ResponseEntity<Map<String, Object>> checkReservationNoShow(@PathVariable Long reservationId) {
        log.info("Manually checking no-show status for reservation {}", reservationId);

        try {
            boolean recorded = noShowTrackingService.checkAndRecordNoShow(reservationId);

            return ResponseEntity.ok(Map.of(
                    "reservationId", reservationId,
                    "noShowRecorded", recorded,
                    "message", recorded ? "Reservation marked as no-show" : "Reservation not eligible for no-show"
            ));
        } catch (Exception e) {
            log.error("Error checking no-show for reservation {}", reservationId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to check no-show status",
                    "message", e.getMessage()
            ));
        }
    }
}
