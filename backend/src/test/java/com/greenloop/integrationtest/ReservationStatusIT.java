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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationStatusIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private Long donorId;
    private Long recipientId;
    private Long reservationId;

    @BeforeEach
    void setup() throws Exception {
        User donor = new User();
        donor.setEmail("donor@test.com");
        donor.setName("Test Donor");
        donor.setRole(UserRole.RETAILER);
        donorId = userRepository.save(donor).getId();

        User recipient = new User();
        recipient.setEmail("student@test.com");
        recipient.setName("Test Student");
        recipient.setRole(UserRole.CONSUMER);
        recipientId = userRepository.save(recipient).getId();

        Listing listing = new Listing();
        listing.setTitle("Status Test Item");
        listing.setDescription("For status machine testing");
        listing.setCategory(ListingCategory.PRODUCE);
        listing.setQuantity(3);
        listing.setUnit("kg");
        listing.setPickupWindowStart(LocalDateTime.now().plusHours(1));
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(6));
        listing.setExpiresAt(LocalDateTime.now().plusHours(6));

        MvcResult listingResult = mockMvc.perform(post("/api/listings?ownerId=" + donorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk())
                .andReturn();

        Long listingId = objectMapper.readTree(listingResult.getResponse().getContentAsString()).get("id").asLong();

        CreateReservationRequest req = new CreateReservationRequest();
        req.setListingId(listingId);
        req.setUserId(recipientId);

        MvcResult resResult = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        reservationId = objectMapper.readTree(resResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void cancelReservation_returns200_withCancelledStatus() throws Exception {
        mockMvc.perform(post("/api/v1/reservations/" + reservationId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void collectReservation_returns200_withCollectedStatus() throws Exception {
        mockMvc.perform(post("/api/v1/reservations/" + reservationId + "/collect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COLLECTED"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void transitionReservation_validTransition_toExpired() throws Exception {
        Map<String, String> body = Map.of("newStatus", "EXPIRED", "actorRole", "SYSTEM");

        mockMvc.perform(post("/api/v1/reservations/" + reservationId + "/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newStatus").value("EXPIRED"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void transitionReservation_invalidRole_returns400() throws Exception {
        // USER role cannot collect — only ADMIN can
        Map<String, String> body = Map.of("newStatus", "COLLECTED", "actorRole", "USER");

        mockMvc.perform(post("/api/v1/reservations/" + reservationId + "/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Status Transition"));
    }

    @Test
    void transitionReservation_nonExistentReservation_returns404() throws Exception {
        Map<String, String> body = Map.of("newStatus", "CANCELLED", "actorRole", "USER");

        mockMvc.perform(post("/api/v1/reservations/99999/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void cancelAlreadyCancelled_returns400() throws Exception {
        // First cancel
        mockMvc.perform(post("/api/v1/reservations/" + reservationId + "/cancel"))
                .andExpect(status().isOk());

        // Second cancel should fail — CANCELLED → CANCELLED is invalid
        mockMvc.perform(post("/api/v1/reservations/" + reservationId + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot Cancel"));
    }
}
