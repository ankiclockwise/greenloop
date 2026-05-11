import { describe, expect, it } from "vitest";
import {
  BADGE_CATALOG,
  DONOR_LEADERBOARD,
  STUDENT_LEADERBOARD,
  createFallbackImpact
} from "../../src/data/impactMockData";

describe("impactMockData", () => {
  it("creates fallback impact for the default student user", () => {
    const impact = createFallbackImpact({});

    expect(impact.userType).toBe("retail_user");
    expect(impact.foodReceived).toBeGreaterThan(0);
    expect(impact.badges.every((badge) => badge.earned)).toBe(true);
    expect(impact.badges.every((badge) => badge.appliesTo.includes("retail_user"))).toBe(true);
  });

  it("creates fallback impact for donor users", () => {
    const impact = createFallbackImpact({ actor: "store" });

    expect(impact.userType).toBe("store");
    expect(impact.completedPickups).toBeGreaterThan(0);
    expect(impact.badges.every((badge) => badge.appliesTo.includes("store"))).toBe(true);
  });

  it("keeps mock catalog and leaderboard data shaped for impact screens", () => {
    expect(BADGE_CATALOG).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          id: "first_donation",
          criteria: expect.objectContaining({ metric: "foodDonated" })
        })
      ])
    );
    expect(DONOR_LEADERBOARD[0]).toMatchObject({
      rank: 1,
      userType: "store",
      name: "Maple Market"
    });
    expect(STUDENT_LEADERBOARD[0]).toMatchObject({
      rank: 1,
      userType: "retail_user",
      foodReceived: expect.any(Number)
    });
  });
});
