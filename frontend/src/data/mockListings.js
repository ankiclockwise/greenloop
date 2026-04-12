export const DEFAULT_MOCK_LISTINGS = [
  {
    id: "mock-1",
    name: "Campus Salad Bowls",
    category: "Prepared",
    price: 0,
    distance: 0.7,
    pickupWindowStart: new Date().toISOString(),
    pickupWindowEnd: new Date(Date.now() + 1000 * 60 * 90).toISOString(),
    dietary: ["Vegan", "Gluten-Free"],
    allergens: ["Nuts"],
    imageUrl: "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=800&q=80"
  },
  {
    id: "mock-2",
    name: "Fresh Bakery Bread",
    category: "Bakery",
    price: 2.5,
    distance: 1.2,
    pickupWindowStart: new Date().toISOString(),
    pickupWindowEnd: new Date(Date.now() + 1000 * 60 * 120).toISOString(),
    dietary: ["Vegetarian"],
    allergens: ["Wheat"],
    imageUrl: "https://images.unsplash.com/photo-1511690743698-d9d85f2fbf38?auto=format&fit=crop&w=800&q=80"
  },
  {
    id: "mock-3",
    name: "Dairy-Free Smoothie Packs",
    category: "Dry Goods",
    price: 3.0,
    distance: 2.1,
    pickupWindowStart: new Date().toISOString(),
    pickupWindowEnd: new Date(Date.now() + 1000 * 60 * 45).toISOString(),
    dietary: ["Vegan"],
    allergens: ["Soy"],
    imageUrl: "https://images.unsplash.com/photo-1510626176961-4b07ac8b7e4f?auto=format&fit=crop&w=800&q=80"
  },
  {
    id: "mock-4",
    name: "Campus Stir-Fry Leftovers",
    category: "Prepared",
    price: 0,
    distance: 3.4,
    pickupWindowStart: new Date().toISOString(),
    pickupWindowEnd: new Date(Date.now() + 1000 * 60 * 60).toISOString(),
    dietary: ["Halal"],
    allergens: ["Eggs"],
    imageUrl: "https://images.unsplash.com/photo-1525755662778-989d0524087e?auto=format&fit=crop&w=800&q=80"
  },
  {
    id: "mock-5",
    name: "Fresh Fruit Box",
    category: "Produce",
    price: 1.0,
    distance: 0.9,
    pickupWindowStart: new Date().toISOString(),
    pickupWindowEnd: new Date(Date.now() + 1000 * 60 * 75).toISOString(),
    dietary: ["Vegan", "Kosher"],
    allergens: [],
    imageUrl: "https://images.unsplash.com/photo-1506806732259-39c2d0268443?auto=format&fit=crop&w=800&q=80"
  }
];
