package com.greenloop.tests.reservation;

import com.greenloop.reservation.Reservation;
import com.greenloop.reservation.ReservationRepository;
import com.greenloop.reservation.ReservationStatusMachine;
import com.greenloop.reservation.ReservationStatusMachine.InvalidStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationStatusMachineTest {

    @Mock private ReservationRepository reservationRepository;

    private ReservationStatusMachine statusMachine;

    @BeforeEach
    void setup() {
        statusMachine = new ReservationStatusMachine(reservationRepository);
    }

    // --- isTransitionValid ---

    @ParameterizedTest
    @CsvSource({
        "RESERVED,  COLLECTED, true",
        "RESERVED,  CANCELLED, true",
        "RESERVED,  NO_SHOW,   true",
        "RESERVED,  EXPIRED,   true",
        "COLLECTED, RESERVED,  false",
        "COLLECTED, CANCELLED, false",
        "CANCELLED, RESERVED,  false",
        "NO_SHOW,   RESERVED,  false",
        "EXPIRED,   RESERVED,  false"
    })
    void isTransitionValid_matrix(String from, String to, boolean expected) {
        assertEquals(expected, statusMachine.isTransitionValid(from, to));
    }

    @Test
    void isTransitionValid_unknownCurrentStatus_returnsFalse() {
        assertFalse(statusMachine.isTransitionValid("PENDING", "COLLECTED"));
    }

    @Test
    void isTransitionValid_invalidNewStatus_returnsFalse() {
        assertFalse(statusMachine.isTransitionValid("RESERVED", "FLYING"));
    }

    // --- transition ---

    @Test
    void transition_reservedToCollected_adminRole_succeeds() {
        Reservation r = reservedReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        boolean result = statusMachine.transition(1L, "COLLECTED", "ADMIN");

        assertTrue(result);
        assertEquals("COLLECTED", r.getStatus());
        verify(reservationRepository).save(r);
    }

    @Test
    void transition_reservedToCancelled_userRole_succeeds() {
        Reservation r = reservedReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        boolean result = statusMachine.transition(1L, "CANCELLED", "USER");

        assertTrue(result);
        assertEquals("CANCELLED", r.getStatus());
    }

    @Test
    void transition_reservedToNoShow_systemRole_succeeds() {
        Reservation r = reservedReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        boolean result = statusMachine.transition(1L, "NO_SHOW", "SYSTEM");

        assertTrue(result);
        assertEquals("NO_SHOW", r.getStatus());
    }

    @Test
    void transition_reservedToExpired_systemRole_succeeds() {
        Reservation r = reservedReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertTrue(statusMachine.transition(1L, "EXPIRED", "SYSTEM"));
    }

    @Test
    void transition_reservationNotFound_throws() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> statusMachine.transition(99L, "COLLECTED", "ADMIN"));
    }

    @Test
    void transition_invalidStatusTransition_throws() {
        Reservation r = reservedReservation();
        r.setStatus("COLLECTED");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThrows(InvalidStatusTransitionException.class,
                () -> statusMachine.transition(1L, "RESERVED", "ADMIN"));
    }

    @Test
    void transition_wrongActorRole_throws() {
        Reservation r = reservedReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        // USER cannot collect — only ADMIN can
        assertThrows(InvalidStatusTransitionException.class,
                () -> statusMachine.transition(1L, "COLLECTED", "USER"));
    }

    @Test
    void transition_adminCannotCancel() {
        Reservation r = reservedReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThrows(InvalidStatusTransitionException.class,
                () -> statusMachine.transition(1L, "CANCELLED", "ADMIN"));
    }

    @Test
    void transition_systemCannotCollect() {
        Reservation r = reservedReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThrows(InvalidStatusTransitionException.class,
                () -> statusMachine.transition(1L, "COLLECTED", "SYSTEM"));
    }

    // --- helpers ---

    private Reservation reservedReservation() {
        Reservation r = new Reservation();
        r.setStatus("RESERVED");
        return r;
    }
}
