package com.greenloop.analytics;

import com.greenloop.model.Listing;
import com.greenloop.reservation.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardListingDto {

    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final String imageUrl;
    private final Integer quantity;
    private final String unit;
    private final BigDecimal originalPrice;
    private final BigDecimal discountedPrice;
    private final String dietaryInfo;
    private final String allergens;
    private final String pickupAddress;
    private final String pickupCity;
    private final String pickupState;
    private final String pickupZipCode;
    private final Double pickupLatitude;
    private final Double pickupLongitude;
    private final LocalDateTime pickupWindowStart;
    private final LocalDateTime pickupWindowEnd;
    private final String status;
    private final Integer reservationCount;
    private final BigDecimal co2SavedKg;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private final List<DashboardReservationDto> reservations;

    private DashboardListingDto(Listing l, List<DashboardReservationDto> reservations) {
        this.id = l.getId();
        this.title = l.getTitle();
        this.description = l.getDescription();
        this.category = l.getCategory() != null ? l.getCategory().name() : null;
        this.imageUrl = l.getImageUrl();
        this.quantity = l.getQuantity();
        this.unit = l.getUnit();
        this.originalPrice = l.getOriginalPrice();
        this.discountedPrice = l.getDiscountedPrice();
        this.dietaryInfo = l.getDietaryInfo();
        this.allergens = l.getAllergens();
        this.pickupAddress = l.getPickupAddress();
        this.pickupCity = l.getPickupCity();
        this.pickupState = l.getPickupState();
        this.pickupZipCode = l.getPickupZipCode();
        this.pickupLatitude = l.getPickupLatitude();
        this.pickupLongitude = l.getPickupLongitude();
        this.pickupWindowStart = l.getPickupWindowStart();
        this.pickupWindowEnd = l.getPickupWindowEnd();
        this.status = l.getStatus() != null ? l.getStatus().name() : null;
        this.reservationCount = l.getReservationCount();
        this.co2SavedKg = l.getCo2SavedKg();
        this.expiresAt = l.getExpiresAt();
        this.createdAt = l.getCreatedAt();
        this.reservations = reservations;
    }

    public static DashboardListingDto from(Listing listing, List<Reservation> reservations) {
        List<DashboardReservationDto> resDtos = reservations.stream()
                .map(r -> {
                    DashboardReservationDto.ReservedByDto reservedBy = null;
                    if (r.getUser() != null) {
                        reservedBy = new DashboardReservationDto.ReservedByDto(
                                r.getUser().getId(),
                                r.getUser().getName(),
                                r.getUser().getEmail()
                        );
                    }
                    return new DashboardReservationDto(
                            r.getId(),
                            r.getStatus(),
                            r.getPickupCode(),
                            r.getQuantityReserved(),
                            r.getCreatedAt(),
                            r.getCollectedAt(),
                            reservedBy
                    );
                })
                .collect(Collectors.toList());
        return new DashboardListingDto(listing, resDtos);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public Integer getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public BigDecimal getDiscountedPrice() { return discountedPrice; }
    public String getDietaryInfo() { return dietaryInfo; }
    public String getAllergens() { return allergens; }
    public String getPickupAddress() { return pickupAddress; }
    public String getPickupCity() { return pickupCity; }
    public String getPickupState() { return pickupState; }
    public String getPickupZipCode() { return pickupZipCode; }
    public Double getPickupLatitude() { return pickupLatitude; }
    public Double getPickupLongitude() { return pickupLongitude; }
    public LocalDateTime getPickupWindowStart() { return pickupWindowStart; }
    public LocalDateTime getPickupWindowEnd() { return pickupWindowEnd; }
    public String getStatus() { return status; }
    public Integer getReservationCount() { return reservationCount; }
    public BigDecimal getCo2SavedKg() { return co2SavedKg; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<DashboardReservationDto> getReservations() { return reservations; }
}
