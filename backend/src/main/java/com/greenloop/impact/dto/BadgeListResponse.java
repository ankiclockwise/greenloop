package com.greenloop.impact.dto;

import java.util.List;

public class BadgeListResponse {
    private List<BadgeDto> badges;

    public BadgeListResponse(List<BadgeDto> badges) {
        this.badges = badges;
    }

    public List<BadgeDto> getBadges() { return badges; }
}
