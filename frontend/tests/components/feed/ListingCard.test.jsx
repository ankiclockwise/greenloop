import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { ListingCard } from "../../../src/components/feed/ListingCard";

const listing = {
  id: "listing-1",
  name: "Fresh Bakery Bread",
  category: "Bakery",
  price: 2.5,
  quantity: 5,
  providerName: "Main Street Market",
  distance: 1.2,
  pickupWindowEnd: "2026-05-10T18:30:00.000Z",
  reservationStatus: "available",
  tags: ["Vegetarian"],
  allergens: ["Wheat"],
  imageUrl: "https://example.com/bread.jpg"
};

describe("ListingCard", () => {
  it("renders listing details for a paid listing", () => {
    render(<ListingCard listing={listing} onSelect={vi.fn()} />);

    expect(screen.getByRole("heading", { name: "Fresh Bakery Bread" })).toBeInTheDocument();
    expect(screen.getByText("$2.50")).toBeInTheDocument();
    expect(screen.getByText("5 left from Main Street Market")).toBeInTheDocument();
    expect(screen.getByText("1.2 mi away", { exact: false })).toBeInTheDocument();
    expect(screen.getByText("Vegetarian")).toBeInTheDocument();
    expect(screen.getByText("Contains Wheat")).toBeInTheDocument();
    expect(screen.getByRole("img", { name: "Fresh Bakery Bread" })).toHaveAttribute(
      "src",
      listing.imageUrl
    );
  });

  it("renders free listings and a placeholder when no image exists", () => {
    render(<ListingCard listing={{ ...listing, price: 0, imageUrl: "" }} onSelect={vi.fn()} />);

    expect(screen.getByText("Free", { exact: false })).toBeInTheDocument();
    expect(screen.getByText("🥗")).toBeInTheDocument();
  });

  it("selects the listing from the card or details button", async () => {
    const user = userEvent.setup();
    const onSelect = vi.fn();

    render(<ListingCard listing={listing} onSelect={onSelect} />);

    await user.click(screen.getByRole("article"));
    await user.click(screen.getByRole("button", { name: "View details" }));

    expect(onSelect).toHaveBeenCalledTimes(2);
    expect(onSelect).toHaveBeenCalledWith(listing);
  });
});
