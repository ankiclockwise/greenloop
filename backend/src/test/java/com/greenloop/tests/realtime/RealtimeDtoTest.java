package com.greenloop.tests.realtime;

import com.greenloop.realtime.dto.ListingBroadcastDto;
import com.greenloop.realtime.dto.ReservationNotificationDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RealtimeDtoTest {

    // --- ListingBroadcastDto ---

    @Test
    void listingBroadcastDto_defaultConstructor_noException() {
        ListingBroadcastDto dto = new ListingBroadcastDto();
        assertNotNull(dto);
    }

    @Test
    void listingBroadcastDto_settersAndGetters() {
        ListingBroadcastDto dto = new ListingBroadcastDto();
        dto.setListingId(1L);
        dto.setTitle("Bread");
        dto.setDescription("Fresh bread");
        dto.setCategory("BAKERY");
        dto.setStatus("AVAILABLE");
        dto.setEstimatedValue(new BigDecimal("3.50"));
        dto.setImageUrl("http://img.test");
        dto.setDonorId(10L);
        dto.setDonorName("Alice");

        assertEquals(1L, dto.getListingId());
        assertEquals("Bread", dto.getTitle());
        assertEquals("Fresh bread", dto.getDescription());
        assertEquals("BAKERY", dto.getCategory());
        assertEquals("AVAILABLE", dto.getStatus());
        assertEquals(new BigDecimal("3.50"), dto.getEstimatedValue());
        assertEquals("http://img.test", dto.getImageUrl());
        assertEquals(10L, dto.getDonorId());
        assertEquals("Alice", dto.getDonorName());
    }

    @Test
    void listingBroadcastDto_fullConstructor() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusHours(1);
        LocalDateTime end = now.plusHours(2);

        ListingBroadcastDto dto = new ListingBroadcastDto(
                5L, "Milk", "Fresh milk", "DAIRY", "AVAILABLE",
                new BigDecimal("2.00"), "http://img", 3L, "Bob",
                "123 Main St", now, start, end, "NEW_LISTING"
        );

        assertEquals(5L, dto.getListingId());
        assertEquals("Milk", dto.getTitle());
        assertEquals("DAIRY", dto.getCategory());
        assertEquals(3L, dto.getDonorId());
        assertEquals("NEW_LISTING", dto.getEventType());
    }

    // --- ReservationNotificationDto ---

    @Test
    void reservationNotificationDto_defaultConstructor_noException() {
        ReservationNotificationDto dto = new ReservationNotificationDto();
        assertNotNull(dto);
    }

    @Test
    void reservationNotificationDto_settersAndGetters() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        ReservationNotificationDto dto = new ReservationNotificationDto();
        dto.setReservationId(1L);
        dto.setUserId(2L);
        dto.setListingId(3L);
        dto.setListingTitle("Fruit Box");
        dto.setStatus("RESERVED");
        dto.setNotificationType("CONFIRMATION");
        dto.setMessage("Your reservation is confirmed");
        dto.setPickupWindowStart(start);
        dto.setPickupWindowEnd(end);

        assertEquals(1L, dto.getReservationId());
        assertEquals(2L, dto.getUserId());
        assertEquals(3L, dto.getListingId());
        assertEquals("Fruit Box", dto.getListingTitle());
        assertEquals("RESERVED", dto.getStatus());
        assertEquals("CONFIRMATION", dto.getNotificationType());
        assertEquals("Your reservation is confirmed", dto.getMessage());
        assertEquals(start, dto.getPickupWindowStart());
        assertEquals(end, dto.getPickupWindowEnd());
    }

    @Test
    void reservationNotificationDto_fullConstructor() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        ReservationNotificationDto dto = new ReservationNotificationDto(
                10L, 20L, 30L, "Soup", "RESERVED", "BOOKING", "Booked!", start, end, "123 Main St"
        );

        assertEquals(10L, dto.getReservationId());
        assertEquals(20L, dto.getUserId());
        assertEquals(30L, dto.getListingId());
        assertEquals("Soup", dto.getListingTitle());
        assertEquals("BOOKING", dto.getNotificationType());
    }
}
