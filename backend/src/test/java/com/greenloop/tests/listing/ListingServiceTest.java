package com.greenloop.tests.listing;

import com.greenloop.auth.exception.UserNotFoundException;
import com.greenloop.impact.ImpactService;
import com.greenloop.listing.ListingRepository;
import com.greenloop.listing.ListingService;
import com.greenloop.listing.ListingUpdateRequest;
import com.greenloop.model.Listing;
import com.greenloop.model.ListingStatus;
import com.greenloop.model.User;
import com.greenloop.realtime.ListingEventPublisher;
import com.greenloop.repository.UserRepository;
import com.greenloop.reservation.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingServiceTest {

    @Mock private ListingRepository listingRepository;
    @Mock private UserRepository userRepository;

    @Captor private ArgumentCaptor<Listing> listingCaptor;

    private ListingService listingService;
    private User owner;

    private final ListingEventPublisher noOpPublisher = new ListingEventPublisher(null) {
        @Override public void publishNewListing(Listing listing) {}
        @Override public void publishListingUpdate(Listing listing) {}
        @Override public void publishReservationConfirmation(Reservation reservation, Object user) {}
        @Override public void publishReservationCancellation(Reservation reservation, Object user, String reason) {}
        @Override public void publishNoShowNotification(Reservation reservation, Object user) {}
    };

    private final ImpactService noOpImpact = new ImpactService(null, null, null) {
        @Override public void recordListingCreated(User owner, Listing listing) {}
        @Override public void recordReservationCollected(Listing listing, User receiver) {}
    };

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(42L);
        owner.setEmail("owner@example.com");
        owner.setName("Owner");
        listingService = new ListingService(listingRepository, userRepository, noOpPublisher, noOpImpact);
    }

    @Test
    void createListing_success() {
        Listing listing = new Listing();
        listing.setTitle("Test");
        listing.setDescription("desc");
        listing.setQuantity(1);
        listing.setUnit("pkg");
        listing.setPickupWindowStart(LocalDateTime.now().plusHours(1));
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(2));
        listing.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(42L)).thenReturn(Optional.of(owner));
        when(listingRepository.save(any(Listing.class))).thenAnswer(i -> i.getArgument(0));

        Listing saved = listingService.createListing(listing, 42L);

        assertNotNull(saved);
        assertEquals(ListingStatus.AVAILABLE, saved.getStatus());
        verify(listingRepository, times(1)).save(listingCaptor.capture());
        assertEquals(owner, listingCaptor.getValue().getOwner());
    }

    @Test
    void createListing_userNotFound_throws() {
        Listing listing = new Listing();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> listingService.createListing(listing, 99L));
    }

    @Test
    void createListing_invalidPickupWindow_throws() {
        Listing listing = new Listing();
        listing.setPickupWindowStart(null);
        listing.setPickupWindowEnd(null);

        when(userRepository.findById(42L)).thenReturn(Optional.of(owner));

        assertThrows(IllegalArgumentException.class, () -> listingService.createListing(listing, 42L));
    }

    @Test
    void getListingById_notFound_throws() {
        when(listingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> listingService.getListingById(1L));
    }

    @Test
    void updateListing_appliesChanges() {
        Listing existing = new Listing();
        existing.setTitle("old");
        existing.setQuantity(1);
        existing.setPickupWindowStart(LocalDateTime.now().plusHours(1));
        existing.setPickupWindowEnd(LocalDateTime.now().plusHours(2));
        existing.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(listingRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(listingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ListingUpdateRequest req = new ListingUpdateRequest();
        req.setTitle("new title");
        req.setQuantity(10);

        Listing updated = listingService.updateListing(5L, req);

        assertEquals("new title", updated.getTitle());
        assertEquals(10, updated.getQuantity());
    }

    @Test
    void deleteListing_deletes() {
        Listing existing = new Listing();
        when(listingRepository.findById(7L)).thenReturn(Optional.of(existing));

        listingService.deleteListing(7L);

        verify(listingRepository, times(1)).delete(existing);
    }
}
