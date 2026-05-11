import { useCallback, useEffect, useMemo, useState } from "react";
import axios from "axios";
import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";
import {
  DASHBOARD_SAMPLE_LISTINGS,
  getDashboardMetrics,
  getLocalCreatedListings,
  normalizeDashboardListing
} from "../utils/listingDashboard";

const useMockData = import.meta.env.VITE_USE_MOCK_DATA === "true";
const dashboardOwnerId = import.meta.env.VITE_DEMO_OWNER_ID || "1";

const STATUS_LABELS = {
  available: "Available",
  reserved: "Reserved",
  collected: "Collected",
  expired: "Expired"
};

function formatQuantity(value, unit = "items") {
  return `${value} ${unit || "items"}`;
}

function formatPickupWindow(listing) {
  const end = listing.pickupWindowEnd || listing.expiresAt;

  if (!end) {
    return "Pickup window pending";
  }

  return new Date(end).toLocaleString([], {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
  });
}

function formatDateTime(value) {
  if (!value) {
    return "Not recorded";
  }

  return new Date(value).toLocaleString([], {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
  });
}

export function DashboardPage() {
  const { user, logout } = useAuth();
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dataNote, setDataNote] = useState("");
  const [selectedListingId, setSelectedListingId] = useState(null);

  const loadDashboardListings = useCallback(async () => {
    const localListings = getLocalCreatedListings(user?.email);

    if (useMockData) {
      setListings([...localListings, ...DASHBOARD_SAMPLE_LISTINGS]);
      setDataNote("Showing sample dashboard data while mock mode is enabled.");
      return;
    }

    try {
      setLoading(true);
      setDataNote("");
      const response = await axios.get(`/api/analytics/dashboard/listings/${dashboardOwnerId}`);
      const apiListings = (response.data || []).map(normalizeDashboardListing);
      setListings([...localListings, ...apiListings]);
    } catch (error) {
      console.error("Failed to load dashboard listings:", error);
      setListings(localListings.length > 0 ? localListings : DASHBOARD_SAMPLE_LISTINGS);
      setDataNote("Showing saved local and sample listings because the backend is not available.");
    } finally {
      setLoading(false);
    }
  }, [user?.email]);

  useEffect(() => {
    loadDashboardListings();
  }, [loadDashboardListings]);

  const metrics = useMemo(() => getDashboardMetrics(listings), [listings]);
  const selectedListing = metrics.listings.find(
    (listing) => listing.id === selectedListingId
  );

  return (
    <div className="dashboard-page-shell">
      <header className="feed-header">
        <div>
          <h1>My Dashboard</h1>
          <p className="feed-subtitle">
            Listings created by {user?.displayName || user?.email}
          </p>
        </div>
        <div className="feed-header-right">
          <nav className="top-tabs" aria-label="Main navigation">
            <NavLink to="/" end className={({ isActive }) => `top-tab${isActive ? " active" : ""}`}>
              Feed
            </NavLink>
            <NavLink to="/dashboard" className={({ isActive }) => `top-tab${isActive ? " active" : ""}`}>
              My Dashboard
            </NavLink>
            <NavLink to="/impact" className={({ isActive }) => `top-tab${isActive ? " active" : ""}`}>
              Impact
            </NavLink>
          </nav>
          <Link className="feed-donate-button" to="/">
            Donate Food
          </Link>
          <button className="feed-logout-button" type="button" onClick={() => logout()}>
            Logout
          </button>
        </div>
      </header>

      <main className="dashboard-content">
        {dataNote ? <p className="impact-note">{dataNote}</p> : null}

        <section className="dashboard-listing-panel">
          <div className="section-header">
            <div>
              <span className="eyebrow">Created listings</span>
            </div>
          </div>

          {loading ? (
            <div className="feed-loading">
              <div className="feed-spinner" />
              <span>Loading your listings...</span>
            </div>
          ) : metrics.listings.length > 0 ? (
            <div className="dashboard-listing-list">
              {metrics.listings.map((listing) => (
                <button
                  className="dashboard-listing-row"
                  key={listing.id}
                  type="button"
                  onClick={() => setSelectedListingId(listing.id)}
                >
                  <div className="dashboard-listing-main">
                    <span className="listing-category">{listing.category}</span>
                    <h3>{listing.title}</h3>
                    <p>{listing.pickupLocation}</p>
                  </div>
                  <div className="dashboard-listing-stats">
                    <span className={`reservation-status reservation-status-${listing.status}`}>
                      {STATUS_LABELS[listing.status]}
                    </span>
                    <span>{listing.reservationCount} reserver{listing.reservationCount === 1 ? "" : "s"}</span>
                    <span>
                      {formatQuantity(listing.reservedQuantity, listing.unit)} reserved
                    </span>
                    <span>
                      {formatQuantity(listing.availableQuantity, listing.unit)} available
                    </span>
                    <span>Pickup by {formatPickupWindow(listing)}</span>
                    <strong>{listing.co2Kg} kg CO2</strong>
                  </div>
                </button>
              ))}
            </div>
          ) : (
            <div className="feed-empty">
              <h3>No created listings yet</h3>
              <p>Post your first food listing and it will appear here with status and impact totals.</p>
              <Link className="auth-button dashboard-link" to="/">
                Create a listing
              </Link>
            </div>
          )}
        </section>
      </main>

      {selectedListing ? (
        <div className="confirmation-overlay" role="dialog" aria-modal="true">
          <section className="dashboard-detail-card">
            <div className="section-header">
              <div>
                <span className="eyebrow">Listing details</span>
                <h2>{selectedListing.title}</h2>
              </div>
              <button
                type="button"
                className="dismiss-button"
                onClick={() => setSelectedListingId(null)}
              >
                Close
              </button>
            </div>

            <div className="dashboard-detail-grid">
              <article>
                <span>Status</span>
                <strong>{STATUS_LABELS[selectedListing.status]}</strong>
              </article>
              <article>
                <span>CO2 impact</span>
                <strong>{selectedListing.co2Kg} kg</strong>
              </article>
              <article>
                <span>Quantity</span>
                <strong>{formatQuantity(selectedListing.quantity, selectedListing.unit)}</strong>
              </article>
              <article>
                <span>Reserved</span>
                <strong>{formatQuantity(selectedListing.reservedQuantity, selectedListing.unit)}</strong>
              </article>
              <article>
                <span>Available</span>
                <strong>{formatQuantity(selectedListing.availableQuantity, selectedListing.unit)}</strong>
              </article>
              <article>
                <span>Reservers</span>
                <strong>{selectedListing.reservationCount}</strong>
              </article>
            </div>

            <div className="dashboard-detail-section">
              <h3>Pickup</h3>
              <p>{selectedListing.pickupLocation}</p>
              <p>Window ends {formatPickupWindow(selectedListing)}</p>
            </div>

            <div className="dashboard-detail-section">
              <h3>Reserved by</h3>
              {selectedListing.reservations.length > 0 ? (
                <div className="dashboard-reservation-list">
                  {selectedListing.reservations.map((reservation) => (
                    <article className="dashboard-reservation-card" key={reservation.id}>
                      <div>
                        <strong>{reservation.reservedBy?.name || "Unknown reserver"}</strong>
                        <span className={`reservation-status reservation-status-${reservation.status}`}>
                          {reservation.status}
                        </span>
                      </div>
                      <p>{formatQuantity(reservation.quantityReserved, selectedListing.unit)} reserved</p>
                      <p>{reservation.reservedBy?.email || "No email on reservation"}</p>
                      {reservation.reservedBy?.phone ? <p>{reservation.reservedBy.phone}</p> : null}
                      <p>Reserved {formatDateTime(reservation.reservedAt)}</p>
                      {reservation.collectedAt ? <p>Collected {formatDateTime(reservation.collectedAt)}</p> : null}
                      {reservation.pickupCode ? <p>Pickup code {reservation.pickupCode}</p> : null}
                    </article>
                  ))}
                </div>
              ) : (
                <p>No one has reserved this listing yet.</p>
              )}
            </div>
          </section>
        </div>
      ) : null}
    </div>
  );
}
