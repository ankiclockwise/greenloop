import { describe, expect, it } from "vitest";
import {
  getDashboardMetrics,
  getListingCo2Impact,
  getReservedQuantity,
  normalizeDashboardListing,
  normalizeListingStatus,
  normalizeReservations
} from "../../src/utils/listingDashboard";

describe("listingDashboard", () => {
  it("normalizes backend-style reservation fields for dashboard use", () => {
    const reservations = normalizeReservations([
      {
        id: 1,
        status: "COLLECTED",
        quantity: "3",
        createdAt: "2026-05-10T10:00:00.000Z",
        reservationCode: "GL-123",
        user: { name: "Maya" }
      }
    ]);

    expect(reservations).toEqual([
      {
        id: 1,
        status: "collected",
        quantityReserved: 3,
        reservedAt: "2026-05-10T10:00:00.000Z",
        collectedAt: "",
        pickupCode: "GL-123",
        reservedBy: { name: "Maya" }
      }
    ]);
  });

  it("does not count cancelled, expired, or no-show reservations as reserved quantity", () => {
    expect(
      getReservedQuantity([
        { status: "reserved", quantityReserved: 2 },
        { status: "collected", quantityReserved: 1 },
        { status: "cancelled", quantityReserved: 5 },
        { status: "no_show", quantityReserved: 4 }
      ])
    ).toBe(3);
  });

  it("marks listings expired when pickup time has passed", () => {
    expect(
      normalizeListingStatus({
        status: "AVAILABLE",
        expiresAt: "2000-01-01T00:00:00.000Z"
      })
    ).toBe("expired");
  });

  it("calculates listing impact from explicit values or category estimates", () => {
    expect(getListingCo2Impact({ co2SavedKg: "4.2", quantity: 99 })).toBe(4.2);
    expect(getListingCo2Impact({ category: "PRODUCE", quantity: 3 })).toBe(2.1);
  });

  it("normalizes listing summaries with available and reserved quantities", () => {
    const listing = normalizeDashboardListing({
      id: 10,
      name: "Bread",
      category: "BAKERY",
      quantity: 5,
      pickupAddress: "Main Street Market",
      pickupCity: "Amherst",
      reservations: [
        { status: "RESERVED", quantityReserved: 2 },
        { status: "CANCELLED", quantityReserved: 1 }
      ]
    });

    expect(listing).toMatchObject({
      id: 10,
      title: "Bread",
      pickupLocation: "Main Street Market, Amherst",
      status: "available",
      reservedQuantity: 2,
      availableQuantity: 3,
      co2Kg: 2.5
    });
  });

  it("returns dashboard counts and total co2 across listings", () => {
    const metrics = getDashboardMetrics([
      { id: 1, title: "Salad", category: "PREPARED", quantity: 2, status: "AVAILABLE" },
      { id: 2, title: "Fruit", category: "PRODUCE", quantity: 3, status: "COLLECTED" }
    ]);

    expect(metrics.statusCounts).toEqual({
      available: 1,
      reserved: 0,
      collected: 1,
      expired: 0
    });
    expect(metrics.totalCo2Kg).toBe(4.5);
  });
});
