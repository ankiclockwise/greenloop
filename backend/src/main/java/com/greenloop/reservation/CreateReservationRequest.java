package com.greenloop.reservation;

public class CreateReservationRequest {
    private Long listingId;
    private Long userId;

    public CreateReservationRequest() {}

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}