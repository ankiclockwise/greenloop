package com.greenloop.reservation;

import com.greenloop.impact.ImpactService;
import com.greenloop.model.Listing;
import com.greenloop.model.ListingStatus;
import com.greenloop.model.User;
import com.greenloop.listing.ListingRepository;
import com.greenloop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ImpactService impactService;

    public ReservationService(ReservationRepository reservationRepository,
                              ListingRepository listingRepository,
                              UserRepository userRepository,
                              ImpactService impactService) {
        this.reservationRepository = reservationRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.impactService = impactService;
    }

    @Transactional
    public Reservation createReservation(Long listingId, Long userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!listing.getStatus().name().equals("AVAILABLE")) {
            throw new IllegalStateException("Listing is not available");
        }

        listing.setStatus(ListingStatus.RESERVED);
        listingRepository.save(listing);

        Reservation reservation = new Reservation();
        reservation.setListing(listing);
        reservation.setUser(user);
        reservation.setPickupWindowEnd(listing.getPickupWindowEnd());

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation collectReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        if (!"RESERVED".equals(reservation.getStatus())) {
            throw new IllegalStateException("Reservation is not in RESERVED status");
        }

        reservation.setStatus("COLLECTED");
        reservationRepository.save(reservation);

        Listing listing = reservation.getListing();
        listing.setStatus(ListingStatus.COLLECTED);
        listingRepository.save(listing);

        impactService.recordReservationCollected(listing, reservation.getUser());

        return reservation;
    }
}