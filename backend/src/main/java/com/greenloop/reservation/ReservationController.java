package com.greenloop.reservation;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public Reservation createReservation(@RequestParam Long listingId,
            @RequestParam Long userId) {
        return reservationService.createReservation(listingId, userId);
    }
}