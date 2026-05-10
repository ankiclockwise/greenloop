import {
  DEFAULT_USER_TYPE,
  INITIAL_PAGE,
  METRIC_LABELS,
  UNITS,
  USER_TYPE_LABELS,
  USER_TYPES
} from "../constants/impactConstants";

export function formatUserType(userType) {
  return USER_TYPE_LABELS[userType] || USER_TYPE_LABELS.DEFAULT;
}

export function isStudentUser(userType) {
  return userType === USER_TYPES.STUDENT;
}

export function paginate(items, page, pageSize) {
  const start = (page - INITIAL_PAGE) * pageSize;
  return items.slice(start, start + pageSize);
}

export function getUserType(user) {
  return user?.actor || DEFAULT_USER_TYPE;
}

export function createImpactMetrics(impact) {
  const metrics = [];

  if (isStudentUser(impact.userType) && impact.foodReceived != null) {
    metrics.push({ label: METRIC_LABELS.FOOD_RECEIVED, value: impact.foodReceived });
  }

  metrics.push(
    { label: METRIC_LABELS.FOOD_DONATED, value: impact.foodDonated },
    { label: METRIC_LABELS.CO2_SAVED, value: `${impact.co2SavedKg} ${UNITS.KG}` },
    { label: METRIC_LABELS.LEADERBOARD_POSITION, value: `#${impact.leaderboardPosition}` }
  );

  return metrics;
}
