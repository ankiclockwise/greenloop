package com.greenloop.impact;

import com.greenloop.impact.dto.*;
import com.greenloop.model.Listing;
import com.greenloop.model.User;
import com.greenloop.model.UserRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImpactService {

    private static final List<UserRole> DONOR_ROLES = List.of(UserRole.RETAILER, UserRole.DINING_HALL, UserRole.DONOR);
    private static final List<UserRole> STUDENT_ROLES = List.of(UserRole.CONSUMER);

    private final UserImpactRepository userImpactRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserSyncService userSyncService;

    public ImpactService(UserImpactRepository userImpactRepository,
                         UserBadgeRepository userBadgeRepository,
                         UserSyncService userSyncService) {
        this.userImpactRepository = userImpactRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userSyncService = userSyncService;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    @Transactional
    public ImpactMeResponse getMyImpact(String email, String name, String frontendUserType) {
        User user = getOrCreateUser(email, name, frontendUserType);
        UserImpact stats = getOrCreate(user);
        String userType = toFrontendType(user.getRole());
        int rank = computeRank(user, stats);
        List<BadgeDto> earnedBadges = evaluateAndPersistBadges(user, stats, userType, rank)
                .stream().filter(BadgeDto::isEarned).collect(Collectors.toList());

        return buildMeResponse(userType, stats, rank, earnedBadges);
    }

    @Transactional
    public BadgeListResponse getAllBadges(String email, String name, String frontendUserType) {
        User user = getOrCreateUser(email, name, frontendUserType);

        UserImpact stats = getOrCreate(user);
        String userType = toFrontendType(user.getRole());
        int rank = computeRank(user, stats);
        List<BadgeDto> badges = evaluateAndPersistBadges(user, stats, userType, rank);

        return new BadgeListResponse(badges);
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getDonorLeaderboard(int page, int limit) {
        List<UserImpact> all = userImpactRepository.findByUserRolesOrderByCo2(DONOR_ROLES);
        int total = all.size();
        List<LeaderboardItemDto> items = paginateWithRank(all, page, limit, false);
        return new LeaderboardResponse(page, limit, total, items);
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getStudentLeaderboard(int page, int limit) {
        List<UserImpact> all = userImpactRepository.findByUserRolesOrderByCo2(STUDENT_ROLES);
        int total = all.size();
        List<LeaderboardItemDto> items = paginateWithRank(all, page, limit, true);
        return new LeaderboardResponse(page, limit, total, items);
    }

    // ── Called by other services ──────────────────────────────────────────────

    @Transactional
    public void recordListingCreated(User owner, Listing listing) {
        UserImpact stats = getOrCreate(owner);
        stats.setDonationCount(stats.getDonationCount() + 1);
        stats.setFoodDonated(stats.getFoodDonated() + listing.getQuantity());
        userImpactRepository.save(stats);
    }

    @Transactional
    public void recordReservationCollected(Listing listing, User receiver) {
        User donor = listing.getOwner();
        int qty = listing.getQuantity();
        BigDecimal co2 = listing.getCo2SavedKg() != null
                ? listing.getCo2SavedKg().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Update donor impact
        UserImpact donorStats = getOrCreate(donor);
        donorStats.setCompletedPickups(donorStats.getCompletedPickups() + 1);
        donorStats.setCo2SavedKg(donorStats.getCo2SavedKg().add(co2));
        userImpactRepository.save(donorStats);

        // Update receiver impact (only for students)
        if (receiver.getRole() == UserRole.CONSUMER) {
            UserImpact receiverStats = getOrCreate(receiver);
            receiverStats.setFoodReceived(receiverStats.getFoodReceived() + qty);
            receiverStats.setPickupCount(receiverStats.getPickupCount() + 1);
            receiverStats.setCo2SavedKg(receiverStats.getCo2SavedKg().add(co2));
            userImpactRepository.save(receiverStats);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private User getOrCreateUser(String email, String name, String frontendUserType) {
        try {
            return userSyncService.getOrCreate(email, name, fromFrontendType(frontendUserType));
        } catch (DataIntegrityViolationException e) {
            return userSyncService.findByEmail(email).orElseThrow();
        }
    }

    private static UserRole fromFrontendType(String frontendType) {
        if (frontendType == null) return UserRole.CONSUMER;
        return switch (frontendType) {
            case "store" -> UserRole.RETAILER;
            case "diner" -> UserRole.DINING_HALL;
            default -> UserRole.CONSUMER;
        };
    }

    private UserImpact getOrCreate(User user) {
        try {
            return userSyncService.getOrCreateImpact(user);
        } catch (DataIntegrityViolationException e) {
            return userSyncService.findImpactByUserId(user.getId()).orElseThrow();
        }
    }

    private int computeRank(User user, UserImpact stats) {
        List<UserRole> roles = DONOR_ROLES.contains(user.getRole()) ? DONOR_ROLES : STUDENT_ROLES;
        List<UserImpact> sorted = userImpactRepository.findByUserRolesOrderByCo2(roles);
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getUser().getId().equals(user.getId())) {
                return i + 1;
            }
        }
        return sorted.size() + 1;
    }

    private List<BadgeDto> evaluateAndPersistBadges(User user, UserImpact stats,
                                                     String userType, int rank) {
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
            case "foodDonated" -> stats.getFoodDonated();
            case "foodReceived" -> stats.getFoodReceived();
            case "donationCount" -> stats.getDonationCount();
            case "pickupCount" -> stats.getPickupCount();
            case "completedPickups" -> stats.getCompletedPickups();
            case "co2SavedKg" -> stats.getCo2SavedKg().doubleValue();
            case "leaderboardPosition" -> rank;
            case "weeklyDonationStreak" -> 0; // computed by carbon tracking feature
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

    private ImpactMeResponse buildMeResponse(String userType, UserImpact stats,
                                              int rank, List<BadgeDto> earnedBadges) {
        boolean isStudent = "retail_user".equals(userType);
        boolean isDonor = !isStudent;

        return new ImpactMeResponse(
                userType,
                isStudent ? stats.getFoodReceived() : null,
                stats.getFoodDonated(),
                stats.getDonationCount(),
                isStudent ? stats.getPickupCount() : null,
                isDonor ? stats.getCompletedPickups() : null,
                round1(stats.getCo2SavedKg().doubleValue()),
                rank,
                earnedBadges
        );
    }

    private List<LeaderboardItemDto> paginateWithRank(List<UserImpact> all,
                                                       int page, int limit, boolean student) {
        int from = Math.max(0, (page - 1) * limit);
        int to = Math.min(all.size(), from + limit);
        List<LeaderboardItemDto> items = new ArrayList<>();

        for (int i = from; i < to; i++) {
            UserImpact ui = all.get(i);
            User u = ui.getUser();
            int rank = i + 1;
            double co2 = round1(ui.getCo2SavedKg().doubleValue());

            if (student) {
                items.add(LeaderboardItemDto.forStudent(
                        rank, String.valueOf(u.getId()), u.getName(),
                        ui.getFoodReceived(), ui.getPickupCount(),
                        ui.getFoodDonated(), ui.getDonationCount(), co2));
            } else {
                items.add(LeaderboardItemDto.forDonor(
                        rank, String.valueOf(u.getId()), toFrontendType(u.getRole()), u.getName(),
                        ui.getFoodDonated(), ui.getDonationCount(),
                        ui.getCompletedPickups(), co2));
            }
        }

        return items;
    }

    public static String toFrontendType(UserRole role) {
        return switch (role) {
            case CONSUMER -> "retail_user";
            case RETAILER -> "store";
            case DINING_HALL -> "diner";
            default -> "store";
        };
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
