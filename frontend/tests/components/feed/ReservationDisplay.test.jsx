import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { QRCodeDisplay } from "../../../src/components/feed/QRCodeDisplay";
import { ReservationConfirmation } from "../../../src/components/feed/ReservationConfirmation";
import { ReservationStatusIndicator } from "../../../src/components/feed/ReservationStatusIndicator";

describe("ReservationStatusIndicator", () => {
  it("shows labels for known and unknown statuses", () => {
    const { rerender } = render(<ReservationStatusIndicator status="confirmed" />);
    expect(screen.getByText("Pickup confirmed")).toHaveClass(
      "reservation-status-confirmed"
    );

    rerender(<ReservationStatusIndicator status="mystery" />);
    expect(screen.getByText("Available")).toHaveClass("reservation-status-mystery");
  });
});

describe("QRCodeDisplay", () => {
  it("renders a deterministic qr-style grid for a reservation code", () => {
    render(<QRCodeDisplay value="GL-123" />);

    expect(screen.getByLabelText("QR code for GL-123")).toBeInTheDocument();
    expect(screen.getByText("GL-123")).toBeInTheDocument();
    expect(document.querySelectorAll(".qr-cell")).toHaveLength(121);
    expect(document.querySelectorAll(".qr-cell.filled").length).toBeGreaterThan(0);
  });
});

describe("ReservationConfirmation", () => {
  it("renders nothing without a listing", () => {
    const { container } = render(<ReservationConfirmation listing={null} onClose={vi.fn()} />);
    expect(container).toBeEmptyDOMElement();
  });

  it("shows confirmation details and closes", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
      <ReservationConfirmation
        listing={{
          id: "abc",
          title: "Campus Salad Bowls",
          pickupLocation: "Worcester Dining Commons",
          pickupWindowEnd: "2026-05-10T18:30:00.000Z",
          reservationStatus: "confirmed",
          reservationCode: "GL-SALAD"
        }}
        onClose={onClose}
      />
    );

    expect(screen.getByText("Reservation confirmed")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Campus Salad Bowls" })).toBeInTheDocument();
    expect(screen.getByText("Worcester Dining Commons")).toBeInTheDocument();
    expect(screen.getByText("GL-SALAD")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Close" }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
