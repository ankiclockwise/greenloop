package com.greenloop.tests.listing;

import com.greenloop.listing.ListingRepository;
import com.greenloop.model.ListingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ListingRepositoryTest {

    @Test
    void interfaceExists() {
        // Sanity check: reference expected methods/params to ensure signatures didn't change.
        // This test does not execute repository logic; it only references the type.
        Class<ListingRepository> repoClass = ListingRepository.class;
        assertNotNull(repoClass);

        // ensure method names exist via reflection (basic smoke checks)
        try {
            repoClass.getMethod("findByStatus", ListingStatus.class);
            repoClass.getMethod("findExpiredListings", ListingStatus.class, LocalDateTime.class);
            repoClass.getMethod("countByStatus", ListingStatus.class);
        } catch (NoSuchMethodException e) {
            fail("Expected repository methods not found: " + e.getMessage());
        }
    }
}
