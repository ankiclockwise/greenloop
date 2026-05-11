package com.greenloop.tests.analytics;

import com.greenloop.analytics.AnalyticsService;
import com.greenloop.analytics.BusinessAnalyticsResponse;
import com.greenloop.analytics.DashboardListingDto;
import com.greenloop.listing.ListingRepository;
import com.greenloop.model.Listing;
import com.greenloop.model.ListingCategory;
import com.greenloop.model.ListingStatus;
import com.greenloop.model.User;
import com.greenloop.model.UserRole;
import com.greenloop.repository.UserRepository;
import com.greenloop.reservation.Reservation;
import com.greenloop.reservation.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ListingRepository listingRepository;
    @Mock private ReservationRepository reservationRepository;

    private AnalyticsService analyticsService;

    private User owner;

    @BeforeEach
    void setup() {
        analyticsService = new AnalyticsService(userRepository, listingRepository, reservationRepository);

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@test.com");
        owner.setRole(UserRole.RETAILER);
    }

    // --- getBusinessAnalytics ---

    @Test
    void getBusinessAnalytics_unknownUser_returnsEmptyResponse() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        BusinessAnalyticsResponse response = analyticsService.getBusinessAnalytics("unknown@test.com");

        assertEquals(0, response.getTotalListings());
        assertEquals(0, response.getRescuedListings());
        assertEquals(0.0, response.getRescueRate());
        assertEquals(0.0, response.getRevenueRecovered());
        assertTrue(response.getPeakPickupSlots().isEmpty());
    }

    @Test
    void getBusinessAnalytics_noListings_returnsZeros() {
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(listingRepository.findByOwnerId(1L)).thenReturn(List.of());

        BusinessAnalyticsResponse response = analyticsService.getBusinessAnalytics("owner@test.com");

        assertEquals(0, response.getTotalListings());
        assertEquals(0.0, response.getRescueRate());
    }

    @Test
    void getBusinessAnalytics_countsCorrectly() {
        Listing collected = listing(ListingStatus.COLLECTED, LocalDateTime.now().plusHours(1));
        collected.setDiscountedPrice(new BigDecimal("5.00"));
        collected.setQuantity(2);

        Listing available = listing(ListingStatus.AVAILABLE, LocalDateTime.now().plusHours(2));
        Listing expired = listing(ListingStatus.EXPIRED, LocalDateTime.now().minusHours(1));

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(listingRepository.findByOwnerId(1L)).thenReturn(List.of(collected, available, expired));

        BusinessAnalyticsResponse response = analyticsService.getBusinessAnalytics("owner@test.com");

        assertEquals(3, response.getTotalListings());
        assertEquals(1, response.getRescuedListings());
        assertEquals(1, response.getExpiredListings());
        assertEquals(33.3, response.getRescueRate());
        assertEquals(10.0, response.getRevenueRecovered());
    }

    @Test
    void getBusinessAnalytics_rescueRate_roundsToOneDecimal() {
        List<Listing> listings = List.of(
                listing(ListingStatus.COLLECTED, LocalDateTime.now().plusHours(1)),
                listing(ListingStatus.AVAILABLE, LocalDateTime.now().plusHours(1)),
                listing(ListingStatus.AVAILABLE, LocalDateTime.now().plusHours(1))
        );

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(listingRepository.findByOwnerId(1L)).thenReturn(listings);

        BusinessAnalyticsResponse response = analyticsService.getBusinessAnalytics("owner@test.com");

        assertEquals(33.3, response.getRescueRate());
    }

    @Test
    void getBusinessAnalytics_peakSlots_sortedByCountDescending() {
        Listing l1 = listing(ListingStatus.AVAILABLE, LocalDateTime.of(2026, 5, 1, 10, 0));
        Listing l2 = listing(ListingStatus.AVAILABLE, LocalDateTime.of(2026, 5, 1, 10, 0));
        Listing l3 = listing(ListingStatus.AVAILABLE, LocalDateTime.of(2026, 5, 1, 14, 0));

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(listingRepository.findByOwnerId(1L)).thenReturn(List.of(l1, l2, l3));

        BusinessAnalyticsResponse response = analyticsService.getBusinessAnalytics("owner@test.com");

        assertEquals(2, response.getPeakPickupSlots().size());
        assertEquals(10, response.getPeakPickupSlots().get(0).getHour());
        assertEquals(2, response.getPeakPickupSlots().get(0).getCount());
        assertEquals(14, response.getPeakPickupSlots().get(1).getHour());
    }

    @Test
    void getBusinessAnalytics_cancelledListings_excludedFromPeakSlots() {
        Listing cancelled = listing(ListingStatus.CANCELLED, LocalDateTime.of(2026, 5, 1, 9, 0));
        Listing available = listing(ListingStatus.AVAILABLE, LocalDateTime.of(2026, 5, 1, 11, 0));

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(listingRepository.findByOwnerId(1L)).thenReturn(List.of(cancelled, available));

        BusinessAnalyticsResponse response = analyticsService.getBusinessAnalytics("owner@test.com");

        assertEquals(1, response.getPeakPickupSlots().size());
        assertEquals(11, response.getPeakPickupSlots().get(0).getHour());
    }

    @Test
    void getBusinessAnalytics_revenueUsesOriginalPriceWhenNoDiscount() {
        Listing collected = listing(ListingStatus.COLLECTED, LocalDateTime.now().plusHours(1));
        collected.setOriginalPrice(new BigDecimal("8.00"));
        collected.setDiscountedPrice(null);
        collected.setQuantity(3);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(listingRepository.findByOwnerId(1L)).thenReturn(List.of(collected));

        BusinessAnalyticsResponse response = analyticsService.getBusinessAnalytics("owner@test.com");

        assertEquals(24.0, response.getRevenueRecovered());
    }

    // --- getDashboardListings ---

    @Test
    void getDashboardListings_noListings_returnsEmpty() {
        when(listingRepository.findByOwnerId(1L)).thenReturn(List.of());

        List<DashboardListingDto> result = analyticsService.getDashboardListings(1L);

        assertTrue(result.isEmpty());
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void getDashboardListings_listingsWithReservations() {
        Listing l = listing(ListingStatus.RESERVED, LocalDateTime.now().plusHours(1));

        User reserver = new User();
        reserver.setId(2L);
        reserver.setName("Jane");
        reserver.setEmail("jane@test.com");

        Reservation res = new Reservation();
        res.setListing(l);
        res.setUser(reserver);
        res.setStatus("RESERVED");
        res.setQuantityReserved(1);
        res.setPickupCode("GRN-ABC12345");

        when(listingRepository.findByOwnerId(1L)).thenReturn(List.of(l));
        when(reservationRepository.findByListingIdsWithUser(anyList())).thenReturn(List.of(res));

        List<DashboardListingDto> result = analyticsService.getDashboardListings(1L);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getReservations().size());
        assertEquals("Jane", result.get(0).getReservations().get(0).getReservedBy().getName());
        assertEquals("GRN-ABC12345", result.get(0).getReservations().get(0).getPickupCode());
    }

    @Test
    void getDashboardListings_listingWithNoReservations() {
        Listing l = listing(ListingStatus.AVAILABLE, LocalDateTime.now().plusHours(1));

        when(listingRepository.findByOwnerId(1L)).thenReturn(List.of(l));
        when(reservationRepository.findByListingIdsWithUser(anyList())).thenReturn(List.of());

        List<DashboardListingDto> result = analyticsService.getDashboardListings(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getReservations().isEmpty());
    }

    // --- helpers ---

    private long idSeq = 1;

    private Listing listing(ListingStatus status, LocalDateTime pickupStart) {
        Listing l = new Listing();
        setId(l, idSeq++);
        l.setOwner(owner);
        l.setStatus(status);
        l.setQuantity(1);
        l.setPickupWindowStart(pickupStart);
        l.setPickupWindowEnd(pickupStart.plusHours(1));
        l.setExpiresAt(pickupStart.plusHours(2));
        l.setCategory(ListingCategory.PRODUCE);
        return l;
    }

    private static void setId(Listing listing, long id) {
        try {
            java.lang.reflect.Field f = Listing.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(listing, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
