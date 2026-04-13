import { QRCodeDisplay } from "./QRCodeDisplay";
import { ReservationStatusIndicator } from "./ReservationStatusIndicator";

export function ReservationConfirmation({ listing, onClose }) {
  if (!listing) {
    return null;
  }

  return (
    <div className="confirmation-card">
      <div className="section-header">
        <div>
          <span className="eyebrow">Reservation confirmed</span>
          <h2>{listing.title}</h2>
        </div>
        <button type="button" className="dismiss-button" onClick={onClose}>
          Close
        </button>
      </div>

      <div className="confirmation-summary">
        <ReservationStatusIndicator status={listing.reservationStatus} />
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
    </div>
  );
}
