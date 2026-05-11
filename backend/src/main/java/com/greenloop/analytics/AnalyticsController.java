package com.greenloop.analytics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/business")
    public BusinessAnalyticsResponse getBusinessAnalytics(@RequestParam String ownerEmail) {
        return analyticsService.getBusinessAnalytics(ownerEmail);
    }

    @GetMapping("/dashboard/listings/{ownerId}")
    public List<DashboardListingDto> getDashboardListings(@PathVariable Long ownerId) {
        return analyticsService.getDashboardListings(ownerId);
    }
}
