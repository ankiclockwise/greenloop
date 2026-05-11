package com.greenloop.impact;

import java.util.List;

public enum BadgeDefinition {

    FIRST_DONATION("first_donation", "First Donation",
        "Donated food for the first time.",
        List.of("retail_user", "store", "diner"), "foodDonated", 1, false),

    COMMUNITY_HELPER("community_helper", "Community Helper",
        "Shared 10 or more food donations.",
        List.of("retail_user", "store", "diner"), "donationCount", 10, false),

    WASTE_WARRIOR("waste_warrior", "Waste Warrior",
        "Helped save 50kg or more of CO2.",
        List.of("retail_user", "store", "diner"), "co2SavedKg", 50, false),

    TOP_CONTRIBUTOR("top_contributor", "Top Contributor",
        "Reached the top 10 in your leaderboard.",
        List.of("retail_user", "store", "diner"), "leaderboardPosition", 10, true),

    GREENLOOP_CHAMPION("greenloop_champion", "GreenLoop Champion",
        "Saved 500kg or more of CO2 through GreenLoop activity.",
        List.of("retail_user", "store", "diner"), "co2SavedKg", 500, false),

    FIRST_PICKUP("first_pickup", "First Pickup",
        "Reserved and received food for the first time.",
        List.of("retail_user"), "foodReceived", 1, false),

    CAMPUS_RESCUER("campus_rescuer", "Campus Rescuer",
        "Received food 10 or more times.",
        List.of("retail_user"), "pickupCount", 10, false),

    RELIABLE_DONOR("reliable_donor", "Reliable Donor",
        "Listed or donated food every week for 4 weeks.",
        List.of("store", "diner"), "weeklyDonationStreak", 4, false),

    NEIGHBORHOOD_HERO("neighborhood_hero", "Neighborhood Hero",
        "Helped complete 50 or more student pickups.",
        List.of("store", "diner"), "completedPickups", 50, false);

    public final String id;
    public final String name;
    public final String description;
    public final List<String> appliesTo;
    public final String metric;
    public final double threshold;
    // true means earned when value <= threshold (e.g. leaderboardPosition <= 10)
    public final boolean lessThanOrEqual;

    BadgeDefinition(String id, String name, String description,
                    List<String> appliesTo, String metric, double threshold,
                    boolean lessThanOrEqual) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.appliesTo = appliesTo;
        this.metric = metric;
        this.threshold = threshold;
        this.lessThanOrEqual = lessThanOrEqual;
    }

    public boolean appliesTo(String userType) {
        return appliesTo.contains(userType);
    }
}
