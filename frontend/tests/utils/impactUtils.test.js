import { describe, expect, it } from "vitest";
import {
  createImpactMetrics,
  formatUserType,
  getUserType,
  paginate
} from "../../src/utils/impactUtils";

describe("impactUtils", () => {
  it("paginates items from the first page", () => {
    expect(paginate(["a", "b", "c", "d", "e"], 2, 2)).toEqual(["c", "d"]);
  });

  it("falls back to the default user type when a user has no actor", () => {
    expect(getUserType({ email: "student@example.com" })).toBe("retail_user");
    expect(formatUserType("UNKNOWN")).toBe("Member");
  });

  it("includes student-only received food metrics for student impact", () => {
    expect(
      createImpactMetrics({
        userType: "retail_user",
        foodReceived: 4,
        foodDonated: 2,
        co2SavedKg: 12.5,
        leaderboardPosition: 3
      })
    ).toEqual([
      { label: "Food received", value: 4 },
      { label: "Food donated", value: 2 },
      { label: "CO2 saved", value: "12.5 kg" },
      { label: "Leaderboard position", value: "#3" }
    ]);
  });
});
