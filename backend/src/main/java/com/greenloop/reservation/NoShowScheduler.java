package com.greenloop.reservation;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task component for monitoring and recording no-show reservations.
 *
 * Runs every 5 minutes to check for reservations with expired pickup windows.
 * Queries the database for reservations in RESERVED status with pickupWindowEnd < now,
 * then delegates to NoShowTrackingService for processing.
 *
 * This ensures timely handling of no-shows without requiring manual intervention.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@Component
public class NoShowScheduler {

    private static final Logger log = LoggerFactory.getLogger(NoShowScheduler.class);

    private static final String RESERVATION_STATUS_RESERVED = "RESERVED";
    private static final long SCHEDULER_INTERVAL_MS = 300000; // 5 minutes

    private final ReservationRepository reservationRepository;
    private final NoShowTrackingService noShowTrackingService;

    /**
     * Constructs the NoShowScheduler.
     *
     * @param reservationRepository the reservation data repository
     * @param noShowTrackingService the no-show tracking service
     */
    public NoShowScheduler(ReservationRepository reservationRepository,
                          NoShowTrackingService noShowTrackingService) {
        this.reservationRepository = reservationRepository;
        this.noShowTrackingService = noShowTrackingService;
    }

    /**
     * Scheduled task that checks for expired reservations and records no-shows.
     *
     * Executes every 5 minutes (300,000 ms).
     *
     * Process:
     * 1. Query database for all RESERVED reservations with pickupWindowEnd < now
     * 2. For each expired reservation, call NoShowTrackingService.checkAndRecordNoShow()
     * 3. Log summary of processing results
     *
     * Note: This method is idempotent and safe to run multiple times. Reservations
     * that are already marked as NO_SHOW will be skipped.
     */
    @Scheduled(fixedRate = SCHEDULER_INTERVAL_MS)
    public void checkExpiredReservations() {
        try {
            LocalDateTime now = LocalDateTime.now();

            log.debug("Starting no-show check scheduler at {}", now);

            // Query for reservations with expired pickup windows
            List<?> expiredReservations = reservationRepository.findExpiredReservations(RESERVATION_STATUS_RESERVED, now);

            if (expiredReservations.isEmpty()) {
                log.debug("No expired reservations found");
                return;
            }

            log.info("Found {} expired reservations to process", expiredReservations.size());

            int processedCount = 0;
            int successCount = 0;
            int errorCount = 0;

            // Process each expired reservation
            for (Object reservation : expiredReservations) {
                try {
                    Long reservationId = extractReservationId(reservation);
                    boolean recorded = noShowTrackingService.checkAndRecordNoShow(reservationId);

                    if (recorded) {
                        successCount++;
                    }
                    processedCount++;

                } catch (Exception e) {
                    errorCount++;
                    log.error("Error processing reservation for no-show", e);
                }
            }

            // Log summary
            log.info("No-show check completed: processed={}, recorded={}, errors={}",
                    processedCount, successCount, errorCount);

        } catch (Exception e) {
            log.error("Fatal error in no-show scheduler", e);
        }
    }

    /**
     * Alternative scheduled task for periodic cleanup and statistics.
     *
     * Runs once per hour to log overall no-show statistics.
     * Can be extended to implement cleanup or archival logic.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void logNoShowStatistics() {
        try {
            long totalNoShows = reservationRepository.countByStatus("NO_SHOW");
            long activeReservations = reservationRepository.countByStatus(RESERVATION_STATUS_RESERVED);

            log.info("No-show statistics - Total no-shows: {}, Active reservations: {}",
                    totalNoShows, activeReservations);

        } catch (Exception e) {
            log.error("Error logging no-show statistics", e);
        }
    }

    /**
     * Extracts the reservation ID from a reservation entity.
     *
     * @param reservation the reservation entity
     * @return the reservation ID
     */
    private Long extractReservationId(Object reservation) {
        try {
            var method = reservation.getClass().getMethod("getId");
            return (Long) method.invoke(reservation);
        } catch (Exception e) {
            log.error("Could not extract ID from reservation", e);
            throw new RuntimeException("Unable to extract reservation ID", e);
        }
    }
}
