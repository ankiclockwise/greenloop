package com.greenloop.tests.listing;

import com.greenloop.listing.ListingUpdateRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ListingUpdateRequestTest {

    @Test
    void gettersAndSetters() {
        ListingUpdateRequest r = new ListingUpdateRequest();
        r.setTitle("hi");
        r.setQuantity(3);
        r.setPickupWindowStart(LocalDateTime.now());

        assertEquals("hi", r.getTitle());
        assertEquals(3, r.getQuantity());
        assertNotNull(r.getPickupWindowStart());
    }
}
