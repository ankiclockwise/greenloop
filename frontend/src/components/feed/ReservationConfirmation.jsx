import { useState } from "react";
import { QRCodeDisplay } from "./QRCodeDisplay";
import { ReservationStatusIndicator } from "./ReservationStatusIndicator";

export function ReservationConfirmation({ listing, onClose, onCollect }) {
  const [collecting, setCollecting] = useState(false);
  const [collected, setCollected] = useState(false);

  if (!listing) {
    return null;
  }

  async function handleMarkPickedUp() {
    setCollecting(true);
    try {
      await onCollect(listing.reservationId);
      setCollected(true);
    } finally {
      setCollecting(false);
    }
  }

  return (
    <div className="confirmation-card">
      <div className="section-header">
        <div>
          <span className="eyebrow">{collected ? "Picked up!" : "Reservation confirmed"}</span>
          <h2>{listing.title}</h2>
        </div>
        <button type="button" className="dismiss-button" onClick={onClose}>
          Close
        </button>
      </div>

      <div className="confirmation-summary">
        <ReservationStatusIndicator status={collected ? "picked_up" : listing.reservationStatus} />
        <p>
          Pickup at <strong>{listing.pickupLocation}</strong> before{" "}
          {new Date(listing.pickupWindowEnd).toLocaleString([], {
            month: "short",
            day: "numeric",
            hour: "numeric",
            minute: "2-digit"
          })}
          .
        </p>
      </div>

      <QRCodeDisplay value={listing.reservationCode || `GL-${listing.id}`} />

      {!collected && (
        <button
          type="button"
          className="auth-button"
          style={{ marginTop: "1rem", width: "100%" }}
          onClick={handleMarkPickedUp}
          disabled={collecting}
        >
          {collecting ? "Marking as picked up..." : "Mark as Picked Up"}
        </button>
      )}

      {collected && (
        <p style={{ textAlign: "center", marginTop: "1rem", color: "green", fontWeight: 600 }}>
          CO2 saved recorded!
        </p>
      )}
    </div>
  );
}
