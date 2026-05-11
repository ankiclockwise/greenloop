package com.greenloop.analytics;

import com.greenloop.impact.ImpactService;
import com.greenloop.listing.ListingRepository;
import com.greenloop.model.Listing;
import com.greenloop.model.ListingStatus;
import com.greenloop.model.User;
import com.greenloop.repository.UserRepository;
import com.greenloop.reservation.Reservation;
import com.greenloop.reservation.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ReservationRepository reservationRepository;

    public AnalyticsService(UserRepository userRepository, ListingRepository listingRepository,
                            ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public BusinessAnalyticsResponse getBusinessAnalytics(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new BusinessAnalyticsResponse("retail_user", 0, 0, 0, 0.0, 0.0, List.of());
        }

        List<Listing> listings = listingRepository.findByOwnerId(user.getId());

        int total = listings.size();
        int rescued = (int) listings.stream().filter(l -> l.getStatus() == ListingStatus.COLLECTED).count();
        int expired = (int) listings.stream().filter(l -> l.getStatus() == ListingStatus.EXPIRED).count();

        double rescueRate = total == 0 ? 0.0 : Math.round((rescued * 100.0 / total) * 10.0) / 10.0;

        double revenueRecovered = listings.stream()
                .filter(l -> l.getStatus() == ListingStatus.COLLECTED)
                .mapToDouble(l -> {
                    BigDecimal price = l.getDiscountedPrice() != null ? l.getDiscountedPrice()
                            : l.getOriginalPrice() != null ? l.getOriginalPrice()
                            : BigDecimal.ZERO;
                    return price.doubleValue() * l.getQuantity();
                })
                .sum();
        revenueRecovered = Math.round(revenueRecovered * 100.0) / 100.0;

        List<PeakSlotDto> peakSlots = computePeakSlots(listings);

        String userType = ImpactService.toFrontendType(user.getRole());
        return new BusinessAnalyticsResponse(userType, total, rescued, expired,
                rescueRate, revenueRecovered, peakSlots);
    }

    @Transactional(readOnly = true)
    public List<DashboardListingDto> getDashboardListings(Long ownerId) {
        List<Listing> listings = listingRepository.findByOwnerId(ownerId);
        if (listings.isEmpty()) return List.of();

        List<Long> listingIds = listings.stream().map(Listing::getId).collect(Collectors.toList());
        List<Reservation> reservations = reservationRepository.findByListingIdsWithUser(listingIds);

        Map<Long, List<Reservation>> byListingId = reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getListing().getId()));

        return listings.stream()
                .map(l -> DashboardListingDto.from(l, byListingId.getOrDefault(l.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private List<PeakSlotDto> computePeakSlots(List<Listing> listings) {
        Map<Integer, Long> countByHour = listings.stream()
                .filter(l -> l.getPickupWindowStart() != null)
                .filter(l -> l.getStatus() != ListingStatus.CANCELLED)
                .collect(Collectors.groupingBy(
                        l -> l.getPickupWindowStart().getHour(),
                        Collectors.counting()
                ));

        return countByHour.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(e -> new PeakSlotDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
