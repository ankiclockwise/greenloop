package com.greenloop.reservation;

import com.greenloop.model.Listing;
import com.greenloop.model.User;
import com.greenloop.listing.ListingRepository;
import com.greenloop.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ListingRepository listingRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    public Reservation createReservation(Long listingId, Long userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!listing.getStatus().name().equals("AVAILABLE")) {
            throw new IllegalStateException("Listing is not available");
        }

        listing.setStatus(com.greenloop.model.ListingStatus.RESERVED);

        Reservation reservation = new Reservation();
        reservation.setListing(listing);
        reservation.setUser(user);

        return reservationRepository.save(reservation);
    }
}