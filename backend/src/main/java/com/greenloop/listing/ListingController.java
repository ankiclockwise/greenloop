package com.greenloop.listing;

import com.greenloop.model.Listing;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    public Listing createListing(@RequestBody Listing listing,
            @RequestParam Long ownerId) {
        return listingService.createListing(listing, ownerId);
    }

    @GetMapping
    public List<Listing> getAvailableListings() {
        return listingService.getAvailableListings();
    }

    @GetMapping("/{id}")
    public Listing getListingById(@PathVariable Long id) {
        return listingService.getListingById(id);
    }

    @GetMapping("/owner/{ownerId}")
    public List<Listing> getListingsByOwner(@PathVariable Long ownerId) {
        return listingService.getListingsByOwner(ownerId);
    }

    @PatchMapping("/{id}")
    public Listing updateListing(@PathVariable Long id,
            @RequestBody ListingUpdateRequest request) {
        return listingService.updateListing(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteListing(@PathVariable Long id) {
        listingService.deleteListing(id);
    }
}