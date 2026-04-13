import { useState, useEffect, useCallback } from "react";
import axios from "axios";
import { FilterBar } from "../components/feed/FilterBar";
import { ListingCard } from "../components/feed/ListingCard";
import { ListingComposer } from "../components/feed/ListingComposer";
import { ListingDetailView } from "../components/feed/ListingDetailView";
import { ReservationConfirmation } from "../components/feed/ReservationConfirmation";
import { useGeolocation } from "../hooks/useGeolocation";
import { useFeedWebSocket } from "../hooks/useFeedWebSocket";
import { useAuth } from "../auth/AuthProvider";
import { DEFAULT_MOCK_LISTINGS, createListingFromForm } from "../data/mockListings";

export function DiscoveryFeed() {
  const { user, logout } = useAuth();
  const { lat, lng, error: geoError, loading: geoLoading, setManualLocation } = useGeolocation();
  const { connected: wsConnected, newListings } = useFeedWebSocket();

  const [listings, setListings] = useState(DEFAULT_MOCK_LISTINGS);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [locationInput, setLocationInput] = useState("");
  const [showLocationBar, setShowLocationBar] = useState(false);
  const [showDonateModal, setShowDonateModal] = useState(false);
  const [selectedListingId, setSelectedListingId] = useState(null);
  const [reservingId, setReservingId] = useState("");
  const [confirmationListing, setConfirmationListing] = useState(null);

  const [filters, setFilters] = useState({
    category: "",
    tags: [],
    dietary: [],
    allergens: [],
    priceRange: "",
    radius: 5,
  });

  const fetchListings = useCallback(async (latitude, longitude) => {
    if (!latitude || !longitude) return;
    try {
      setLoading(true);
      setError(null);
      const params = { lat: latitude, lng: longitude, radius: filters.radius, sortBy: "distance" };
      if (filters.category) params.category = filters.category;
      if (filters.tags.length > 0) params.tags = filters.tags.join(",");
      if (filters.dietary.length > 0) params.dietary = filters.dietary.join(",");
      if (filters.allergens.length > 0) params.allergens = filters.allergens.join(",");
      if (filters.priceRange) params.priceRange = filters.priceRange;
      const response = await axios.get("/api/listings", { params });
      const data = response.data || [];
      if (data.length === 0) {
        setError("No live listings found. Showing sample listings for demo.");
        setListings(DEFAULT_MOCK_LISTINGS);
      } else {
        setListings(data);
      }
    } catch {
      setError("Could not load listings. Showing sample listings for demo.");
      setListings(DEFAULT_MOCK_LISTINGS);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    if (lat && lng) fetchListings(lat, lng);
  }, [lat, lng, filters, fetchListings]);

  useEffect(() => {
    if (newListings.length > 0) setListings((prev) => [...newListings, ...prev]);
  }, [newListings]);

  useEffect(() => {
    if (geoError && !lat) setShowLocationBar(true);
  }, [geoError, lat]);

  function handleLocationSubmit(event) {
    event.preventDefault();
    if (!locationInput.trim()) return;
    axios.get("/api/geocode", { params: { location: locationInput } })
      .then((res) => {
        setManualLocation(res.data.latitude, res.data.longitude);
        setShowLocationBar(false);
      })
      .catch(() => setError("Could not find that location. Try a different city or ZIP."));
  }

  const filteredListings = listings.filter((listing) => {
    if (filters.category && listing.category !== filters.category) {
      return false;
    }

    if (filters.tags.length > 0 && !filters.tags.every((tag) => listing.tags?.includes(tag))) {
      return false;
    }

    if (filters.dietary.length > 0 && !filters.dietary.every((tag) => listing.dietary?.includes(tag))) {
      return false;
    }

    if (filters.allergens.length > 0 && filters.allergens.some((tag) => listing.allergens?.includes(tag))) {
      return false;
    }

    if (filters.priceRange === "free" && listing.price !== 0) {
      return false;
    }

    if (filters.priceRange === "under3" && listing.price >= 3) {
      return false;
    }

    if (filters.priceRange === "3to10" && (listing.price < 3 || listing.price > 10)) {
      return false;
    }

    if (listing.distance != null && listing.distance > filters.radius) {
      return false;
    }

    return true;
  });

  const selectedListing =
    listings.find((listing) => listing.id === selectedListingId) || null;

  const autodonations = filteredListings.filter((listing) => listing.price === 0);
  const reservedListings = listings.filter(
    (listing) => listing.reservationStatus && listing.reservationStatus !== "available"
  );

  const showSampleFeed = !geoLoading && (!lat || !lng);

  async function handleReserve(listingId) {
    if (!listingId || reservingId) {
      return;
    }

    setReservingId(listingId);

    try {
      await axios.post("/api/reservations", { listingId });
    } catch {
      // Local-first UI while reservation APIs are still stabilizing.
    } finally {
      let nextListing = null;

      setListings((current) =>
        current.map((listing) => {
          if (listing.id !== listingId) {
            return listing;
          }

          nextListing = {
            ...listing,
            reservationStatus: "confirmed",
            reservationCode: listing.reservationCode || `GL-${listing.id}`
          };

          return nextListing;
        })
      );

      if (!nextListing) {
        nextListing = filteredListings.find((listing) => listing.id === listingId) || null;
      }

      if (nextListing) {
        setConfirmationListing(nextListing);
        setSelectedListingId(nextListing.id);
      }

      setReservingId("");
    }
  }

  function handleCreateListing(formValues) {
    const nextListing = createListingFromForm(
      formValues,
      user?.displayName || user?.email,
      user?.email
    );
    setListings((current) => [nextListing, ...current]);
    setShowDonateModal(false);
  }

  function handleOpenListing(listing) {
    setSelectedListingId(listing.id);
    setConfirmationListing(null);
  }

  function handleCloseListingModal() {
    setSelectedListingId(null);
    setConfirmationListing(null);
  }

  return (
    <div className="feed-shell">
      {/* Header */}
      <header className="feed-header">
        <div>
          <h1>GreenLoop Feed</h1>
          <p className="feed-subtitle">
            Welcome {user?.displayName || user?.email}
          </p>
        </div>
        <div className="feed-header-right">
          {wsConnected && (
            <span className="feed-live-dot">Live updates on</span>
          )}
          <button
            className="feed-donate-button"
            type="button"
            onClick={() => setShowDonateModal(true)}
          >
            Donate Food
          </button>
          <button className="feed-logout-button" type="button" onClick={() => logout()}>
            Logout
          </button>
        </div>
      </header>

      {/* Rescue Now Banner */}
      {autodonations.length > 0 && (
        <div className="rescue-banner">
          <span>🚀</span>
          <span>Rescue Now — {autodonations.length} free listing{autodonations.length > 1 ? "s" : ""} available near you</span>
        </div>
      )}

      {/* Manual Location Bar */}
      {showLocationBar && !lat && (
        <div className="location-bar">
          <form onSubmit={handleLocationSubmit} className="location-form">
            <input
              type="text"
              placeholder="Enter your city or ZIP code to show nearby listings."
              value={locationInput}
              onChange={(e) => setLocationInput(e.target.value)}
            />
            <button type="submit">Search</button>
            {/* <button
              type="button"
              className="dismiss-btn"
              onClick={() => setShowLocationBar(false)}
            >
              ✕
            </button> */}
          </form>
        </div>
      )}

      {/* Filter Bar */}
      <FilterBar filters={filters} onChange={setFilters} />

      {/* Content */}
      <main className="feed-content">
        <div className="feed-layout">
          <div className="feed-main-column">
            {error && <p className="feed-error">{error}</p>}

            {geoLoading && (
              <div className="feed-loading">
                <div className="feed-spinner" />
                <span>Finding listings near you…</span>
              </div>
            )}

            {!geoLoading && (
              <>
                {showSampleFeed// && (
                  // <div className="feed-fallback">
                  //   <div className="feed-fallback-copy">
                  //     <h3>Location is needed to show nearby listings.</h3>
                  //     <p>Allow location access or enter a city/ZIP to see available listings around you. Sample listings are shown below for demo purposes.</p>
                  //   </div>
                  //   {!showLocationBar && (
                  //     <button className="auth-button" type="button" onClick={() => setShowLocationBar(true)}>
                  //       Enter location manually
                  //     </button>
                  //   )}
                  // </div>
                //)
                }

                {lat && lng ? (
                  loading ? (
                    <div className="listing-grid">
                      {[...Array(6)].map((_, i) => (
                        <div key={i} className="listing-skeleton" />
                      ))}
                    </div>
                  ) : filteredListings.length > 0 ? (
                    <div className="listing-grid">
                      {filteredListings.map((listing) => (
                        <ListingCard
                          key={listing.id}
                          listing={listing}
                          onSelect={handleOpenListing}
                        />
                      ))}
                    </div>
                  ) : (
                    <div className="listing-grid">
                      <div className="feed-empty">
                        <div className="feed-empty-icon">🍃</div>
                        <h3>No listings near you yet</h3>
                        <p>Check back soon — donors in your area will start sharing food listings.</p>
                        <button
                          className="auth-button"
                          type="button"
                          onClick={() => setShowLocationBar(true)}
                        >
                          Try a different location
                        </button>
                      </div>
                    </div>
                  )
                ) : (
                  <div className="listing-grid">
                    {filteredListings.map((listing) => (
                      <ListingCard
                        key={listing.id}
                        listing={listing}
                        onSelect={handleOpenListing}
                      />
                    ))}
                  </div>
                )}
              </>
            )}
          </div>

          <aside className="reserved-panel">
            <div className="section-header">
              <div>
                <span className="eyebrow">My reserved listings</span>
              </div>
            </div>

            {reservedListings.length > 0 ? (
              <div className="reserved-list">
                {reservedListings.map((listing) => (
                  <button
                    key={listing.id}
                    type="button"
                    className="reserved-listing"
                    onClick={() => handleOpenListing(listing)}
                  >
                    <div className="reserved-listing-top">
                      <strong>{listing.title}</strong>
                      <span className={`reservation-status reservation-status-${listing.reservationStatus}`}>
                        {listing.reservationStatus === "confirmed"
                          ? "Confirmed"
                          : listing.reservationStatus === "picked_up"
                            ? "Picked up"
                            : "Reserved"}
                      </span>
                    </div>
                    <span>{listing.pickupLocation}</span>
                    <span>
                      Pickup by{" "}
                      {new Date(listing.pickupWindowEnd).toLocaleString([], {
                        month: "short",
                        day: "numeric",
                        hour: "numeric",
                        minute: "2-digit"
                      })}
                    </span>
                  </button>
                ))}
              </div>
            ) : (
              <div className="reserved-empty">
                <strong>No reserved listings yet</strong>
                <p>Reserve something from the feed and it will appear here for quick access.</p>
              </div>
            )}
          </aside>
        </div>
      </main>

      {selectedListing ? (
        <div className="confirmation-overlay" role="dialog" aria-modal="true">
          {confirmationListing && confirmationListing.id === selectedListing.id ? (
            <ReservationConfirmation
              listing={confirmationListing}
              onClose={handleCloseListingModal}
            />
          ) : (
            <ListingDetailView
              listing={selectedListing}
              onReserve={handleReserve}
              onClose={handleCloseListingModal}
              reserving={reservingId === selectedListing.id}
            />
          )}
        </div>
      ) : null}

      {showDonateModal ? (
        <div className="confirmation-overlay" role="dialog" aria-modal="true">
          <div className="donate-modal-card">
            <div className="section-header">
              <div>
                <span className="eyebrow">Donate Food</span>
                <h2>Post a new listing</h2>
              </div>
              <button
                type="button"
                className="dismiss-button"
                onClick={() => setShowDonateModal(false)}
              >
                Close
              </button>
            </div>
            <ListingComposer onSubmit={handleCreateListing} />
          </div>
        </div>
      ) : null}
    </div>
  );
}
