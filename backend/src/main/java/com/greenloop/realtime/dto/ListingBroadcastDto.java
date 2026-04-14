package com.greenloop.realtime.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Data Transfer Object for broadcasting listing updates via WebSocket.
 *
 * Sent to all connected clients when new listings are created or existing listings are updated.
 * Optimized for network transmission with essential listing information.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
public class ListingBroadcastDto {

    private Long listingId;
    private String title;
    private String description;
    private String category;
    private String status; // AVAILABLE, RESERVED, COLLECTED, EXPIRED
    private BigDecimal estimatedValue;
    private String imageUrl;
    private Long donorId;
    private String donorName;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime pickupWindowStart;
    private LocalDateTime pickupWindowEnd;
    private String eventType; // NEW or UPDATE

    /**
     * Constructs a new ListingBroadcastDto.
     */
    public ListingBroadcastDto() {
    }

    /**
     * Constructs a new ListingBroadcastDto with essential listing information.
     *
     * @param listingId the listing ID
     * @param title the listing title
     * @param description the listing description
     * @param category the listing category
     * @param status the listing status
     * @param estimatedValue the estimated value
     * @param imageUrl the image URL
     * @param donorId the donor ID
     * @param donorName the donor name
     * @param location the pickup location
     * @param createdAt the creation timestamp
     * @param pickupWindowStart the pickup window start time
     * @param pickupWindowEnd the pickup window end time
     * @param eventType the event type (NEW or UPDATE)
     */
    public ListingBroadcastDto(Long listingId, String title, String description, String category,
                              String status, BigDecimal estimatedValue, String imageUrl,
                              Long donorId, String donorName, String location, LocalDateTime createdAt,
                              LocalDateTime pickupWindowStart, LocalDateTime pickupWindowEnd, String eventType) {
        this.listingId = listingId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.estimatedValue = estimatedValue;
        this.imageUrl = imageUrl;
        this.donorId = donorId;
        this.donorName = donorName;
        this.location = location;
        this.createdAt = createdAt;
        this.pickupWindowStart = pickupWindowStart;
        this.pickupWindowEnd = pickupWindowEnd;
        this.eventType = eventType;
    }

    // Getters and Setters

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "ListingBroadcastDto{" +
                "listingId=" + listingId +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", donorName='" + donorName + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
