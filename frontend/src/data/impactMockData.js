import {
  DEFAULT_USER_TYPE,
  FALLBACK_IMPACT_BY_USER_TYPE,
  USER_TYPES
} from "../constants/impactConstants";

export const BADGE_CATALOG = [
  {
    id: "first_donation",
    name: "First Donation",
    description: "Donated food for the first time.",
    appliesTo: [USER_TYPES.STUDENT, USER_TYPES.STORE, USER_TYPES.DINER],
    criteria: { metric: "foodDonated", threshold: 1 },
    earned: true,
    earnedAt: "2026-04-21T10:00:00Z"
  },
  {
    id: "community_helper",
    name: "Community Helper",
    description: "Shared 10 or more food donations.",
    appliesTo: [USER_TYPES.STUDENT, USER_TYPES.STORE, USER_TYPES.DINER],
    criteria: { metric: "donationCount", threshold: 10 },
    earned: true,
    earnedAt: "2026-04-29T16:30:00Z"
  },
  {
    id: "waste_warrior",
    name: "Waste Warrior",
    description: "Helped save 50kg or more of CO2.",
    appliesTo: [USER_TYPES.STUDENT, USER_TYPES.STORE, USER_TYPES.DINER],
    criteria: { metric: "co2SavedKg", threshold: 50 },
    earned: true,
    earnedAt: "2026-05-02T12:15:00Z"
  },
  {
    id: "first_pickup",
    name: "First Pickup",
    description: "Reserved and received food for the first time.",
    appliesTo: [USER_TYPES.STUDENT],
    criteria: { metric: "foodReceived", threshold: 1 },
    earned: false
  },
  {
    id: "campus_rescuer",
    name: "Campus Rescuer",
    description: "Received food 10 or more times.",
    appliesTo: [USER_TYPES.STUDENT],
    criteria: { metric: "pickupCount", threshold: 10 },
    earned: false
  },
  {
    id: "reliable_donor",
    name: "Reliable Donor",
    description: "Listed or donated food every week for 4 weeks.",
    appliesTo: [USER_TYPES.STORE, USER_TYPES.DINER],
    criteria: { metric: "weeklyDonationStreak", threshold: 4 },
    earned: false
  },
  {
    id: "neighborhood_hero",
    name: "Neighborhood Hero",
    description: "Helped complete 50 or more student pickups.",
    appliesTo: [USER_TYPES.STORE, USER_TYPES.DINER],
    criteria: { metric: "completedPickups", threshold: 50 },
    earned: false
  },
  {
    id: "top_contributor",
    name: "Top Contributor",
    description: "Reached the top 10 in your leaderboard.",
    appliesTo: [USER_TYPES.STUDENT, USER_TYPES.STORE, USER_TYPES.DINER],
    criteria: { metric: "leaderboardPosition", threshold: 10, comparison: "lessThanOrEqual" },
    earned: false
  },
  {
    id: "greenloop_champion",
    name: "GreenLoop Champion",
    description: "Saved 500kg or more of CO2 through GreenLoop activity.",
    appliesTo: [USER_TYPES.STUDENT, USER_TYPES.STORE, USER_TYPES.DINER],
    criteria: { metric: "co2SavedKg", threshold: 500 },
    earned: false
  }
];

export const DONOR_LEADERBOARD = [
  { rank: 1, userId: "store_1", userType: USER_TYPES.STORE, name: "Maple Market", foodDonated: 148, donationCount: 58, completedPickups: 132, co2SavedKg: 612.4 },
  { rank: 2, userId: "diner_1", userType: USER_TYPES.DINER, name: "North Campus Diner", foodDonated: 121, donationCount: 44, completedPickups: 118, co2SavedKg: 503.8 },
  { rank: 3, userId: "store_2", userType: USER_TYPES.STORE, name: "Fresh Basket", foodDonated: 95, donationCount: 37, completedPickups: 92, co2SavedKg: 394.2 },
  { rank: 4, userId: "diner_2", userType: USER_TYPES.DINER, name: "Elm Street Eats", foodDonated: 84, donationCount: 31, completedPickups: 77, co2SavedKg: 332.1 },
  { rank: 5, userId: "store_3", userType: USER_TYPES.STORE, name: "Green Shelf Grocery", foodDonated: 72, donationCount: 26, completedPickups: 69, co2SavedKg: 289.6 },
  { rank: 6, userId: "diner_3", userType: USER_TYPES.DINER, name: "Commons Kitchen", foodDonated: 69, donationCount: 24, completedPickups: 65, co2SavedKg: 271.3 },
  { rank: 7, userId: "store_4", userType: USER_TYPES.STORE, name: "Corner Pantry", foodDonated: 61, donationCount: 22, completedPickups: 56, co2SavedKg: 248.7 },
  { rank: 8, userId: "diner_4", userType: USER_TYPES.DINER, name: "Late Plate Cafe", foodDonated: 57, donationCount: 19, completedPickups: 48, co2SavedKg: 221.5 },
  { rank: 9, userId: "store_5", userType: USER_TYPES.STORE, name: "Harvest Co-op", foodDonated: 49, donationCount: 17, completedPickups: 43, co2SavedKg: 197.9 },
  { rank: 10, userId: "diner_5", userType: USER_TYPES.DINER, name: "South Hall Grill", foodDonated: 43, donationCount: 15, completedPickups: 39, co2SavedKg: 173.4 },
  { rank: 11, userId: "store_6", userType: USER_TYPES.STORE, name: "Oak Street Grocer", foodDonated: 38, donationCount: 13, completedPickups: 34, co2SavedKg: 151.8 },
  { rank: 12, userId: "diner_6", userType: USER_TYPES.DINER, name: "Union Kitchen", foodDonated: 34, donationCount: 11, completedPickups: 29, co2SavedKg: 136.2 }
];

export const STUDENT_LEADERBOARD = [
  { rank: 1, userId: "retail_user_1", userType: USER_TYPES.STUDENT, name: "Maya R.", foodReceived: 44, pickupCount: 39, foodDonated: 9, donationCount: 6, co2SavedKg: 103.2 },
  { rank: 2, userId: "retail_user_2", userType: USER_TYPES.STUDENT, name: "Jordan P.", foodReceived: 39, pickupCount: 35, foodDonated: 7, donationCount: 4, co2SavedKg: 94.8 },
  { rank: 3, userId: "retail_user_3", userType: USER_TYPES.STUDENT, name: "Ari S.", foodReceived: 35, pickupCount: 32, foodDonated: 11, donationCount: 8, co2SavedKg: 91.4 },
  { rank: 4, userId: "retail_user_4", userType: USER_TYPES.STUDENT, name: "Sam K.", foodReceived: 30, pickupCount: 28, foodDonated: 5, donationCount: 3, co2SavedKg: 75.9 },
  { rank: 5, userId: "retail_user_5", userType: USER_TYPES.STUDENT, name: "Nina T.", foodReceived: 28, pickupCount: 25, foodDonated: 4, donationCount: 3, co2SavedKg: 68.1 },
  { rank: 6, userId: "retail_user_6", userType: USER_TYPES.STUDENT, name: "Dev A.", foodReceived: 24, pickupCount: 22, foodDonated: 6, donationCount: 4, co2SavedKg: 62.6 },
  { rank: 7, userId: "retail_user_7", userType: USER_TYPES.STUDENT, name: "Leah M.", foodReceived: 19, pickupCount: 18, foodDonated: 3, donationCount: 2, co2SavedKg: 51.3 },
  { rank: 8, userId: "retail_user_8", userType: USER_TYPES.STUDENT, name: "Chris V.", foodReceived: 17, pickupCount: 16, foodDonated: 2, donationCount: 2, co2SavedKg: 43.7 },
  { rank: 9, userId: "retail_user_9", userType: USER_TYPES.STUDENT, name: "Priya N.", foodReceived: 15, pickupCount: 14, foodDonated: 4, donationCount: 3, co2SavedKg: 39.5 },
  { rank: 10, userId: "retail_user_10", userType: USER_TYPES.STUDENT, name: "Eli W.", foodReceived: 13, pickupCount: 13, foodDonated: 1, donationCount: 1, co2SavedKg: 33.8 },
  { rank: 11, userId: "retail_user_11", userType: USER_TYPES.STUDENT, name: "Tara J.", foodReceived: 11, pickupCount: 10, foodDonated: 2, donationCount: 2, co2SavedKg: 28.6 },
  { rank: 12, userId: "retail_user_12", userType: USER_TYPES.STUDENT, name: "Owen C.", foodReceived: 9, pickupCount: 9, foodDonated: 1, donationCount: 1, co2SavedKg: 22.4 }
];

export function createFallbackImpact(user) {
  const userType = user?.actor || DEFAULT_USER_TYPE;
  const fallbackImpact = FALLBACK_IMPACT_BY_USER_TYPE[userType] || FALLBACK_IMPACT_BY_USER_TYPE[DEFAULT_USER_TYPE];

  return {
    userType,
    ...fallbackImpact,
    badges: BADGE_CATALOG.filter((badge) => badge.earned && badge.appliesTo.includes(userType))
  };
}
