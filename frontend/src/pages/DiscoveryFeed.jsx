import { useState, useEffect, useCallback } from "react";
import axios from "axios";
import { FilterBar } from "../components/feed/FilterBar";
import { ListingCard } from "../components/feed/ListingCard";
import { useGeolocation } from "../hooks/useGeolocation";
import { useFeedWebSocket } from "../hooks/useFeedWebSocket";
import { useAuth } from "../auth/AuthProvider";
import { DEFAULT_MOCK_LISTINGS } from "../data/mockListings";

export function DiscoveryFeed() {
  const { user } = useAuth();
  const { lat, lng, error: geoError, loading: geoLoading, setManualLocation } = useGeolocation();
  const { connected: wsConnected, newListings } = useFeedWebSocket();

  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [locationInput, setLocationInput] = useState("");
  const [showLocationBar, setShowLocationBar] = useState(false);

  const [filters, setFilters] = useState({
    category: "",
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

  const autodonations = listings.filter((l) => l.price === 0);

  const showSampleFeed = !geoLoading && (!lat || !lng);

  return (
    <div className="feed-shell">
      {/* Header */}
      <header className="feed-header">
        <h1>GreenLoop Feed</h1>
        <div className="feed-header-right">
          {wsConnected && (
            <span className="feed-live-dot">Live updates on</span>
          )}
          <span className="feed-user">{user?.displayName || user?.email}</span>
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
              placeholder="Enter your city or ZIP code"
              value={locationInput}
              onChange={(e) => setLocationInput(e.target.value)}
            />
            <button type="submit">Search</button>
            <button
              type="button"
              className="dismiss-btn"
              onClick={() => setShowLocationBar(false)}
            >
              ✕
            </button>
          </form>
        </div>
      )}

      {/* Filter Bar */}
      <FilterBar filters={filters} onChange={setFilters} />

      {/* Content */}
      <main className="feed-content">
        {error && <p className="feed-error">{error}</p>}

        {geoLoading && (
          <div className="feed-loading">
            <div className="feed-spinner" />
            <span>Finding listings near you…</span>
          </div>
        )}

        {!geoLoading && (
          <>
            {showSampleFeed && (
              <div className="feed-fallback">
                <div className="feed-fallback-copy">
                  <h3>Location is needed to show nearby listings.</h3>
                  <p>Allow location access or enter a city/ZIP to see available listings around you. Sample listings are shown below for demo purposes.</p>
                </div>
                {!showLocationBar && (
                  <button className="auth-button" type="button" onClick={() => setShowLocationBar(true)}>
                    Enter location manually
                  </button>
                )}
              </div>
            )}

            {lat && lng ? (
              loading ? (
                <div className="listing-grid">
                  {[...Array(6)].map((_, i) => (
                    <div key={i} className="listing-skeleton" />
                  ))}
                </div>
              ) : listings.length > 0 ? (
                <div className="listing-grid">
                  {listings.map((listing) => (
                    <ListingCard key={listing.id} listing={listing} />
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
                {DEFAULT_MOCK_LISTINGS.map((listing) => (
                  <ListingCard key={listing.id} listing={listing} />
                ))}
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
