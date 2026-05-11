package com.greenloop.impact.dto;

import java.util.List;
import java.util.Map;

public class BadgeDto {
    private String id;
    private String name;
    private String description;
    private List<String> appliesTo;
    private Map<String, Object> criteria;
    private boolean earned;
    private String earnedAt;

    public BadgeDto(String id, String name, String description,
                    List<String> appliesTo, Map<String, Object> criteria,
                    boolean earned, String earnedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.appliesTo = appliesTo;
        this.criteria = criteria;
        this.earned = earned;
        this.earnedAt = earnedAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getAppliesTo() { return appliesTo; }
    public Map<String, Object> getCriteria() { return criteria; }
    public boolean isEarned() { return earned; }
    public String getEarnedAt() { return earnedAt; }
}
