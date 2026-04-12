package com.greenloop.model;

public enum UserRole {
    CONSUMER("consumer"),
    RETAILER("retailer"),
    DINING_HALL("dining_hall"),
    DONOR("donor"),
    ADMIN("admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String value) {
        if (value == null || value.isEmpty()) {
            return CONSUMER;
        }
        try {
            return UserRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CONSUMER;
        }
    }
}
