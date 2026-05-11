import axios from "axios";
import { createListingFromForm } from "../data/mockListings";
import { normalizeDashboardListing, storeCreatedListing } from "./listingDashboard";

const CATEGORY_MAP = {
  Produce: "PRODUCE",
  Bakery: "BAKERY",
  Prepared: "PREPARED",
  Dairy: "DAIRY",
  "Dry Goods": "PANTRY",
  Beverages: "BEVERAGE"
};

export async function createFoodListing(formValues, user) {
  try {
    const response = await axios.post(`/api/listings?ownerId=${user?.dbId || 1}`, {
      title: formValues.name,
      description: formValues.description || formValues.name,
      category: CATEGORY_MAP[formValues.category] || formValues.category.toUpperCase(),
      quantity: parseInt(formValues.quantity, 10),
      unit: "piece",
      originalPrice: parseFloat(formValues.price) || 0,
      discountedPrice: parseFloat(formValues.price) || 0,
      pickupAddress: formValues.pickupLocation,
      pickupWindowStart: formValues.pickupWindowStart,
      pickupWindowEnd: formValues.pickupWindowEnd,
      expiresAt: formValues.pickupWindowEnd
    });

    const dashboardListing = normalizeDashboardListing({
      ...response.data,
      ownerEmail: user?.email
    });
    storeCreatedListing(dashboardListing);

    return { listing: response.data, source: "api" };
  } catch (error) {
    console.error("Failed to create listing:", error);
    const localListing = createListingFromForm(
      formValues,
      user?.displayName || user?.email,
      user?.email
    );
    storeCreatedListing(normalizeDashboardListing(localListing));

    return { listing: localListing, source: "local" };
  }
}
