package com.greenloop.impact.dto;

import java.util.List;

public class ImpactMeResponse {
    private String userType;
    private Integer foodReceived;
    private int foodDonated;
    private int donationCount;
    private Integer pickupCount;
    private Integer completedPickups;
    private double co2SavedKg;
    private int leaderboardPosition;
    private List<BadgeDto> badges;

    public ImpactMeResponse(String userType, Integer foodReceived, int foodDonated,
                            int donationCount, Integer pickupCount, Integer completedPickups,
                            double co2SavedKg, int leaderboardPosition, List<BadgeDto> badges) {
        this.userType = userType;
        this.foodReceived = foodReceived;
        this.foodDonated = foodDonated;
        this.donationCount = donationCount;
        this.pickupCount = pickupCount;
        this.completedPickups = completedPickups;
        this.co2SavedKg = co2SavedKg;
        this.leaderboardPosition = leaderboardPosition;
        this.badges = badges;
    }

    public String getUserType() { return userType; }
    public Integer getFoodReceived() { return foodReceived; }
    public int getFoodDonated() { return foodDonated; }
    public int getDonationCount() { return donationCount; }
    public Integer getPickupCount() { return pickupCount; }
    public Integer getCompletedPickups() { return completedPickups; }
    public double getCo2SavedKg() { return co2SavedKg; }
    public int getLeaderboardPosition() { return leaderboardPosition; }
    public List<BadgeDto> getBadges() { return badges; }
}
