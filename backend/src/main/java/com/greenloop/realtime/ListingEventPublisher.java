package com.greenloop.realtime;

import com.greenloop.realtime.dto.ListingBroadcastDto;
import com.greenloop.realtime.dto.ReservationNotificationDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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
    public void publishNewListing(Object listing) {
        try {
            ListingBroadcastDto dto = convertToListingBroadcastDto(listing, "NEW");

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
    public void publishListingUpdate(Object listing) {
        try {
            ListingBroadcastDto dto = convertToListingBroadcastDto(listing, "UPDATE");

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
    public void publishReservationConfirmation(Object reservation, Object user) {
        try {
            ReservationNotificationDto dto = convertToReservationNotificationDto(reservation, user, "CONFIRMATION");

            String userQueue = "/queue/reservations/" + getUserId(user);

            log.info("Publishing reservation confirmation to user queue: {}", userQueue);

            messagingTemplate.convertAndSend(userQueue, dto);

        } catch (Exception e) {
            log.error("Failed to publish reservation confirmation event", e);
        }
    }

    /**
     * Publishes a reservation cancellation notification to a user.
     *
     * Called when a reservation is cancelled by the user or system.
     *
     * @param reservation the Reservation entity being cancelled
     * @param user the User entity (recipient)
     * @param reason the reason for cancellation
     */
    public void publishReservationCancellation(Object reservation, Object user, String reason) {
        try {
            ReservationNotificationDto dto = convertToReservationNotificationDto(reservation, user, "CANCELLATION");
            dto.setMessage("Your reservation has been cancelled. Reason: " + reason);

            String userQueue = "/queue/reservations/" + getUserId(user);

            log.info("Publishing reservation cancellation to user queue: {}", userQueue);

            messagingTemplate.convertAndSend(userQueue, dto);

        } catch (Exception e) {
            log.error("Failed to publish reservation cancellation event", e);
        }
    }

    /**
     * Publishes a no-show notification to a user.
     *
     * Called by NoShowTrackingService when a user fails to collect a reserved item.
     *
     * @param reservation the Reservation entity marked as NO_SHOW
     * @param user the User entity (recipient)
     */
    public void publishNoShowNotification(Object reservation, Object user) {
        try {
            ReservationNotificationDto dto = convertToReservationNotificationDto(reservation, user, "NO_SHOW_WARNING");
            dto.setMessage("Your reservation has been marked as no-show due to missed pickup window.");
            dto.setStatus("NO_SHOW");

            String userQueue = "/queue/reservations/" + getUserId(user);

            log.info("Publishing no-show notification to user queue: {}", userQueue);

            messagingTemplate.convertAndSend(userQueue, dto);

        } catch (Exception e) {
            log.error("Failed to publish no-show notification event", e);
        }
    }

    /**
     * Converts a Listing entity to a ListingBroadcastDto.
     *
     * Uses reflection to extract fields from the Listing entity.
     * Assumes Listing has getters for: listingId, title, description, category, status,
     * estimatedValue, imageUrl, donorId, createdAt, pickupWindowStart, pickupWindowEnd
     *
     * @param listing the Listing entity
     * @param eventType the event type (NEW or UPDATE)
     * @return the converted ListingBroadcastDto
     */
    private ListingBroadcastDto convertToListingBroadcastDto(Object listing, String eventType) {
        try {
            ListingBroadcastDto dto = new ListingBroadcastDto();
            dto.setListingId(getFieldValue(listing, "id", Long.class));
            dto.setTitle(getFieldValue(listing, "title", String.class));
            dto.setDescription(getFieldValue(listing, "description", String.class));
            dto.setCategory(getFieldValue(listing, "category", String.class));
            dto.setStatus(getFieldValue(listing, "status", String.class));
            dto.setEstimatedValue((BigDecimal) getFieldValue(listing, "estimatedValue", Object.class));
            dto.setImageUrl(getFieldValue(listing, "imageUrl", String.class));
            dto.setDonorId(getFieldValue(listing, "donorId", Long.class));
            dto.setDonorName(getFieldValue(listing, "donorName", String.class));
            dto.setLocation(getFieldValue(listing, "location", String.class));
            dto.setCreatedAt(getFieldValue(listing, "createdAt", LocalDateTime.class));
            dto.setPickupWindowStart(getFieldValue(listing, "pickupWindowStart", LocalDateTime.class));
            dto.setPickupWindowEnd(getFieldValue(listing, "pickupWindowEnd", LocalDateTime.class));
            dto.setEventType(eventType);

            return dto;
        } catch (Exception e) {
            log.error("Failed to convert Listing entity to DTO", e);
            throw new RuntimeException("Unable to convert listing for broadcast", e);
        }
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
    private ReservationNotificationDto convertToReservationNotificationDto(Object reservation, Object user, String notificationType) {
        try {
            ReservationNotificationDto dto = new ReservationNotificationDto();
            dto.setReservationId(getFieldValue(reservation, "id", Long.class));
            dto.setUserId(getUserId(user));
            dto.setListingId(getFieldValue(reservation, "listingId", Long.class));
            dto.setListingTitle(getFieldValue(reservation, "listingTitle", String.class));
            dto.setStatus(getFieldValue(reservation, "status", String.class));
            dto.setPickupWindowStart(getFieldValue(reservation, "pickupWindowStart", LocalDateTime.class));
            dto.setPickupWindowEnd(getFieldValue(reservation, "pickupWindowEnd", LocalDateTime.class));
            dto.setLocation(getFieldValue(reservation, "location", String.class));
            dto.setNotificationType(notificationType);
            dto.setNotificationTime(LocalDateTime.now());

            if (notificationType.equals("CONFIRMATION")) {
                dto.setMessage("Your reservation has been confirmed!");
            }

            return dto;
        } catch (Exception e) {
            log.error("Failed to convert Reservation entity to notification DTO", e);
            throw new RuntimeException("Unable to convert reservation for notification", e);
        }
    }

    /**
     * Extracts a field value from an object using reflection.
     *
     * @param object the object to extract from
     * @param fieldName the name of the field
     * @param type the expected type
     * @return the field value
     */
    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(Object object, String fieldName, Class<T> type) {
        try {
            var method = object.getClass().getMethod("get" + capitalize(fieldName));
            return (T) method.invoke(object);
        } catch (Exception e) {
            log.warn("Could not extract field {} from {}", fieldName, object.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Extracts the user ID from a User entity.
     *
     * @param user the User entity
     * @return the user ID
     */
    private Long getUserId(Object user) {
        return getFieldValue(user, "id", Long.class);
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
