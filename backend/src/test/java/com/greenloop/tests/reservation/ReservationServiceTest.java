package com.greenloop.tests.reservation;

import com.greenloop.impact.ImpactService;
import com.greenloop.listing.ListingRepository;
import com.greenloop.model.Listing;
import com.greenloop.model.ListingStatus;
import com.greenloop.model.User;
import com.greenloop.repository.UserRepository;
import com.greenloop.reservation.Reservation;
import com.greenloop.reservation.ReservationRepository;
import com.greenloop.reservation.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ListingRepository listingRepository;
    @Mock private UserRepository userRepository;

    private final ImpactService noOpImpact = new ImpactService(null, null, null) {
        @Override public void recordListingCreated(User owner, Listing listing) {}
        @Override public void recordReservationCollected(Listing listing, User receiver) {}
    };

    private ReservationService reservationService;

    private User owner;
    private User reserver;
    private Listing listing;

    @BeforeEach
    void setup() {
        reservationService = new ReservationService(reservationRepository, listingRepository, userRepository, noOpImpact);

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@test.com");

        reserver = new User();
        reserver.setId(2L);
        reserver.setEmail("reserver@test.com");

        listing = new Listing();
        listing.setOwner(owner);
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(2));
    }

    @Test
    void createReservation_success() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reserver));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));
        when(listingRepository.save(any(Listing.class))).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation(10L, 2L);

        assertNotNull(result);
        assertEquals(reserver, result.getUser());
        assertNotNull(result.getPickupCode());
        assertTrue(result.getPickupCode().startsWith("GRN-"));
        assertEquals(1, result.getQuantityReserved());
        assertEquals(ListingStatus.RESERVED, listing.getStatus());
    }

    @Test
    void createReservation_listingNotFound_throws() {
        when(listingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(99L, 2L));
    }

    @Test
    void createReservation_userNotFound_throws() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(10L, 99L));
    }

    @Test
    void createReservation_listingNotAvailable_throws() {
        listing.setStatus(ListingStatus.RESERVED);
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reserver));

        assertThrows(IllegalStateException.class, () -> reservationService.createReservation(10L, 2L));
    }

    @Test
    void createReservation_selfReservation_throws() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reservationService.createReservation(10L, 1L));
        assertEquals("Cannot reserve your own listing", ex.getMessage());
    }

    @Test
    void createReservation_pickupCodeIsUnique() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reserver));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(listingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation r1 = reservationService.createReservation(10L, 2L);
        listing.setStatus(ListingStatus.AVAILABLE);
        Reservation r2 = reservationService.createReservation(10L, 2L);

        assertNotEquals(r1.getPickupCode(), r2.getPickupCode());
    }

    @Test
    void collectReservation_success() {
        Reservation reservation = new Reservation();
        reservation.setListing(listing);
        reservation.setUser(reserver);
        reservation.setStatus("RESERVED");

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(listingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.collectReservation(5L);

        assertEquals("COLLECTED", result.getStatus());
        assertNotNull(result.getCollectedAt());
        assertEquals(ListingStatus.COLLECTED, listing.getStatus());
    }

    @Test
    void collectReservation_notFound_throws() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reservationService.collectReservation(99L));
    }

    @Test
    void collectReservation_notReservedStatus_throws() {
        Reservation reservation = new Reservation();
        reservation.setStatus("COLLECTED");

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class, () -> reservationService.collectReservation(5L));
    }

    @Test
    void collectReservation_setsCollectedAt() {
        Reservation reservation = new Reservation();
        reservation.setListing(listing);
        reservation.setUser(reserver);
        reservation.setStatus("RESERVED");

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(listingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        reservationService.collectReservation(5L);

        assertTrue(reservation.getCollectedAt().isAfter(before));
        assertTrue(reservation.getCollectedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}
