package com.greenloop.reservation;

import com.greenloop.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public Reservation createReservation(@RequestBody CreateReservationRequest request) {
        Long userId = resolveUserId(request.getUserId());
        return reservationService.createReservation(request.getListingId(), userId);
    }

    private Long resolveUserId(Long userIdFromRequest) {
        if (userIdFromRequest != null) {
            return userIdFromRequest;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        return 1L; // default fallback
    }
}