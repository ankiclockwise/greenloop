package com.greenloop.reservation;

/**
 * Import bridge for ListingEventPublisher from realtime package.
 *
 * This file is created to allow imports within the reservation package context.
 * In production, this would typically be accessed via Spring's auto-wiring mechanism.
 *
 * The actual implementation is in: com.greenloop.realtime.ListingEventPublisher
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
public interface ListingEventPublisher {

    /**
     * Publishes a new listing to all connected WebSocket clients.
     *
     * @param listing the Listing entity to publish
     */
    void publishNewListing(Object listing);

    /**
     * Publishes a listing update to all connected WebSocket clients.
     *
     * @param listing the updated Listing entity
     */
    void publishListingUpdate(Object listing);

    /**
     * Publishes a reservation confirmation to a specific user's queue.
     *
     * @param reservation the Reservation entity
     * @param user the User entity (recipient)
     */
    void publishReservationConfirmation(Object reservation, Object user);

    /**
     * Publishes a reservation cancellation notification.
     *
     * @param reservation the cancelled Reservation entity
     * @param user the User entity (recipient)
     * @param reason the cancellation reason
     */
    void publishReservationCancellation(Object reservation, Object user, String reason);

    /**
     * Publishes a no-show notification to a user.
     *
     * @param reservation the Reservation entity marked as NO_SHOW
     * @param user the User entity (recipient)
     */
    void publishNoShowNotification(Object reservation, Object user);
}
