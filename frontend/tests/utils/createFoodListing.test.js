import axios from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createFoodListing } from "../../src/utils/createFoodListing";
import { LOCAL_CREATED_LISTINGS_KEY } from "../../src/utils/listingDashboard";

vi.mock("axios", () => ({
  default: {
    post: vi.fn()
  }
}));

const formValues = {
  name: "Fresh fruit box",
  category: "Produce",
  quantity: "4",
  price: "1.5",
  pickupWindowStart: "2026-05-10T10:00",
  pickupWindowEnd: "2026-05-10T12:00",
  pickupLocation: "Student Union",
  pickupNotes: "Use north entrance",
  description: "Apples and bananas",
  tags: ["Vegan"]
};

const user = {
  email: "donor@example.com",
  displayName: "Donor User"
};

describe("createFoodListing", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    window.localStorage.clear();
  });

  it("posts listing values to the backend and stores the dashboard listing", async () => {
    axios.post.mockResolvedValueOnce({
      data: {
        id: 123,
        title: "Fresh fruit box",
        category: "PRODUCE",
        quantity: 4,
        status: "AVAILABLE",
        pickupAddress: "Student Union",
        pickupCity: "Amherst"
      }
    });

    const result = await createFoodListing(formValues, user);

    expect(axios.post).toHaveBeenCalledWith("/api/listings?ownerId=1", {
      title: "Fresh fruit box",
      description: "Apples and bananas",
      category: "PRODUCE",
      quantity: 4,
      unit: "piece",
      originalPrice: 1.5,
      discountedPrice: 1.5,
      pickupAddress: "Student Union",
      pickupWindowStart: "2026-05-10T10:00",
      pickupWindowEnd: "2026-05-10T12:00",
      expiresAt: "2026-05-10T12:00"
    });
    expect(result).toMatchObject({
      source: "api",
      listing: { id: 123, title: "Fresh fruit box" }
    });
    expect(JSON.parse(window.localStorage.getItem(LOCAL_CREATED_LISTINGS_KEY))[0]).toMatchObject({
      id: 123,
      title: "Fresh fruit box",
      ownerEmail: "donor@example.com"
    });
  });

  it("creates and stores a local listing when the backend is unavailable", async () => {
    axios.post.mockRejectedValueOnce(new Error("Backend unavailable"));
    vi.spyOn(console, "error").mockImplementation(() => {});

    const result = await createFoodListing(formValues, user);

    expect(result.source).toBe("local");
    expect(result.listing).toMatchObject({
      name: "Fresh fruit box",
      providerName: "Donor User",
      ownerEmail: "donor@example.com",
      reservationStatus: "available"
    });
    expect(JSON.parse(window.localStorage.getItem(LOCAL_CREATED_LISTINGS_KEY))[0]).toMatchObject({
      title: "Fresh fruit box",
      ownerEmail: "donor@example.com"
    });
  });
});
