package com.greenloop.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * REST API controller for reservation status transitions.
 *
 * Provides endpoints for updating reservation status with validation
 * and error handling.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
// @RestController
@RequestMapping("/api/v1/reservations")
public class ReservationStatusController {

    private static final Logger log = LoggerFactory.getLogger(ReservationStatusController.class);

    private final ReservationStatusMachine statusMachine;

    /**
     * Constructs the ReservationStatusController.
     *
     * @param statusMachine the reservation status machine
     */
    public ReservationStatusController(ReservationStatusMachine statusMachine) {
        this.statusMachine = statusMachine;
    }

    /**
     * Transitions a reservation to a new status.
     *
     * Requires:
     * - reservationId: path parameter
     * - newStatus: request body
     * - actorRole: request body (USER, ADMIN, SYSTEM)
     *
     * @param reservationId the reservation ID
     * @param request the transition request containing newStatus and actorRole
     * @return response indicating success or error details
     */
    @PostMapping("/{reservationId}/transition")
    public ResponseEntity<Map<String, Object>> transitionReservation(
            @PathVariable Long reservationId,
            @RequestBody TransitionRequest request) {

        log.info("Requesting status transition for reservation {}: {} -> {} (actor: {})",
                reservationId, "?", request.getNewStatus(), request.getActorRole());

        try {
            boolean success = statusMachine.transition(
                    reservationId,
                    request.getNewStatus(),
                    request.getActorRole()
            );

            return ResponseEntity.ok(Map.of(
                    "reservationId", reservationId,
                    "newStatus", request.getNewStatus(),
                    "success", success,
                    "message", "Reservation status updated successfully"
            ));

        } catch (ReservationStatusMachine.InvalidStatusTransitionException e) {
            log.warn("Invalid status transition for reservation {}: {}", reservationId, e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid Status Transition",
                    "message", e.getMessage(),
                    "reservationId", reservationId
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Reservation not found: {}", reservationId);
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Not Found",
                    "message", "Reservation not found",
                    "reservationId", reservationId
            ));

        } catch (Exception e) {
            log.error("Error transitioning reservation {}", reservationId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "reservationId", reservationId
            ));
        }
    }

    /**
     * Cancels a user's reservation.
     *
     * This is a convenience endpoint that transitions status to CANCELLED.
     *
     * @param reservationId the reservation ID
     * @return response indicating success or error
     */
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelReservation(@PathVariable Long reservationId) {

        log.info("User requesting cancellation of reservation {}", reservationId);

        try {
            boolean success = statusMachine.transition(
                    reservationId,
                    "CANCELLED",
                    "USER"
            );

            return ResponseEntity.ok(Map.of(
                    "reservationId", reservationId,
                    "status", "CANCELLED",
                    "success", success,
                    "message", "Reservation cancelled successfully"
            ));

        } catch (ReservationStatusMachine.InvalidStatusTransitionException e) {
            log.warn("Cannot cancel reservation {}: {}", reservationId, e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Cannot Cancel",
                    "message", e.getMessage(),
                    "reservationId", reservationId
            ));

        } catch (Exception e) {
            log.error("Error cancelling reservation {}", reservationId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "reservationId", reservationId
            ));
        }
    }

    /**
     * Confirms pickup of a reserved item (admin only).
     *
     * Transitions the reservation to COLLECTED status.
     *
     * @param reservationId the reservation ID
     * @return response indicating success or error
     */
    @PostMapping("/{reservationId}/collect")
    public ResponseEntity<Map<String, Object>> collectReservation(@PathVariable Long reservationId) {

        log.info("Admin confirming collection for reservation {}", reservationId);

        try {
            boolean success = statusMachine.transition(
                    reservationId,
                    "COLLECTED",
                    "ADMIN"
            );

            return ResponseEntity.ok(Map.of(
                    "reservationId", reservationId,
                    "status", "COLLECTED",
                    "success", success,
                    "message", "Item collected successfully"
            ));

        } catch (ReservationStatusMachine.InvalidStatusTransitionException e) {
            log.warn("Cannot collect reservation {}: {}", reservationId, e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Cannot Collect",
                    "message", e.getMessage(),
                    "reservationId", reservationId
            ));

        } catch (Exception e) {
            log.error("Error collecting reservation {}", reservationId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "reservationId", reservationId
            ));
        }
    }

    /**
     * Request DTO for reservation status transitions.
     */
    public static class TransitionRequest {
        private String newStatus;
        private String actorRole;

        public String getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(String newStatus) {
            this.newStatus = newStatus;
        }

        public String getActorRole() {
            return actorRole;
        }

        public void setActorRole(String actorRole) {
            this.actorRole = actorRole;
        }
    }
}
