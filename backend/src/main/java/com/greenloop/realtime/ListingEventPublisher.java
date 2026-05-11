package com.greenloop.realtime;

import com.greenloop.model.Listing;
import com.greenloop.realtime.dto.ListingBroadcastDto;
import com.greenloop.realtime.dto.ReservationNotificationDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Service for publishing real-time events to connected WebSocket clients.
 *
 * Coordinates with ListingService and ReservationService to broadcast listing updates
 * and send user-specific reservation notifications.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@Service
public class ListingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ListingEventPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructs the ListingEventPublisher.
     *
     * @param messagingTemplate the STOMP messaging template for sending messages
     */
    public ListingEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Publishes a new listing to all connected clients.
     *
     * Called by ListingService after a new listing is persisted to the database.
     * Broadcasts the listing to the /topic/feed destination for real-time delivery.
     *
     * @param listing the Listing entity to publish (assumed to have all required fields)
     */
    public void publishNewListing(Listing listing) {
        try {
            ListingBroadcastDto dto = fromListing(listing, "NEW");
            log.info("Publishing new listing to feed: {} (ID: {})", dto.getTitle(), dto.getListingId());
            messagingTemplate.convertAndSend("/topic/feed", dto);
        } catch (Exception e) {
            log.error("Failed to publish new listing event", e);
        }
    }

    /**
     * Publishes a listing update to all connected clients.
     *
     * Called by ListingService when an existing listing's status, details, or availability changes.
     * Broadcasts the update to the /topic/feed.updates destination.
     *
     * @param listing the updated Listing entity
     */
    public void publishListingUpdate(Listing listing) {
        try {
            ListingBroadcastDto dto = fromListing(listing, "UPDATE");
            log.info("Publishing listing update: {} (ID: {})", dto.getTitle(), dto.getListingId());
            messagingTemplate.convertAndSend("/topic/feed.updates", dto);
        } catch (Exception e) {
            log.error("Failed to publish listing update event", e);
        }
    }

    /**
     * Publishes a reservation confirmation to a specific user's personal queue.
     *
     * Called by ReservationService after a reservation is successfully created and confirmed.
     * Sends the notification to the user's individual queue for secure, user-specific delivery.
     *
     * @param reservation the Reservation entity
     * @param user the User entity (recipient)
     */
    public void publishReservationConfirmation(com.greenloop.reservation.Reservation reservation, Object user) {
        try {
            ReservationNotificationDto dto = convertToReservationNotificationDto(reservation, user, "CONFIRMATION");
            messagingTemplate.convertAndSend("/queue/reservations/" + getUserId(user), dto);
            log.info("Publishing reservation confirmation to user {}", getUserId(user));
        } catch (Exception e) {
            log.error("Failed to publish reservation confirmation event", e);
        }
    }

    public void publishReservationCancellation(com.greenloop.reservation.Reservation reservation, Object user, String reason) {
        try {
            ReservationNotificationDto dto = convertToReservationNotificationDto(reservation, user, "CANCELLATION");
            dto.setMessage("Your reservation has been cancelled. Reason: " + reason);
            messagingTemplate.convertAndSend("/queue/reservations/" + getUserId(user), dto);
        } catch (Exception e) {
            log.error("Failed to publish reservation cancellation event", e);
        }
    }

    public void publishNoShowNotification(com.greenloop.reservation.Reservation reservation, Object user) {
        try {
            ReservationNotificationDto dto = convertToReservationNotificationDto(reservation, user, "NO_SHOW_WARNING");
            dto.setMessage("Your reservation has been marked as no-show due to missed pickup window.");
            dto.setStatus("NO_SHOW");
            messagingTemplate.convertAndSend("/queue/reservations/" + getUserId(user), dto);
        } catch (Exception e) {
            log.error("Failed to publish no-show notification event", e);
        }
    }

    private ListingBroadcastDto fromListing(Listing listing, String eventType) {
        ListingBroadcastDto dto = new ListingBroadcastDto();
        dto.setListingId(listing.getId());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setCategory(listing.getCategory() != null ? listing.getCategory().name() : null);
        dto.setStatus(listing.getStatus() != null ? listing.getStatus().name() : null);
        dto.setEstimatedValue(listing.getDiscountedPrice() != null
                ? listing.getDiscountedPrice() : listing.getOriginalPrice());
        dto.setImageUrl(listing.getImageUrl());
        dto.setDonorId(listing.getOwner() != null ? listing.getOwner().getId() : null);
        dto.setDonorName(listing.getOwner() != null ? listing.getOwner().getName() : null);
        dto.setLocation(listing.getPickupAddress() != null
                ? listing.getPickupAddress() + ", " + listing.getPickupCity() : null);
        dto.setCreatedAt(listing.getCreatedAt());
        dto.setPickupWindowStart(listing.getPickupWindowStart());
        dto.setPickupWindowEnd(listing.getPickupWindowEnd());
        dto.setEventType(eventType);
        return dto;
    }

    /**
     * Converts a Reservation entity to a ReservationNotificationDto.
     *
     * Uses reflection to extract fields from the Reservation entity.
     *
     * @param reservation the Reservation entity
     * @param user the User entity (recipient)
     * @param notificationType the type of notification
     * @return the converted ReservationNotificationDto
     */
    private ReservationNotificationDto convertToReservationNotificationDto(
            com.greenloop.reservation.Reservation reservation, Object user, String notificationType) {
        ReservationNotificationDto dto = new ReservationNotificationDto();
        dto.setReservationId(reservation.getId());
        dto.setUserId(getUserId(user));
        dto.setListingId(reservation.getListingId());
        dto.setStatus(reservation.getStatus());
        dto.setPickupWindowEnd(reservation.getPickupWindowEnd());
        dto.setNotificationType(notificationType);
        dto.setNotificationTime(LocalDateTime.now());
        if ("CONFIRMATION".equals(notificationType)) {
            dto.setMessage("Your reservation has been confirmed!");
        }
        return dto;
    }

    private Long getUserId(Object user) {
        try {
            return (Long) user.getClass().getMethod("getId").invoke(user);
        } catch (Exception e) {
            return null;
        }
    }
}
