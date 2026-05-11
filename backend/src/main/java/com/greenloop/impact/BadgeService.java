package com.greenloop.impact;

import com.greenloop.impact.dto.BadgeDto;
import com.greenloop.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BadgeService {

    private final UserBadgeRepository userBadgeRepository;

    public BadgeService(UserBadgeRepository userBadgeRepository) {
        this.userBadgeRepository = userBadgeRepository;
    }

    @Transactional
    public List<BadgeDto> evaluateAndPersist(User user, UserImpact stats, String userType, int rank) {
        Map<String, UserBadge> earned = userBadgeRepository.findByUserId(user.getId())
                .stream().collect(Collectors.toMap(UserBadge::getBadgeId, b -> b));

        List<BadgeDto> result = new ArrayList<>();

        for (BadgeDefinition def : BadgeDefinition.values()) {
            if (!def.appliesTo(userType)) {
                continue;
            }
            double metricValue = resolveMetric(def.metric, stats, rank);
            boolean meetsThreshold = def.lessThanOrEqual
                    ? metricValue <= def.threshold
                    : metricValue >= def.threshold;

            String earnedAt = null;
            if (meetsThreshold) {
                UserBadge badge = earned.get(def.id);
                if (badge == null) {
                    badge = userBadgeRepository.save(UserBadge.earn(user, def.id));
                    earned.put(def.id, badge);
                }
                earnedAt = badge.getEarnedAt().atOffset(ZoneOffset.UTC).toString();
            }

            result.add(new BadgeDto(
                    def.id, def.name, def.description, def.appliesTo,
                    buildCriteria(def), meetsThreshold, earnedAt
            ));
        }

        return result;
    }

    private double resolveMetric(String metric, UserImpact stats, int rank) {
        return switch (metric) {
            case "foodDonated"        -> stats.getFoodDonated();
            case "foodReceived"       -> stats.getFoodReceived();
            case "donationCount"      -> stats.getDonationCount();
            case "pickupCount"        -> stats.getPickupCount();
            case "completedPickups"   -> stats.getCompletedPickups();
            case "co2SavedKg"         -> stats.getCo2SavedKg().doubleValue();
            case "leaderboardPosition"-> rank;
            case "weeklyDonationStreak" -> 0; // placeholder until carbon tracking feature
            default -> 0;
        };
    }

    private Map<String, Object> buildCriteria(BadgeDefinition def) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        criteria.put("metric", def.metric);
        criteria.put("threshold", (int) def.threshold);
        if (def.lessThanOrEqual) {
            criteria.put("comparison", "lessThanOrEqual");
        }
        return criteria;
    }
}
