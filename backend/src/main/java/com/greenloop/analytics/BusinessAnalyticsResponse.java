package com.greenloop.analytics;

import java.util.List;

public class BusinessAnalyticsResponse {
    private final String userType;
    private final int totalListings;
    private final int rescuedListings;
    private final int expiredListings;
    private final double rescueRate;
    private final double revenueRecovered;
    private final List<PeakSlotDto> peakPickupSlots;

    public BusinessAnalyticsResponse(String userType, int totalListings, int rescuedListings,
                                     int expiredListings, double rescueRate,
                                     double revenueRecovered, List<PeakSlotDto> peakPickupSlots) {
        this.userType = userType;
        this.totalListings = totalListings;
        this.rescuedListings = rescuedListings;
        this.expiredListings = expiredListings;
        this.rescueRate = rescueRate;
        this.revenueRecovered = revenueRecovered;
        this.peakPickupSlots = peakPickupSlots;
    }

    public String getUserType() { return userType; }
    public int getTotalListings() { return totalListings; }
    public int getRescuedListings() { return rescuedListings; }
    public int getExpiredListings() { return expiredListings; }
    public double getRescueRate() { return rescueRate; }
    public double getRevenueRecovered() { return revenueRecovered; }
    public List<PeakSlotDto> getPeakPickupSlots() { return peakPickupSlots; }
}
