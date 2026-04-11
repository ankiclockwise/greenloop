package com.greenloop.realtime;

import com.greenloop.realtime.dto.ListingBroadcastDto;
import com.greenloop.realtime.dto.ReservationNotificationDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket controller for real-time feed updates and user notifications.
 *
 * Handles incoming STOMP messages from clients and broadcasts updates via:
 * - /topic/feed: broadcast new listings to all connected clients
 * - /topic/feed.updates: broadcast listing updates to all connected clients
 * - /queue/reservations/{userId}: send reservation confirmations to specific user
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@Controller
public class FeedWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(FeedWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructs the WebSocket controller.
     *
     * @param messagingTemplate the STOMP messaging template for sending messages
     */
    public FeedWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Receives new listing notifications and broadcasts to all connected clients.
     *
     * Maps incoming messages from /app/listing.new to /topic/feed for broadcast.
     * Called by ListingEventPublisher after a new listing is persisted.
     *
     * @param listing the new listing broadcast DTO containing listing details
     */
    @MessageMapping("/listing.new")
    public void handleNewListing(@Payload ListingBroadcastDto listing) {
        log.info("Broadcasting new listing: {} (ID: {})", listing.getTitle(), listing.getListingId());

        messagingTemplate.convertAndSend("/topic/feed", listing);
    }

    /**
     * Receives listing update notifications and broadcasts to all connected clients.
     *
     * Maps incoming messages from /app/listing.update to /topic/feed.updates for broadcast.
     * Called by ListingEventPublisher when a listing's status or details change.
     *
     * Example updates: availability status change, price adjustment, location update
     *
     * @param listing the updated listing broadcast DTO
     */
    @MessageMapping("/listing.update")
    public void handleListingUpdate(@Payload ListingBroadcastDto listing) {
        log.info("Broadcasting listing update: {} (ID: {})", listing.getTitle(), listing.getListingId());

        messagingTemplate.convertAndSend("/topic/feed.updates", listing);
    }

    /**
     * Sends reservation confirmation to a specific user via their personal queue.
     *
     * Maps incoming messages from /app/reservation.confirm to /queue/reservations/{userId}.
     * Called by ListingEventPublisher after a reservation is confirmed.
     *
     * The message is routed to the user's individual queue, ensuring only that user
     * receives their reservation confirmation notification.
     *
     * @param notification the reservation notification DTO containing confirmation details
     */
    @MessageMapping("/reservation.confirm")
    public void handleReservationConfirmation(@Payload ReservationNotificationDto notification) {
        String userQueueDestination = "/queue/reservations/" + notification.getUserId();

        log.info("Sending reservation confirmation to user queue: {}", userQueueDestination);

        messagingTemplate.convertAndSend(userQueueDestination, notification);
    }
}
