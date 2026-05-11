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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImpactService {

    private static final List<UserRole> DONOR_ROLES = List.of(UserRole.RETAILER, UserRole.DINING_HALL, UserRole.DONOR);
    private static final List<UserRole> STUDENT_ROLES = List.of(UserRole.CONSUMER);

    private final UserImpactRepository userImpactRepository;
    private final UserSyncService userSyncService;
    private final BadgeService badgeService;

    public ImpactService(UserImpactRepository userImpactRepository,
                         UserSyncService userSyncService,
                         BadgeService badgeService) {
        this.userImpactRepository = userImpactRepository;
        this.userSyncService = userSyncService;
        this.badgeService = badgeService;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    @Transactional
    public ImpactMeResponse getMyImpact(String email, String name, String frontendUserType) {
        User user = getOrCreateUser(email, name, frontendUserType);
        UserImpact stats = getOrCreate(user);
        String userType = toFrontendType(user.getRole());
        int rank = computeRank(user);
        List<BadgeDto> earnedBadges = badgeService.evaluateAndPersist(user, stats, userType, rank)
                .stream().filter(BadgeDto::isEarned).collect(Collectors.toList());

        return buildMeResponse(user.getId(), userType, stats, rank, earnedBadges);
    }

    @Transactional
    public BadgeListResponse getAllBadges(String email, String name, String frontendUserType) {
        User user = getOrCreateUser(email, name, frontendUserType);
        UserImpact stats = getOrCreate(user);
        String userType = toFrontendType(user.getRole());
        int rank = computeRank(user);
        List<BadgeDto> badges = badgeService.evaluateAndPersist(user, stats, userType, rank);
        return new BadgeListResponse(badges);
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getDonorLeaderboard(int page, int limit) {
        List<UserImpact> all = userImpactRepository.findByUserRolesOrderByCo2(DONOR_ROLES);
        return new LeaderboardResponse(page, limit, all.size(), paginateWithRank(all, page, limit, false));
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getStudentLeaderboard(int page, int limit) {
        List<UserImpact> all = userImpactRepository.findByUserRolesOrderByCo2(STUDENT_ROLES);
        return new LeaderboardResponse(page, limit, all.size(), paginateWithRank(all, page, limit, true));
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
        BigDecimal co2 = listing.getCo2SavedKg() != null
                ? listing.getCo2SavedKg().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        UserImpact donorStats = getOrCreate(donor);
        donorStats.setCompletedPickups(donorStats.getCompletedPickups() + 1);
        donorStats.setCo2SavedKg(donorStats.getCo2SavedKg().add(co2));
        userImpactRepository.save(donorStats);

        if (receiver.getRole() == UserRole.CONSUMER) {
            UserImpact receiverStats = getOrCreate(receiver);
            receiverStats.setFoodReceived(receiverStats.getFoodReceived() + listing.getQuantity());
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

    private UserImpact getOrCreate(User user) {
        try {
            return userSyncService.getOrCreateImpact(user);
        } catch (DataIntegrityViolationException e) {
            return userSyncService.findImpactByUserId(user.getId()).orElseThrow();
        }
    }

    private int computeRank(User user) {
        List<UserRole> roles = DONOR_ROLES.contains(user.getRole()) ? DONOR_ROLES : STUDENT_ROLES;
        List<UserImpact> sorted = userImpactRepository.findByUserRolesOrderByCo2(roles);
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getUser().getId().equals(user.getId())) {
                return i + 1;
            }
        }
        return sorted.size() + 1;
    }

    private ImpactMeResponse buildMeResponse(Long userId, String userType, UserImpact stats,
                                              int rank, List<BadgeDto> earnedBadges) {
        boolean isStudent = "retail_user".equals(userType);
        return new ImpactMeResponse(
                userId,
                userType,
                isStudent ? stats.getFoodReceived() : null,
                stats.getFoodDonated(),
                stats.getDonationCount(),
                isStudent ? stats.getPickupCount() : null,
                isStudent ? null : stats.getCompletedPickups(),
                round1(stats.getCo2SavedKg().doubleValue()),
                rank,
                earnedBadges
        );
    }

    private List<LeaderboardItemDto> paginateWithRank(List<UserImpact> all, int page, int limit, boolean student) {
        int from = Math.max(0, (page - 1) * limit);
        int to = Math.min(all.size(), from + limit);

        return all.subList(from, to).stream().map(ui -> {
            User u = ui.getUser();
            int rank = all.indexOf(ui) + 1;
            double co2 = round1(ui.getCo2SavedKg().doubleValue());
            String userType = toFrontendType(u.getRole());

            return student
                    ? LeaderboardItemDto.forStudent(rank, String.valueOf(u.getId()), userType, u.getName(),
                        ui.getFoodReceived(), ui.getPickupCount(), ui.getFoodDonated(), ui.getDonationCount(), co2)
                    : LeaderboardItemDto.forDonor(rank, String.valueOf(u.getId()), userType, u.getName(),
                        ui.getFoodDonated(), ui.getDonationCount(), ui.getCompletedPickups(), co2);
        }).collect(Collectors.toList());
    }

    public static String toFrontendType(UserRole role) {
        return switch (role) {
            case CONSUMER    -> "retail_user";
            case RETAILER    -> "store";
            case DINING_HALL -> "diner";
            default          -> "store";
        };
    }

    private static UserRole fromFrontendType(String frontendType) {
        if (frontendType == null) return UserRole.CONSUMER;
        return switch (frontendType) {
            case "store" -> UserRole.RETAILER;
            case "diner" -> UserRole.DINING_HALL;
            default      -> UserRole.CONSUMER;
        };
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
