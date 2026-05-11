package com.greenloop.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenloop.impact.UserImpact;
import com.greenloop.impact.UserImpactRepository;
import com.greenloop.listing.ListingRepository;
import com.greenloop.model.Listing;
import com.greenloop.model.ListingCategory;
import com.greenloop.model.User;
import com.greenloop.model.UserRole;
import com.greenloop.repository.UserRepository;
import com.greenloop.reservation.CreateReservationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration test for the full food rescue flow:
 * Create listing → Reserve → Collect → Verify CO2 saved in UserImpact
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CollectReservationIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserImpactRepository userImpactRepository;
    @Autowired private ListingRepository listingRepository;
    @Autowired private ObjectMapper objectMapper;

    private Long donorId;
    private Long recipientId;
    private Long listingId;
    private Long reservationId;

    @BeforeEach
    void setup() throws Exception {
        User donor = new User();
        donor.setEmail("donor@greenloop.test");
        donor.setName("Test Donor");
        donor.setRole(UserRole.RETAILER);
        donorId = userRepository.save(donor).getId();

        User recipient = new User();
        recipient.setEmail("student@greenloop.test");
        recipient.setName("Test Student");
        recipient.setRole(UserRole.CONSUMER);
        recipientId = userRepository.save(recipient).getId();

        // Create a listing (10 kg of produce)
        Listing listing = new Listing();
        listing.setTitle("Fresh Vegetables");
        listing.setDescription("Surplus vegetables from dining hall");
        listing.setCategory(ListingCategory.PRODUCE);
        listing.setQuantity(10);
        listing.setUnit("kg");
        listing.setPickupWindowStart(LocalDateTime.now().plusHours(1));
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(6));
        listing.setExpiresAt(LocalDateTime.now().plusHours(6));

        MvcResult listingResult = mockMvc.perform(post("/api/listings?ownerId=" + donorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk())
                .andReturn();

        listingId = objectMapper.readTree(listingResult.getResponse().getContentAsString()).get("id").asLong();

        // Reserve the listing
        CreateReservationRequest request = new CreateReservationRequest();
        request.setListingId(listingId);
        request.setUserId(recipientId);

        MvcResult reservationResult = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        reservationId = objectMapper.readTree(reservationResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void collect_setsReservationStatusToCollected() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + reservationId + "/collect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COLLECTED"));
    }

    @Test
    void collect_setsListingStatusToCollected() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + reservationId + "/collect"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/listings/" + listingId))
                .andExpect(jsonPath("$.status").value("COLLECTED"));
    }

    @Test
    void collect_populatesCo2SavedKgOnListing() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + reservationId + "/collect"))
                .andExpect(status().isOk());

        // 10 kg * 2.0 PRODUCE factor = 20.0 kg CO2
        Listing listing = listingRepository.findById(listingId).orElseThrow();
        assertNotNull(listing.getCo2SavedKg());
        assertTrue(listing.getCo2SavedKg().doubleValue() > 0,
                "CO2 saved should be positive after collect");
        assertEquals(20.0, listing.getCo2SavedKg().doubleValue(), 0.1);
    }

    @Test
    void collect_recordsCo2InUserImpact() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + reservationId + "/collect"))
                .andExpect(status().isOk());

        Optional<UserImpact> recipientImpact = userImpactRepository.findByUserId(recipientId);
        assertTrue(recipientImpact.isPresent(), "UserImpact should be created for recipient");
        assertTrue(recipientImpact.get().getCo2SavedKg().doubleValue() > 0,
                "Recipient's CO2 saved should be positive");
        assertEquals(20.0, recipientImpact.get().getCo2SavedKg().doubleValue(), 0.1);
    }

    @Test
    void collect_incrementsDonorDonationCount() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + reservationId + "/collect"))
                .andExpect(status().isOk());

        Optional<UserImpact> donorImpact = userImpactRepository.findByUserId(donorId);
        assertTrue(donorImpact.isPresent());
        assertTrue(donorImpact.get().getCompletedPickups() > 0,
                "Donor's completed pickups should increment after collect");
    }

    @Test
    void collect_cannotCollectTwice() throws Exception {
        mockMvc.perform(patch("/api/reservations/" + reservationId + "/collect"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/reservations/" + reservationId + "/collect"))
                .andExpect(status().is5xxServerError());
    }
}
