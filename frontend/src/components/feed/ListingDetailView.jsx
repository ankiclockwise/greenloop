import { QRCodeDisplay } from "./QRCodeDisplay";
import { ReservationStatusIndicator } from "./ReservationStatusIndicator";
import { useAuth } from "../../auth/AuthProvider";

function formatCurrency(price) {
  return price === 0 ? "Free" : `$${Number(price || 0).toFixed(2)}`;
}

function formatWindow(start, end) {
  return `${new Date(start).toLocaleString([], {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
  })} - ${new Date(end).toLocaleTimeString([], {
    hour: "numeric",
    minute: "2-digit"
  })}`;
}

export function ListingDetailView({ listing, onReserve, onClose, reserving }) {
  const { user } = useAuth();

  if (!listing) {
    return null;
  }

  const isReserved = listing.reservationStatus !== "available";
  const isOwnListing =
    !!user?.email &&
    (listing.ownerEmail === user.email || listing.owner?.email === user.email);

  return (
    <div className="detail-panel">
      <div className="section-header">
        <div>
          <span className="eyebrow">Listing details</span>
          <h2>{listing.title}</h2>
        </div>
        <div className="modal-header-actions">
          <ReservationStatusIndicator status={listing.reservationStatus} />
          <button type="button" className="dismiss-button" onClick={onClose}>
            Close
          </button>
        </div>
      </div>

      <p className="detail-description">{listing.description}</p>

      <div className="detail-meta-grid">
        <article>
          <strong>Category</strong>
          <span>{listing.category}</span>
        </article>
        <article>
          <strong>Quantity</strong>
          <span>{listing.quantity} available</span>
        </article>
        <article>
          <strong>Price</strong>
          <span>{formatCurrency(listing.price)}</span>
        </article>
        <article>
          <strong>Pickup window</strong>
          <span>{formatWindow(listing.pickupWindowStart, listing.pickupWindowEnd)}</span>
        </article>
      </div>

      <div className="detail-section">
        <strong>Pickup location</strong>
        <p>{listing.pickupLocation}</p>
        {listing.pickupNotes ? <p className="detail-muted">{listing.pickupNotes}</p> : null}
      </div>

      {listing.tags?.length > 0 ? (
        <div className="detail-section">
          <strong>Tags</strong>
          <div className="listing-tags">
            {listing.tags.map((tag) => (
              <span key={tag} className="listing-tag">
                {tag}
              </span>
            ))}
          </div>
        </div>
      ) : null}

      {isReserved ? (
        <div className="detail-section">
          <strong>Pickup code</strong>
          <QRCodeDisplay value={listing.reservationCode || `GL-${listing.id}`} />
        </div>
      ) : null}

      {isOwnListing ? (
        <p className="detail-owner-note">
          This is your own listing, so it cannot be reserved from your account.
        </p>
      ) : null}

      <button
        type="button"
        className={`reserve-button${isReserved || isOwnListing ? " reserved" : ""}`}
        disabled={isReserved || isOwnListing}
        onClick={() => onReserve(listing.id)}
      >
        {reserving
          ? "Reserving..."
          : isOwnListing
            ? "Your listing"
            : isReserved
              ? "Already reserved"
              : "Reserve"}
      </button>
    </div>
  );
}
