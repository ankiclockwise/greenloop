package com.greenloop.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenloop.model.Listing;
import com.greenloop.model.ListingCategory;
import com.greenloop.model.User;
import com.greenloop.model.UserRole;
import com.greenloop.repository.UserRepository;
import com.greenloop.reservation.CreateReservationRequest;
import com.greenloop.reservation.ReservationRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ObjectMapper objectMapper;

    private Long donorId;
    private Long recipientId;
    private Long listingId;

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
        listing.setTitle("Test Bread");
        listing.setDescription("Fresh bread");
        listing.setCategory(ListingCategory.BAKERY);
        listing.setQuantity(5);
        listing.setUnit("piece");
        listing.setPickupWindowStart(LocalDateTime.now().plusHours(1));
        listing.setPickupWindowEnd(LocalDateTime.now().plusHours(6));
        listing.setExpiresAt(LocalDateTime.now().plusHours(6));

        MvcResult result = mockMvc.perform(post("/api/listings?ownerId=" + donorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk())
                .andReturn();

        listingId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void createReservation_changesListingStatusToReserved() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setListingId(listingId);
        request.setUserId(recipientId);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(get("/api/listings/" + listingId))
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    void createReservation_reservingAlreadyReservedListing_throws() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setListingId(listingId);
        request.setUserId(recipientId);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getUserReservations_returnsReservationWithListing() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setListingId(listingId);
        request.setUserId(recipientId);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/user/" + recipientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("RESERVED"))
                .andExpect(jsonPath("$[0].listing.title").value("Test Bread"));
    }
}
