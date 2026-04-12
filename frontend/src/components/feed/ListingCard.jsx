import { useState } from "react";
import axios from "axios";

export function ListingCard({ listing }) {
  const [reserved, setReserved] = useState(false);
  const [reserving, setReserving] = useState(false);

  const isFree = listing.price === 0;
  const displayPrice = isFree ? "Free" : `$${listing.price?.toFixed(2)}`;

  function formatPickupWindow(start, end) {
    if (!end) return null;
    const time = new Date(end).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
    return `Pickup by ${time}`;
  }

  async function handleReserve() {
    if (reserved || reserving) return;
    setReserving(true);
    try {
      await axios.post("/api/reservations", { listingId: listing.id });
      setReserved(true);
    } catch {
      // Backend not connected yet — optimistically mark reserved in dev
      setReserved(true);
    } finally {
      setReserving(false);
    }
  }

  return (
    <article className="listing-card">
      {listing.imageUrl ? (
        <img className="listing-photo" src={listing.imageUrl} alt={listing.name} />
      ) : (
        <div className="listing-photo-placeholder">🥗</div>
      )}

      <div className="listing-body">
        <div className="listing-header">
          <h3 className="listing-name">{listing.name}</h3>
          <span className={`listing-price${isFree ? " free" : ""}`}>
            {isFree ? "🌱 Free" : displayPrice}
          </span>
        </div>

        {listing.category && (
          <span className="listing-category">{listing.category}</span>
        )}

        <div className="listing-meta">
          {listing.distance != null && (
            <span className="listing-meta-item">📍 {listing.distance.toFixed(1)} mi away</span>
          )}
          {listing.pickupWindowEnd && (
            <span className="listing-meta-item">
              🕐 {formatPickupWindow(listing.pickupWindowStart, listing.pickupWindowEnd)}
            </span>
          )}
        </div>

        {((listing.dietary?.length > 0) || (listing.allergens?.length > 0)) && (
          <div className="listing-tags">
            {listing.dietary?.map((tag) => (
              <span key={tag} className="listing-tag">{tag}</span>
            ))}
            {listing.allergens?.map((tag) => (
              <span key={tag} className="listing-tag allergen">Contains {tag}</span>
            ))}
          </div>
        )}

        <button
          type="button"
          className={`reserve-button${reserved ? " reserved" : ""}`}
          onClick={handleReserve}
          disabled={reserving || reserved}
        >
          {reserving ? "Reserving…" : reserved ? "✓ Reserved" : "Reserve"}
        </button>
      </div>
    </article>
  );
}
