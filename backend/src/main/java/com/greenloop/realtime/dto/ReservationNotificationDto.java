package com.greenloop.realtime.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for sending reservation notifications to users via WebSocket.
 *
 * Sent to the user's personal queue (/queue/reservations/{userId}) when their reservation
 * is confirmed, cancelled, or experiences status changes.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
public class ReservationNotificationDto {

    private Long reservationId;
    private Long userId;
    private Long listingId;
    private String listingTitle;
    private String status; // RESERVED, COLLECTED, CANCELLED, NO_SHOW, EXPIRED
    private String notificationType; // CONFIRMATION, CANCELLATION, STATUS_CHANGE, NO_SHOW_WARNING
    private String message;
    private LocalDateTime pickupWindowStart;
    private LocalDateTime pickupWindowEnd;
    private LocalDateTime notificationTime;
    private String location;

    /**
     * Constructs a new ReservationNotificationDto.
     */
    public ReservationNotificationDto() {
        this.notificationTime = LocalDateTime.now();
    }

    /**
     * Constructs a new ReservationNotificationDto with essential information.
     *
     * @param reservationId the reservation ID
     * @param userId the user ID (recipient)
     * @param listingId the listing ID
     * @param listingTitle the listing title
     * @param status the current reservation status
     * @param notificationType the type of notification
     * @param message the human-readable message
     * @param pickupWindowStart the pickup window start time
     * @param pickupWindowEnd the pickup window end time
     * @param location the pickup location
     */
    public ReservationNotificationDto(Long reservationId, Long userId, Long listingId, String listingTitle,
                                     String status, String notificationType, String message,
                                     LocalDateTime pickupWindowStart, LocalDateTime pickupWindowEnd, String location) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.listingId = listingId;
        this.listingTitle = listingTitle;
        this.status = status;
        this.notificationType = notificationType;
        this.message = message;
        this.pickupWindowStart = pickupWindowStart;
        this.pickupWindowEnd = pickupWindowEnd;
        this.location = location;
        this.notificationTime = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getPickupWindowStart() {
        return pickupWindowStart;
    }

    public void setPickupWindowStart(LocalDateTime pickupWindowStart) {
        this.pickupWindowStart = pickupWindowStart;
    }

    public LocalDateTime getPickupWindowEnd() {
        return pickupWindowEnd;
    }

    public void setPickupWindowEnd(LocalDateTime pickupWindowEnd) {
        this.pickupWindowEnd = pickupWindowEnd;
    }

    public LocalDateTime getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(LocalDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "ReservationNotificationDto{" +
                "reservationId=" + reservationId +
                ", userId=" + userId +
                ", listingId=" + listingId +
                ", status='" + status + '\'' +
                ", notificationType='" + notificationType + '\'' +
                '}';
    }
}
