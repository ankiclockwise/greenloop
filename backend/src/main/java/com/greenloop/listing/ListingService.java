package com.greenloop.listing;

import com.greenloop.auth.exception.UserNotFoundException;
import com.greenloop.model.Listing;
import com.greenloop.model.ListingStatus;
import com.greenloop.model.User;
import com.greenloop.repository.UserRepository;
import com.greenloop.realtime.ListingEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ListingEventPublisher listingEventPublisher;

    public ListingService(ListingRepository listingRepository,
            UserRepository userRepository,
            ListingEventPublisher listingEventPublisher) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.listingEventPublisher = listingEventPublisher;
    }

    public Listing createListing(Listing listing, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Owner not found with id: " + ownerId));

        validatePickupWindow(listing);

        listing.setOwner(owner);
        listing.setStatus(ListingStatus.AVAILABLE);

        Listing savedListing = listingRepository.save(listing);
        listingEventPublisher.publishNewListing(savedListing);
        return savedListing;
    }

    public List<Listing> getAvailableListings() {
        return listingRepository.findAvailable();
    }

    public List<Listing> getListingsByOwner(Long ownerId) {
        return listingRepository.findByOwnerId(ownerId);
    }

    public Listing getListingById(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with id: " + listingId));
    }

    private void validatePickupWindow(Listing listing) {
        if (listing.getPickupWindowStart() == null || listing.getPickupWindowEnd() == null) {
            throw new IllegalArgumentException("Pickup window start and end are required");
        }

        if (listing.getPickupWindowStart().isAfter(listing.getPickupWindowEnd())) {
            throw new IllegalArgumentException("Pickup window start cannot be after pickup window end");
        }

        if (listing.getPickupWindowEnd().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Pickup window cannot be in the past");
        }

        if (listing.getExpiresAt() == null) {
            throw new IllegalArgumentException("Expiry time is required");
        }

        if (listing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiry time cannot be in the past");
        }
    }
}