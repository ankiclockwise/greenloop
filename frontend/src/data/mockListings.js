const baseTime = Date.now();

function isoFromNow(minutesFromNow) {
  return new Date(baseTime + minutesFromNow * 60 * 1000).toISOString();
}

export const CATEGORY_OPTIONS = [
  "Produce",
  "Bakery",
  "Prepared",
  "Dairy",
  "Dry Goods",
  "Beverages"
];

export const TAG_OPTIONS = [
  "Vegan",
  "Vegetarian",
  "Gluten-Free",
  "Halal",
  "Kosher",
  "High Protein",
  "Family Pack"
];

export const DEFAULT_MOCK_LISTINGS = [
  {
    id: "mock-1",
    name: "Campus Salad Bowls",
    title: "Campus Salad Bowls",
    description: "Fresh end-of-day grain bowls with roasted vegetables and lemon herb dressing.",
    category: "Prepared",
    tags: ["Vegan", "Gluten-Free"],
    dietary: ["Vegan", "Gluten-Free"],
    allergens: ["Nuts"],
    quantity: 8,
    price: 0,
    distance: 0.7,
    pickupWindowStart: isoFromNow(15),
    pickupWindowEnd: isoFromNow(90),
    pickupLocation: "Worcester Dining Commons",
    pickupNotes: "Meet near the east entrance and bring your student ID.",
    providerName: "UMass Dining",
    providerType: "Dining Hall",
    reservationStatus: "available",
    imageUrl:
      "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=800&q=80"
  },
  {
    id: "mock-2",
    name: "Fresh Bakery Bread",
    title: "Fresh Bakery Bread",
    description: "Assorted sourdough and sandwich loaves baked this morning and ready for quick pickup.",
    category: "Bakery",
    tags: ["Vegetarian", "Family Pack"],
    dietary: ["Vegetarian"],
    allergens: ["Wheat"],
    quantity: 5,
    price: 2.5,
    distance: 1.2,
    pickupWindowStart: isoFromNow(20),
    pickupWindowEnd: isoFromNow(120),
    pickupLocation: "Main Street Market",
    pickupNotes: "Ask for the GreenLoop shelf near checkout.",
    providerName: "Main Street Market",
    providerType: "Retailer",
    reservationStatus: "reserved",
    reservationCode: "GL-BREAD-21",
    imageUrl:
      "https://images.unsplash.com/photo-1511690743698-d9d85f2fbf38?auto=format&fit=crop&w=800&q=80"
  },
  {
    id: "mock-3",
    name: "Dairy-Free Smoothie Packs",
    title: "Dairy-Free Smoothie Packs",
    description: "Frozen fruit, spinach, and chia smoothie packs portioned for four breakfasts.",
    category: "Beverages",
    tags: ["Vegan", "High Protein"],
    dietary: ["Vegan"],
    allergens: ["Soy"],
    quantity: 3,
    price: 3,
    distance: 2.1,
    pickupWindowStart: isoFromNow(5),
    pickupWindowEnd: isoFromNow(45),
    pickupLocation: "Campus Pantry Hub",
    pickupNotes: "Pickup locker code appears after reservation.",
    providerName: "Campus Pantry Hub",
    providerType: "Donor",
    reservationStatus: "confirmed",
    reservationCode: "GL-SMOOTH-44",
    imageUrl:
      "https://images.unsplash.com/photo-1510626176961-4b07ac8b7e4f?auto=format&fit=crop&w=800&q=80"
  },
  {
    id: "mock-4",
    name: "Campus Stir-Fry Leftovers",
    title: "Campus Stir-Fry Leftovers",
    description: "Hot boxed vegetable stir-fry portions available after dinner service.",
    category: "Prepared",
    tags: ["Halal"],
    dietary: ["Halal"],
    allergens: ["Eggs"],
    quantity: 12,
    price: 0,
    distance: 3.4,
    pickupWindowStart: isoFromNow(25),
    pickupWindowEnd: isoFromNow(60),
    pickupLocation: "Berkshire Dining Hall",
    pickupNotes: "Pickup closes sharply at end of service.",
    providerName: "Berkshire Dining",
    providerType: "Dining Hall",
    reservationStatus: "available",
    imageUrl:
      "https://images.unsplash.com/photo-1525755662778-989d0524087e?auto=format&fit=crop&w=800&q=80"
  },
  {
    id: "mock-5",
    name: "Fresh Fruit Box",
    title: "Fresh Fruit Box",
    description: "Mixed apples, oranges, and bananas boxed for same-day pickup.",
    category: "Produce",
    tags: ["Vegan", "Kosher", "Family Pack"],
    dietary: ["Vegan", "Kosher"],
    allergens: [],
    quantity: 10,
    price: 1,
    distance: 0.9,
    pickupWindowStart: isoFromNow(10),
    pickupWindowEnd: isoFromNow(75),
    pickupLocation: "Student Union North Entrance",
    pickupNotes: "Text the volunteer when you arrive.",
    providerName: "North Amherst Volunteers",
    providerType: "Donor",
    reservationStatus: "picked_up",
    reservationCode: "GL-FRUIT-09",
    imageUrl:
      "https://images.unsplash.com/photo-1506806732259-39c2d0268443?auto=format&fit=crop&w=800&q=80"
  }
];

export function createListingFromForm(values, ownerName, ownerEmail) {
  return {
    id: `local-${Date.now()}`,
    name: values.name.trim(),
    title: values.name.trim(),
    description: values.description.trim() || "Newly posted listing",
    category: values.category,
    tags: values.tags,
    dietary: values.tags,
    allergens: [],
    quantity: Number(values.quantity),
    price: values.price === "" ? 0 : Number(values.price),
    distance: 0.2,
    pickupWindowStart: new Date(values.pickupWindowStart).toISOString(),
    pickupWindowEnd: new Date(values.pickupWindowEnd).toISOString(),
    pickupLocation: values.pickupLocation.trim(),
    pickupNotes: values.pickupNotes.trim(),
    providerName: ownerName || "Your GreenLoop Hub",
    ownerEmail: ownerEmail || "",
    providerType: "Retailer",
    reservationStatus: "available",
    imageUrl: ""
  };
}
