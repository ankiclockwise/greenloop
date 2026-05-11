package com.greenloop.tests.impact;

import com.greenloop.impact.dto.LeaderboardItemDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardItemDtoTest {

    @Test
    void forDonor_setsAllFields() {
        LeaderboardItemDto dto = LeaderboardItemDto.forDonor(1, "42", "store", "Alice", 100, 10, 8, 5.5);

        assertEquals(1, dto.getRank());
        assertEquals("42", dto.getUserId());
        assertEquals("store", dto.getUserType());
        assertEquals("Alice", dto.getName());
        assertEquals(100, dto.getFoodDonated());
        assertEquals(10, dto.getDonationCount());
        assertEquals(8, dto.getCompletedPickups());
        assertEquals(5.5, dto.getCo2SavedKg());
        assertNull(dto.getFoodReceived());
        assertNull(dto.getPickupCount());
    }

    @Test
    void forStudent_setsAllFields() {
        LeaderboardItemDto dto = LeaderboardItemDto.forStudent(2, "7", "retail_user", "Bob", 50, 5, 20, 4, 3.2);

        assertEquals(2, dto.getRank());
        assertEquals("7", dto.getUserId());
        assertEquals("retail_user", dto.getUserType());
        assertEquals("Bob", dto.getName());
        assertEquals(50, dto.getFoodReceived());
        assertEquals(5, dto.getPickupCount());
        assertEquals(20, dto.getFoodDonated());
        assertEquals(4, dto.getDonationCount());
        assertEquals(3.2, dto.getCo2SavedKg());
        assertNull(dto.getCompletedPickups());
    }

    @Test
    void forDonor_zeroValues() {
        LeaderboardItemDto dto = LeaderboardItemDto.forDonor(1, "1", "store", "Zero", 0, 0, 0, 0.0);

        assertEquals(0, dto.getFoodDonated());
        assertEquals(0.0, dto.getCo2SavedKg());
    }

    @Test
    void forStudent_userTypeIsPreserved() {
        LeaderboardItemDto dto = LeaderboardItemDto.forStudent(1, "1", "diner", "Hall", 10, 3, 0, 0, 1.0);

        assertEquals("diner", dto.getUserType());
    }
}
