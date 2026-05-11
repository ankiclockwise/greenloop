package com.greenloop.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * State machine for managing reservation status transitions.
 *
 * Defines and validates all legal state transitions for reservations:
 * - RESERVED → COLLECTED (successful pickup)
 * - RESERVED → CANCELLED (user cancellation)
 * - RESERVED → NO_SHOW (missed pickup window)
 * - RESERVED → EXPIRED (system timeout)
 *
 * Ensures business rules are enforced and prevents illegal state transitions.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@Service
public class ReservationStatusMachine {

    private static final Logger log = LoggerFactory.getLogger(ReservationStatusMachine.class);

    private final ReservationRepository reservationRepository;

    // State transition matrix
    private static final Map<String, EnumSet<ReservationStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        // From RESERVED state, we can transition to:
        VALID_TRANSITIONS.put("RESERVED", EnumSet.of(
                ReservationStatus.COLLECTED,
                ReservationStatus.CANCELLED,
                ReservationStatus.NO_SHOW,
                ReservationStatus.EXPIRED
        ));

        // From COLLECTED state, no further transitions
        VALID_TRANSITIONS.put("COLLECTED", EnumSet.noneOf(ReservationStatus.class));

        // From CANCELLED state, no further transitions
        VALID_TRANSITIONS.put("CANCELLED", EnumSet.noneOf(ReservationStatus.class));

        // From NO_SHOW state, no further transitions
        VALID_TRANSITIONS.put("NO_SHOW", EnumSet.noneOf(ReservationStatus.class));

        // From EXPIRED state, no further transitions
        VALID_TRANSITIONS.put("EXPIRED", EnumSet.noneOf(ReservationStatus.class));
    }

    /**
     * Constructs the ReservationStatusMachine.
     *
     * @param reservationRepository the reservation data repository
     */
    public ReservationStatusMachine(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Transitions a reservation to a new status with validation.
     *
     * Validates that:
     * 1. Reservation exists
     * 2. Current status allows transition to new status
     * 3. Actor role has permission for this transition
     * 4. Saves the updated reservation to database
     *
     * @param reservationId the ID of the reservation to transition
     * @param newStatus the target status
     * @param actorRole the role of the actor performing the transition (USER, ADMIN, SYSTEM)
     * @return true if transition was successful
     * @throws InvalidStatusTransitionException if the transition is not allowed
     */
    @Transactional
    public boolean transition(Long reservationId, String newStatus, String actorRole) {
        try {
            var reservation = reservationRepository.findById(reservationId);

            if (reservation.isEmpty()) {
                throw new IllegalArgumentException("Reservation not found: " + reservationId);
            }

            var res = reservation.get();
            String currentStatus = getCurrentStatus(res);

            log.info("Attempting transition for reservation {}: {} -> {} (actor: {})",
                    reservationId, currentStatus, newStatus, actorRole);

            // Validate transition is allowed
            if (!isTransitionValid(currentStatus, newStatus)) {
                throw new InvalidStatusTransitionException(
                        String.format("Cannot transition from %s to %s", currentStatus, newStatus)
                );
            }

            // Validate actor has permission
            if (!hasPermission(currentStatus, newStatus, actorRole)) {
                throw new InvalidStatusTransitionException(
                        String.format("Actor role %s cannot perform transition %s -> %s",
                                actorRole, currentStatus, newStatus)
                );
            }

            // Apply transition
            setStatus(res, newStatus);
            setUpdatedAt(res, LocalDateTime.now());

            // Save to database
            reservationRepository.save(res);

            log.info("Successfully transitioned reservation {} to {}", reservationId, newStatus);

            return true;

        } catch (InvalidStatusTransitionException e) {
            log.warn("Invalid status transition: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during reservation status transition", e);
            throw new RuntimeException("Failed to transition reservation status", e);
        }
    }

    /**
     * Checks if a transition from current status to new status is valid.
     *
     * @param currentStatus the current status
     * @param newStatus the target status
     * @return true if the transition is valid
     */
    public boolean isTransitionValid(String currentStatus, String newStatus) {
        if (!VALID_TRANSITIONS.containsKey(currentStatus)) {
            return false;
        }

        EnumSet<ReservationStatus> validNextStates = VALID_TRANSITIONS.get(currentStatus);

        try {
            ReservationStatus targetStatus = ReservationStatus.valueOf(newStatus);
            return validNextStates.contains(targetStatus);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value: {}", newStatus);
            return false;
        }
    }

    /**
     * Checks if an actor with a given role has permission to perform a transition.
     *
     * Rules:
     * - RESERVED → CANCELLED: USER role only
     * - RESERVED → COLLECTED: ADMIN role only (pickup confirmation)
     * - RESERVED → NO_SHOW: SYSTEM role only (automated)
     * - RESERVED → EXPIRED: SYSTEM role only (automated)
     *
     * @param currentStatus the current status
     * @param newStatus the target status
     * @param actorRole the role of the actor
     * @return true if the actor has permission
     */
    private boolean hasPermission(String currentStatus, String newStatus, String actorRole) {
        if (!"RESERVED".equals(currentStatus)) {
            return false;
        }

        return switch (newStatus) {
            case "CANCELLED" -> "USER".equalsIgnoreCase(actorRole);
            case "COLLECTED" -> "ADMIN".equalsIgnoreCase(actorRole);
            case "NO_SHOW", "EXPIRED" -> "SYSTEM".equalsIgnoreCase(actorRole);
            default -> false;
        };
    }

    /**
     * Gets the current status of a reservation.
     *
     * @param reservation the reservation entity
     * @return the current status
     */
    private String getCurrentStatus(Object reservation) {
        try {
            var method = reservation.getClass().getMethod("getStatus");
            return (String) method.invoke(reservation);
        } catch (Exception e) {
            log.error("Could not extract status from reservation", e);
            throw new RuntimeException("Unable to get reservation status", e);
        }
    }

    /**
     * Sets the status of a reservation.
     *
     * @param reservation the reservation entity
     * @param status the status to set
     */
    private void setStatus(Object reservation, String status) {
        try {
            var method = reservation.getClass().getMethod("setStatus", String.class);
            method.invoke(reservation, status);
        } catch (Exception e) {
            log.error("Could not set status on reservation", e);
            throw new RuntimeException("Unable to set reservation status", e);
        }
    }

    /**
     * Sets the updatedAt timestamp of a reservation.
     *
     * @param reservation the reservation entity
     * @param updatedAt the timestamp to set
     */
    private void setUpdatedAt(Object reservation, LocalDateTime updatedAt) {
        try {
            var method = reservation.getClass().getMethod("setUpdatedAt", LocalDateTime.class);
            method.invoke(reservation, updatedAt);
        } catch (Exception e) {
            log.error("Could not set updatedAt on reservation", e);
            throw new RuntimeException("Unable to set reservation updatedAt", e);
        }
    }

    /**
     * Exception thrown when an invalid status transition is attempted.
     */
    public static class InvalidStatusTransitionException extends RuntimeException {
        public InvalidStatusTransitionException(String message) {
            super(message);
        }

        public InvalidStatusTransitionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Enum of valid reservation statuses.
     */
    enum ReservationStatus {
        RESERVED,
        COLLECTED,
        CANCELLED,
        NO_SHOW,
        EXPIRED
    }
}
