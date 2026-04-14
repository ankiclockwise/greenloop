const STATUS_LABELS = {
  available: "Available",
  reserved: "Reserved",
  confirmed: "Pickup confirmed",
  picked_up: "Picked up"
};

export function ReservationStatusIndicator({ status = "available" }) {
  return (
    <span className={`reservation-status reservation-status-${status}`}>
      {STATUS_LABELS[status] || STATUS_LABELS.available}
    </span>
  );
}
