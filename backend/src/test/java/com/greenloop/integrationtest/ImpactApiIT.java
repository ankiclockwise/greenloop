package com.greenloop.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ImpactApiIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private String donorEmail = "donor@impact.test";
    private String studentEmail = "student@impact.test";
    private Long donorId;
    private Long studentId;

    @BeforeEach
    void setup() throws Exception {
        User donor = new User();
        donor.setEmail(donorEmail);
        donor.setName("Impact Donor");
        donor.setRole(UserRole.RETAILER);
        donorId = userRepository.save(donor).getId();

        User student = new User();
        student.setEmail(studentEmail);
        student.setName("Impact Student");
        student.setRole(UserRole.CONSUMER);
        studentId = userRepository.save(student).getId();
    }

    @Test
    void getMyImpact_newUser_returnsZeroTotals() throws Exception {
        mockMvc.perform(get("/api/impact/me")
                        .param("email", donorEmail)
                        .param("name", "Impact Donor")
                        .param("userType", "donor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.co2SavedKg").value(0.0))
                .andExpect(jsonPath("$.donationCount").value(0))
                .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test
    void getMyImpact_returnsRealUserId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/impact/me")
                        .param("email", donorEmail)
                        .param("name", "Impact Donor")
                        .param("userType", "donor"))
                .andExpect(status().isOk())
                .andReturn();

        Long returnedId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("userId").asLong();
        assert returnedId.equals(donorId) : "userId in response should match DB user id";
    }

    @Test
    void getMyImpact_afterListingCreated_incrementsDonationCount() throws Exception {
        Listing listing = buildListing();
        mockMvc.perform(post("/api/listings?ownerId=" + donorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/impact/me")
                        .param("email", donorEmail)
                        .param("name", "Impact Donor")
                        .param("userType", "donor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.donationCount").value(1));
    }

    @Test
    void getMyImpact_afterCollect_showsCo2Saved() throws Exception {
        // Create listing, reserve, collect — full flow
        Listing listing = buildListing();
        MvcResult listingResult = mockMvc.perform(post("/api/listings?ownerId=" + donorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk()).andReturn();

        Long listingId = objectMapper.readTree(listingResult.getResponse().getContentAsString()).get("id").asLong();

        CreateReservationRequest req = new CreateReservationRequest();
        req.setListingId(listingId);
        req.setUserId(studentId);

        MvcResult resResult = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk()).andReturn();

        Long reservationId = objectMapper.readTree(resResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/reservations/" + reservationId + "/collect"))
                .andExpect(status().isOk());

        // Student should now show CO2 saved
        mockMvc.perform(get("/api/impact/me")
                        .param("email", studentEmail)
                        .param("name", "Impact Student")
                        .param("userType", "retail_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.co2SavedKg").value(20.0)); // 10kg * 2.0 produce factor
    }

    @Test
    void getDonorLeaderboard_returnsOk() throws Exception {
        mockMvc.perform(get("/api/impact/leaderboard/donors")
                        .param("page", "1")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getStudentLeaderboard_returnsOk() throws Exception {
        mockMvc.perform(get("/api/impact/leaderboard/students")
                        .param("page", "1")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    private Listing buildListing() {
        Listing listing = new Listing();
        listing.setTitle("Surplus Produce");
        listing.setDescription("End of day produce");
        listing.setCategory(ListingCategory.PRODUCE);
        listing.setQuantity(10);
        listing.setUnit("kg");
        listing.setPickupWindowStart(LocalDateTime.now().plusHours(1));
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(6));
        listing.setExpiresAt(LocalDateTime.now().plusHours(6));
        return listing;
    }
}
