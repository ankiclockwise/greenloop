package com.greenloop.tests.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenloop.listing.ListingController;
import com.greenloop.listing.ListingService;
import com.greenloop.model.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ListingControllerTest {

    private MockMvc mockMvc;

    private ListingService listingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Listing sample;

    @BeforeEach
    void setup() {
        sample = new Listing();
        sample.setTitle("t");
        sample.setDescription("d");

        // Create a small test double by subclassing ListingService and overriding methods used by controller
        listingService = new ListingService(null, null, null) {
            @Override
            public java.util.List<Listing> getAvailableListings() {
                return Collections.emptyList();
            }

            @Override
            public Listing createListing(Listing listing, Long ownerId) {
                return sample;
            }
        };

        ListingController controller = new ListingController(listingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAvailableListings_ok() throws Exception {
        mockMvc.perform(get("/api/listings").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createListing_ok() throws Exception {
        mockMvc.perform(post("/api/listings?ownerId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("t"));
    }
}
