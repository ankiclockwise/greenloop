package com.greenloop.impact.dto;

public class LeaderboardItemDto {
    private int rank;
    private String userId;
    private String userType;
    private String name;

    // Student-only fields
    private Integer foodReceived;
    private Integer pickupCount;

    // Donor-only fields
    private Integer completedPickups;

    // Shared fields
    private int foodDonated;
    private int donationCount;
    private double co2SavedKg;

    private LeaderboardItemDto() {}

    public static LeaderboardItemDto forDonor(int rank, String userId, String userType, String name,
                                              int foodDonated, int donationCount,
                                              int completedPickups, double co2SavedKg) {
        LeaderboardItemDto dto = new LeaderboardItemDto();
        dto.rank = rank;
        dto.userId = userId;
        dto.userType = userType;
        dto.name = name;
        dto.foodDonated = foodDonated;
        dto.donationCount = donationCount;
        dto.completedPickups = completedPickups;
        dto.co2SavedKg = co2SavedKg;
        return dto;
    }

    public static LeaderboardItemDto forStudent(int rank, String userId, String name,
                                                int foodReceived, int pickupCount,
                                                int foodDonated, int donationCount, double co2SavedKg) {
        LeaderboardItemDto dto = new LeaderboardItemDto();
        dto.rank = rank;
        dto.userId = userId;
        dto.userType = "retail_user";
        dto.name = name;
        dto.foodReceived = foodReceived;
        dto.pickupCount = pickupCount;
        dto.foodDonated = foodDonated;
        dto.donationCount = donationCount;
        dto.co2SavedKg = co2SavedKg;
        return dto;
    }

    public int getRank() { return rank; }
    public String getUserId() { return userId; }
    public String getUserType() { return userType; }
    public String getName() { return name; }
    public Integer getFoodReceived() { return foodReceived; }
    public Integer getPickupCount() { return pickupCount; }
    public Integer getCompletedPickups() { return completedPickups; }
    public int getFoodDonated() { return foodDonated; }
    public int getDonationCount() { return donationCount; }
    public double getCo2SavedKg() { return co2SavedKg; }
}
