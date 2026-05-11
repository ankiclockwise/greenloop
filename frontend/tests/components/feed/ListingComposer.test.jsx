import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { ListingComposer } from "../../../src/components/feed/ListingComposer";

async function fillValidListing(user) {
  await user.type(screen.getByLabelText("Listing name"), "Fresh fruit box");
  await user.clear(screen.getByLabelText("Quantity"));
  await user.type(screen.getByLabelText("Quantity"), "4");
  await user.type(screen.getByLabelText("Price"), "1.50");
  await user.type(screen.getByLabelText("Pickup window start"), "2026-05-10T10:00");
  await user.type(screen.getByLabelText("Pickup window end"), "2026-05-10T12:00");
  await user.type(screen.getByLabelText("Pickup location"), "Student Union");
  await user.type(screen.getByLabelText("Pickup notes"), "Use north entrance");
  await user.type(screen.getByLabelText("Description"), "Fresh apples and bananas");
}

describe("ListingComposer", () => {
  it("shows validation errors when required fields are missing", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<ListingComposer onSubmit={onSubmit} />);

    await user.clear(screen.getByLabelText("Listing name"));
    await user.clear(screen.getByLabelText("Pickup location"));
    await user.click(screen.getByRole("button", { name: "Post listing" }));

    expect(screen.getByText("Listing name is required.")).toBeInTheDocument();
    expect(screen.getByText("Pickup start time is required.")).toBeInTheDocument();
    expect(screen.getByText("Pickup end time is required.")).toBeInTheDocument();
    expect(screen.getByText("Pickup location is required.")).toBeInTheDocument();
    expect(screen.getByText("Please fix the highlighted fields before posting.")).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("validates that pickup end is after pickup start", async () => {
    const user = userEvent.setup();

    render(<ListingComposer onSubmit={vi.fn()} />);

    await user.type(screen.getByLabelText("Listing name"), "Soup");
    await user.type(screen.getByLabelText("Pickup window start"), "2026-05-10T12:00");
    await user.type(screen.getByLabelText("Pickup window end"), "2026-05-10T10:00");
    await user.type(screen.getByLabelText("Pickup location"), "Dining hall");
    await user.click(screen.getByRole("button", { name: "Post listing" }));

    expect(screen.getByText("Pickup end must be after pickup start.")).toBeInTheDocument();
  });

  it("submits valid listing values, resets the form, and shows success", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<ListingComposer onSubmit={onSubmit} />);

    await fillValidListing(user);
    await user.click(screen.getByRole("button", { name: "Vegan" }));
    await user.click(screen.getByRole("button", { name: "Family Pack" }));
    expect(screen.getByText("2 tags selected")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Post listing" }));

    expect(onSubmit).toHaveBeenCalledWith({
      name: "Fresh fruit box",
      category: "Produce",
      quantity: "4",
      price: "1.5",
      pickupWindowStart: "2026-05-10T10:00",
      pickupWindowEnd: "2026-05-10T12:00",
      pickupLocation: "Student Union",
      pickupNotes: "Use north entrance",
      description: "Fresh apples and bananas",
      tags: ["Vegan", "Family Pack"]
    });
    expect(screen.getByText("Listing drafted and added to the feed.")).toBeInTheDocument();
    expect(screen.getByLabelText("Listing name")).toHaveValue("");
    expect(within(screen.getByText("Tags").closest(".tag-picker")).getByText(
      "Add tags to help people filter and find this listing."
    )).toBeInTheDocument();
  });
});
