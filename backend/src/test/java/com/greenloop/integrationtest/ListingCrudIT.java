package com.greenloop.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenloop.listing.ListingUpdateRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ListingCrudIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private Long ownerId;
    private Long listingId;

    @BeforeEach
    void setup() throws Exception {
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setName("Test Owner");
        owner.setRole(UserRole.RETAILER);
        ownerId = userRepository.save(owner).getId();

        Listing listing = buildListing("Original Title");
        MvcResult result = mockMvc.perform(post("/api/listings?ownerId=" + ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk())
                .andReturn();

        listingId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void getListingById_returnsCorrectListing() throws Exception {
        mockMvc.perform(get("/api/listings/" + listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listingId))
                .andExpect(jsonPath("$.title").value("Original Title"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void getListingById_nonExistent_returns500() throws Exception {
        mockMvc.perform(get("/api/listings/99999"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void updateListing_changesTitle() throws Exception {
        ListingUpdateRequest req = new ListingUpdateRequest();
        req.setTitle("Updated Title");
        req.setQuantity(20);

        mockMvc.perform(patch("/api/listings/" + listingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.quantity").value(20));
    }

    @Test
    void updateListing_preservesUnchangedFields() throws Exception {
        ListingUpdateRequest req = new ListingUpdateRequest();
        req.setTitle("New Title Only");

        mockMvc.perform(patch("/api/listings/" + listingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title Only"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void deleteListing_removesFromAvailable() throws Exception {
        mockMvc.perform(delete("/api/listings/" + listingId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteListing_nonExistent_returns500() throws Exception {
        mockMvc.perform(delete("/api/listings/99999"))
                .andExpect(status().is5xxServerError());
    }

    private Listing buildListing(String title) {
        Listing listing = new Listing();
        listing.setTitle(title);
        listing.setDescription("Test description");
        listing.setCategory(ListingCategory.BAKERY);
        listing.setQuantity(10);
        listing.setUnit("piece");
        listing.setPickupWindowStart(LocalDateTime.now().plusHours(1));
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(6));
        listing.setExpiresAt(LocalDateTime.now().plusHours(6));
        return listing;
    }
}
