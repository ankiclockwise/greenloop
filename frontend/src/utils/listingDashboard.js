export const LOCAL_CREATED_LISTINGS_KEY = "greenloop-created-listings";

const CATEGORY_CO2_ESTIMATES = {
  Produce: 0.7,
  PRODUCE: 0.7,
  Bakery: 0.5,
  BAKERY: 0.5,
  Prepared: 1.2,
  PREPARED: 1.2,
  Dairy: 1.8,
  DAIRY: 1.8,
  "Dry Goods": 0.8,
  PANTRY: 0.8,
  Beverages: 0.4,
  BEVERAGE: 0.4
};

// Mirrors GET /api/listings/owner/{ownerId}. Each reservation reserves a
// quantity from the listing, so several users can share one listing.
export const DASHBOARD_SAMPLE_LISTINGS = [
  {
    id: 101,
    title: "Campus Salad Bowls",
    description: "Fresh end-of-day grain bowls with roasted vegetables and lemon herb dressing.",
    category: "PREPARED",
    quantity: 8,
    unit: "bowls",
    status: "AVAILABLE",
    reservationCount: 0,
    co2SavedKg: 9.6,
    pickupAddress: "Worcester Dining Commons",
    pickupCity: "Amherst",
    pickupWindowStart: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
    pickupWindowEnd: new Date(Date.now() + 90 * 60 * 1000).toISOString(),
    expiresAt: new Date(Date.now() + 90 * 60 * 1000).toISOString(),
    owner: {
      id: 1,
      name: "Your GreenLoop Hub",
      email: "donor@greenloop.test"
    },
    reservations: []
  },
  {
    id: 102,
    title: "Fresh Bakery Bread",
    description: "Assorted sourdough and sandwich loaves baked this morning.",
    category: "BAKERY",
    quantity: 5,
    unit: "loaves",
    status: "AVAILABLE",
    reservationCount: 2,
    co2SavedKg: 2.5,
    pickupAddress: "Main Street Market",
    pickupCity: "Amherst",
    pickupWindowStart: new Date(Date.now() + 20 * 60 * 1000).toISOString(),
    pickupWindowEnd: new Date(Date.now() + 120 * 60 * 1000).toISOString(),
    expiresAt: new Date(Date.now() + 120 * 60 * 1000).toISOString(),
    owner: {
      id: 1,
      name: "Your GreenLoop Hub",
      email: "donor@greenloop.test"
    },
    reservations: [
      {
        id: 501,
        status: "RESERVED",
        quantityReserved: 2,
        reservedAt: new Date(Date.now() - 18 * 60 * 1000).toISOString(),
        pickupCode: "GL-BREAD-21",
        reservedBy: {
          id: 22,
          name: "Maya Patel",
          email: "maya.patel@umass.edu",
          phone: "(413) 555-0148"
        }
      },
      {
        id: 506,
        status: "RESERVED",
        quantityReserved: 1,
        reservedAt: new Date(Date.now() - 9 * 60 * 1000).toISOString(),
        pickupCode: "GL-BREAD-22",
        reservedBy: {
          id: 34,
          name: "Elena Brooks",
          email: "elena.brooks@umass.edu",
          phone: "(413) 555-0176"
        }
      }
    ]
  },
  {
    id: 103,
    title: "Fresh Fruit Box",
    description: "Mixed apples, oranges, and bananas boxed for same-day pickup.",
    category: "PRODUCE",
    quantity: 10,
    unit: "boxes",
    status: "COLLECTED",
    reservationCount: 1,
    co2SavedKg: 7,
    pickupAddress: "Student Union North Entrance",
    pickupCity: "Amherst",
    pickupWindowStart: new Date(Date.now() - 150 * 60 * 1000).toISOString(),
    pickupWindowEnd: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
    expiresAt: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
    owner: {
      id: 1,
      name: "Your GreenLoop Hub",
      email: "donor@greenloop.test"
    },
    reservations: [
      {
        id: 502,
        status: "COLLECTED",
        quantityReserved: 10,
        reservedAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
        collectedAt: new Date(Date.now() - 70 * 60 * 1000).toISOString(),
        pickupCode: "GL-FRUIT-09",
        reservedBy: {
          id: 29,
          name: "Jordan Lee",
          email: "jordan.lee@umass.edu",
          phone: "(413) 555-0182"
        }
      }
    ]
  },
  {
    id: 104,
    title: "Campus Stir-Fry Leftovers",
    description: "Hot boxed vegetable stir-fry portions after dinner service.",
    category: "PREPARED",
    quantity: 12,
    unit: "portions",
    status: "RESERVED",
    co2SavedKg: 14.4,
    reservationCount: 3,
    pickupAddress: "Berkshire Dining Hall",
    pickupCity: "Amherst",
    pickupWindowStart: new Date(Date.now() - 120 * 60 * 1000).toISOString(),
    pickupWindowEnd: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
    expiresAt: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
    owner: {
      id: 1,
      name: "Your GreenLoop Hub",
      email: "donor@greenloop.test"
    },
    reservations: [
      {
        id: 503,
        status: "RESERVED",
        quantityReserved: 4,
        reservedAt: new Date(Date.now() - 90 * 60 * 1000).toISOString(),
        pickupCode: "GL-STIR-01",
        reservedBy: {
          id: 31,
          name: "Ari Morgan",
          email: "ari.morgan@umass.edu",
          phone: "(413) 555-0191"
        }
      },
      {
        id: 504,
        status: "RESERVED",
        quantityReserved: 3,
        reservedAt: new Date(Date.now() - 82 * 60 * 1000).toISOString(),
        pickupCode: "GL-STIR-02",
        reservedBy: {
          id: 32,
          name: "Sam Rivera",
          email: "sam.rivera@umass.edu",
          phone: "(413) 555-0133"
        }
      },
      {
        id: 505,
        status: "RESERVED",
        quantityReserved: 5,
        reservedAt: new Date(Date.now() - 75 * 60 * 1000).toISOString(),
        pickupCode: "GL-STIR-03",
        reservedBy: {
          id: 33,
          name: "Nora Chen",
          email: "nora.chen@umass.edu",
          phone: "(413) 555-0157"
        }
      }
    ]
  }
];

function readLocalListings() {
  try {
    return JSON.parse(window.localStorage.getItem(LOCAL_CREATED_LISTINGS_KEY) || "[]");
  } catch {
    return [];
  }
}

function writeLocalListings(listings) {
  window.localStorage.setItem(LOCAL_CREATED_LISTINGS_KEY, JSON.stringify(listings));
}

export function getLocalCreatedListings(userEmail) {
  const listings = readLocalListings();

  if (!userEmail) {
    return listings;
  }

  return listings.filter((listing) => !listing.ownerEmail || listing.ownerEmail === userEmail);
}

export function storeCreatedListing(listing) {
  const current = readLocalListings();
  const withoutDuplicate = current.filter((item) => String(item.id) !== String(listing.id));
  writeLocalListings([listing, ...withoutDuplicate].slice(0, 40));
}

export function normalizeDashboardListing(listing) {
  const title = listing.title || listing.name || "Untitled listing";
  const pickupLocation = [listing.pickupAddress, listing.pickupCity]
    .filter(Boolean)
    .join(", ") || listing.pickupLocation || "Pickup location pending";
  const status = normalizeListingStatus(listing);
  const co2Kg = getListingCo2Impact(listing);
  const reservations = normalizeReservations(listing.reservations || listing.reservationDetails || []);
  const quantity = Number(listing.quantity || 0);
  const reservedQuantity = getReservedQuantity(reservations);
  const collectedQuantity = getCollectedQuantity(reservations);
  const availableQuantity = Math.max(quantity - reservedQuantity, 0);

  return {
    ...listing,
    id: listing.id,
    title,
    name: title,
    pickupLocation,
    status,
    reservationStatus: status,
    reservations,
    reservedBy: listing.reservedBy || reservations[0]?.reservedBy || null,
    reservationCount: Number(listing.reservationCount ?? reservations.length),
    reservedQuantity,
    availableQuantity,
    collectedQuantity,
    co2Kg
  };
}

export function normalizeReservations(reservations) {
  return reservations.map((reservation) => ({
    id: reservation.id,
    status: String(reservation.status || "RESERVED").toLowerCase(),
    quantityReserved: Number(reservation.quantityReserved ?? reservation.quantity ?? 1),
    reservedAt: reservation.reservedAt || reservation.createdAt || "",
    collectedAt: reservation.collectedAt || "",
    pickupCode: reservation.pickupCode || reservation.reservationCode || "",
    reservedBy: reservation.reservedBy || reservation.user || null
  }));
}

export function getReservedQuantity(reservations) {
  return reservations
    .filter((reservation) => !["cancelled", "expired", "no_show"].includes(reservation.status))
    .reduce((total, reservation) => total + reservation.quantityReserved, 0);
}

export function getCollectedQuantity(reservations) {
  return reservations
    .filter((reservation) => reservation.status === "collected")
    .reduce((total, reservation) => total + reservation.quantityReserved, 0);
}

export function normalizeListingStatus(listing) {
  const rawStatus = listing.status || listing.reservationStatus || "available";
  const status = String(rawStatus).toLowerCase();
  const expiry = listing.expiresAt || listing.pickupWindowEnd;

  if (expiry && new Date(expiry) < new Date() && !["collected", "cancelled"].includes(status)) {
    return "expired";
  }

  if (status === "picked_up" || status === "confirmed") {
    return status === "picked_up" ? "collected" : "reserved";
  }

  if (["available", "reserved", "collected", "expired"].includes(status)) {
    return status;
  }

  return "available";
}

export function getListingCo2Impact(listing) {
  const explicitValue = listing.co2SavedKg ?? listing.co2Kg ?? listing.co2ImpactKg;

  if (explicitValue != null && !Number.isNaN(Number(explicitValue))) {
    return Number(explicitValue);
  }

  const quantity = Number(listing.quantity || 1);
  const perUnitEstimate = CATEGORY_CO2_ESTIMATES[listing.category] || 0.9;

  return Number((quantity * perUnitEstimate).toFixed(1));
}

export function getDashboardMetrics(listings) {
  const normalizedListings = listings.map(normalizeDashboardListing);
  const statusCounts = {
    available: 0,
    reserved: 0,
    collected: 0,
    expired: 0
  };

  normalizedListings.forEach((listing) => {
    if (statusCounts[listing.status] != null) {
      statusCounts[listing.status] += 1;
    }
  });

  const totalCo2Kg = normalizedListings.reduce(
    (total, listing) => total + listing.co2Kg,
    0
  );

  return {
    listings: normalizedListings,
    statusCounts,
    totalCo2Kg: Number(totalCo2Kg.toFixed(1))
  };
}
