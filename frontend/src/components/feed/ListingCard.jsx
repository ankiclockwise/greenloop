import { ReservationStatusIndicator } from "./ReservationStatusIndicator";

export function ListingCard({ listing, onSelect }) {
  const isFree = listing.price === 0;
  const displayPrice = isFree ? "Free" : `$${listing.price?.toFixed(2)}`;

  function formatPickupWindow(end) {
    if (!end) return null;
    const time = new Date(end).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
    return `Pickup by ${time}`;
  }

  return (
    <article className="listing-card" onClick={() => onSelect(listing)}>
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

        <div className="listing-card-topline">
          {listing.category ? <span className="listing-category">{listing.category}</span> : null}
          <ReservationStatusIndicator status={listing.reservationStatus} />
        </div>

        {listing.quantity != null ? (
          <p className="listing-summary">{listing.quantity} left from {listing.providerName}</p>
        ) : null}

        <div className="listing-meta">
          {listing.distance != null && (
            <span className="listing-meta-item">📍 {listing.distance.toFixed(1)} mi away</span>
          )}
          {listing.pickupWindowEnd && (
            <span className="listing-meta-item">
              🕐 {formatPickupWindow(listing.pickupWindowEnd)}
            </span>
          )}
        </div>

        {((listing.tags?.length > 0) || (listing.allergens?.length > 0)) && (
          <div className="listing-tags">
            {listing.tags?.map((tag) => (
              <span key={tag} className="listing-tag">{tag}</span>
            ))}
            {listing.allergens?.map((tag) => (
              <span key={tag} className="listing-tag allergen">Contains {tag}</span>
            ))}
          </div>
        )}

        <button
          type="button"
          className="reserve-button"
          onClick={(event) => {
            event.stopPropagation();
            onSelect(listing);
          }}
        >
          View details
        </button>
      </div>
    </article>
  );
}
