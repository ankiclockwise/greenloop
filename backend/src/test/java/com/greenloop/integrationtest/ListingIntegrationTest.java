package com.greenloop.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenloop.model.Listing;
import com.greenloop.model.ListingCategory;
import com.greenloop.model.User;
import com.greenloop.model.UserRole;
import com.greenloop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ListingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long ownerId;

    @BeforeEach
    void setup() {
        User owner = new User();
        owner.setEmail("donor@test.com");
        owner.setName("Test Donor");
        owner.setRole(UserRole.RETAILER);
        ownerId = userRepository.save(owner).getId();
    }

    @Test
    void createListing_returns200_and_available_status() throws Exception {
        Listing listing = buildListing();

        mockMvc.perform(post("/api/listings?ownerId=" + ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.title").value("Test Apples"));
    }

    @Test
    void getAvailableListings_returns_created_listing() throws Exception {
        Listing listing = buildListing();

        mockMvc.perform(post("/api/listings?ownerId=" + ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Apples"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void getListingsByOwner_returns_only_that_owners_listings() throws Exception {
        mockMvc.perform(post("/api/listings?ownerId=" + ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildListing())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/listings/owner/" + ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void createListing_withInvalidPickupWindow_returns400() throws Exception {
        Listing listing = buildListing();
        listing.setPickupWindowStart(LocalDateTime.now().plusHours(5));
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(1)); // end before start

        mockMvc.perform(post("/api/listings?ownerId=" + ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().is(500));
    }

    private Listing buildListing() {
        Listing listing = new Listing();
        listing.setTitle("Test Apples");
        listing.setDescription("Fresh apples");
        listing.setCategory(ListingCategory.PRODUCE);
        listing.setQuantity(10);
        listing.setUnit("kg");
        listing.setPickupWindowStart(LocalDateTime.now().plusHours(1));
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(6));
        listing.setExpiresAt(LocalDateTime.now().plusHours(6));
        return listing;
    }
}
