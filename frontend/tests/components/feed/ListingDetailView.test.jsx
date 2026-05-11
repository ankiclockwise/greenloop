import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ListingDetailView } from "../../../src/components/feed/ListingDetailView";

const authState = vi.hoisted(() => ({ user: null }));

vi.mock("../../../src/auth/AuthProvider", () => ({
  useAuth: () => ({ user: authState.user })
}));

const listing = {
  id: "listing-1",
  title: "Campus Salad Bowls",
  description: "Fresh bowls with roasted vegetables.",
  category: "Prepared",
  quantity: 8,
  price: 0,
  pickupWindowStart: "2026-05-10T16:00:00.000Z",
  pickupWindowEnd: "2026-05-10T18:00:00.000Z",
  pickupLocation: "Worcester Dining Commons",
  pickupNotes: "Meet near the east entrance.",
  tags: ["Vegan", "Gluten-Free"],
  reservationStatus: "available",
  ownerEmail: "owner@example.com"
};

describe("ListingDetailView", () => {
  beforeEach(() => {
    authState.user = { email: "student@example.com" };
  });

  it("renders nothing without a listing", () => {
    const { container } = render(
      <ListingDetailView listing={null} onReserve={vi.fn()} onClose={vi.fn()} />
    );
    expect(container).toBeEmptyDOMElement();
  });

  it("renders listing details and reserves available listings", async () => {
    const user = userEvent.setup();
    const onReserve = vi.fn();
    const onClose = vi.fn();

    render(
      <ListingDetailView
        listing={listing}
        onReserve={onReserve}
        onClose={onClose}
        reserving={false}
      />
    );

    expect(screen.getByRole("heading", { name: "Campus Salad Bowls" })).toBeInTheDocument();
    expect(screen.getByText("Fresh bowls with roasted vegetables.")).toBeInTheDocument();
    expect(screen.getByText("8 available")).toBeInTheDocument();
    expect(screen.getByText("Free")).toBeInTheDocument();
    expect(screen.getByText("Worcester Dining Commons")).toBeInTheDocument();
    expect(screen.getByText("Vegan")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Reserve" }));
    await user.click(screen.getByRole("button", { name: "Close" }));

    expect(onReserve).toHaveBeenCalledWith("listing-1");
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it("disables reservation when listing is already reserved", () => {
    render(
      <ListingDetailView
        listing={{
          ...listing,
          reservationStatus: "confirmed",
          reservationCode: "GL-SALAD"
        }}
        onReserve={vi.fn()}
        onClose={vi.fn()}
      />
    );

    expect(screen.getByRole("button", { name: "Already reserved" })).toBeDisabled();
    expect(screen.getByText("Pickup code")).toBeInTheDocument();
    expect(screen.getByText("GL-SALAD")).toBeInTheDocument();
  });

  it("prevents users from reserving their own listing", () => {
    authState.user = { email: "owner@example.com" };

    render(
      <ListingDetailView listing={listing} onReserve={vi.fn()} onClose={vi.fn()} />
    );

    expect(screen.getByText("This is your own listing, so it cannot be reserved from your account.")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Your listing" })).toBeDisabled();
  });

  it("shows the reserving button state", () => {
    render(
      <ListingDetailView
        listing={listing}
        onReserve={vi.fn()}
        onClose={vi.fn()}
        reserving
      />
    );

    expect(screen.getByRole("button", { name: "Reserving..." })).toBeEnabled();
  });
});
