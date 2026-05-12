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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AnalyticsIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private Long ownerId;
    private String ownerEmail;

    @BeforeEach
    void setup() throws Exception {
        ownerEmail = "retailer@test.com";
        User owner = new User();
        owner.setEmail(ownerEmail);
        owner.setName("Test Retailer");
        owner.setRole(UserRole.RETAILER);
        ownerId = userRepository.save(owner).getId();
    }

    @Test
    void getBusinessAnalytics_unknownEmail_returnsZeros() throws Exception {
        mockMvc.perform(get("/api/analytics/business?ownerEmail=unknown@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalListings").value(0))
                .andExpect(jsonPath("$.rescuedListings").value(0))
                .andExpect(jsonPath("$.rescueRate").value(0.0));
    }

    @Test
    void getBusinessAnalytics_withListings_returnsTotals() throws Exception {
        Listing listing = buildListing("Apples");
        mockMvc.perform(post("/api/listings?ownerId=" + ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/business?ownerEmail=" + ownerEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalListings").value(1))
                .andExpect(jsonPath("$.userType").value("store"));
    }

    @Test
    void getDashboardListings_noListings_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard/listings/" + ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getDashboardListings_withListing_returnsEntries() throws Exception {
        Listing listing = buildListing("Bread Loaf");
        mockMvc.perform(post("/api/listings?ownerId=" + ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/dashboard/listings/" + ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Bread Loaf"));
    }

    @Test
    void getBusinessAnalytics_multipleListings_computesRescueRate() throws Exception {
        mockMvc.perform(post("/api/listings?ownerId=" + ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildListing("Item A"))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/listings?ownerId=" + ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildListing("Item B"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/business?ownerEmail=" + ownerEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalListings").value(2))
                .andExpect(jsonPath("$.rescuedListings").value(0))
                .andExpect(jsonPath("$.rescueRate").value(0.0));
    }

    private Listing buildListing(String title) {
        Listing listing = new Listing();
        listing.setTitle(title);
        listing.setDescription("Test item");
        listing.setCategory(ListingCategory.PRODUCE);
        listing.setQuantity(5);
        listing.setUnit("kg");
        listing.setPickupWindowStart(LocalDateTime.now().plusHours(1));
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(6));
        listing.setExpiresAt(LocalDateTime.now().plusHours(6));
        return listing;
    }
}
