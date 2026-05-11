export const PAGE_SIZE = 5;
export const INITIAL_PAGE = 1;
export const PAGE_STEP = 1;

export const IMPACT_ROUTES = {
  FEED: "/",
  IMPACT: "/impact"
};

export const USER_TYPES = {
  STUDENT: "retail_user",
  STORE: "store",
  DINER: "diner"
};

export const DEFAULT_USER_TYPE = USER_TYPES.STUDENT;

export const USER_TYPE_LABELS = {
  [USER_TYPES.STUDENT]: "Student",
  [USER_TYPES.STORE]: "Store",
  [USER_TYPES.DINER]: "Diner",
  DEFAULT: "Member"
};

export const IMPACT_API_ENDPOINTS = {
  ME: "/api/impact/me",
  BADGES: "/api/impact/badges",
  DONOR_LEADERBOARD: "/api/impact/leaderboard/donors",
  STUDENT_LEADERBOARD: "/api/impact/leaderboard/students"
};

export const IMPACT_COPY = {
  PAGE_TITLE: "GreenLoop Impact",
  NAV_LABEL: "Main navigation",
  FEED_TAB: "Feed",
  IMPACT_TAB: "Impact",
  DONATE_FOOD: "Donate Food",
  LOGOUT: "Logout",
  MY_IMPACT_EYEBROW: "My Impact",
  MY_IMPACT_TITLE: "Your food loop is working",
  BADGES_EYEBROW: "My Badges",
  BADGES_TITLE: "Recognition",
  LEADERBOARD_EYEBROW: "Leaderboard",
  DONOR_LEADERBOARD_TITLE: "Stores + Diners",
  STUDENT_LEADERBOARD_TITLE: "Students",
  DATA_NOTE: "Showing sample impact data until the backend impact APIs are available.",
  PREVIOUS_PAGE: "Previous",
  NEXT_PAGE: "Next",
  PAGE_LABEL: "Page",
  PAGE_OF_LABEL: "of",
  LOCKED_BADGE: "Locked",
  EARNED_BADGE_DATE_PREFIX: "Earned",
  BADGE_INFO_SYMBOL: "i",
  IMPACT_METRICS_ARIA_LABEL: "My impact metrics",
  LEADERBOARDS_ARIA_LABEL: "Leaderboards"
};

export const METRIC_LABELS = {
  FOOD_RECEIVED: "Food received",
  FOOD_DONATED: "Food donated",
  CO2_SAVED: "CO2 saved",
  LEADERBOARD_POSITION: "Leaderboard position",
  BADGES_EARNED: "Badges earned"
};

export const LEADERBOARD_COLUMNS = {
  RANK: "Rank",
  NAME: "Name",
  RECEIVED: "Received",
  DONATED: "Donated",
  CO2_SAVED: "CO2 saved"
};

export const UNITS = {
  KG: "kg"
};

export const FALLBACK_IMPACT_BY_USER_TYPE = {
  [USER_TYPES.STUDENT]: {
    foodReceived: 18,
    foodDonated: 8,
    donationCount: 6,
    pickupCount: 16,
    completedPickups: null,
    co2SavedKg: 54.5,
    leaderboardPosition: 14
  },
  [USER_TYPES.STORE]: {
    foodReceived: null,
    foodDonated: 42,
    donationCount: 15,
    pickupCount: null,
    completedPickups: 38,
    co2SavedKg: 180.2,
    leaderboardPosition: 3
  },
  [USER_TYPES.DINER]: {
    foodReceived: null,
    foodDonated: 36,
    donationCount: 12,
    pickupCount: null,
    completedPickups: 31,
    co2SavedKg: 151.7,
    leaderboardPosition: 5
  }
};
