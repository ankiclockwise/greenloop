import { renderHook, waitFor } from "@testing-library/react";
import axios from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { IMPACT_API_ENDPOINTS } from "../../src/constants/impactConstants";
import { IMPACT_COPY } from "../../src/constants/impactConstants";
import { useImpactData } from "../../src/hooks/useImpactData";

vi.mock("axios", () => ({
  default: {
    get: vi.fn()
  }
}));

describe("useImpactData", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("loads impact, badges, and leaderboard data from APIs", async () => {
    axios.get.mockImplementation((url) => {
      if (url === IMPACT_API_ENDPOINTS.ME) {
        return Promise.resolve({
        data: {
          userType: "store",
          foodDonated: 20,
          co2SavedKg: 50,
          leaderboardPosition: 2,
          badges: [{ id: "api-badge" }]
        }
        });
      }

      if (url === IMPACT_API_ENDPOINTS.BADGES) {
        return Promise.resolve({
        data: {
          badges: [{ id: "api-badge", name: "API Badge", earned: true }]
        }
        });
      }

      if (url === IMPACT_API_ENDPOINTS.DONOR_LEADERBOARD) {
        return Promise.resolve({
        data: {
          items: [{ userId: "donor-1", rank: 1, name: "Maple Market" }],
          total: 1
        }
        });
      }

      if (url === IMPACT_API_ENDPOINTS.STUDENT_LEADERBOARD) {
        return Promise.resolve({
        data: {
          items: [{ userId: "student-1", rank: 1, name: "Maya R." }],
          total: 1
        }
      });
      }

      return Promise.reject(new Error(`Unexpected URL: ${url}`));
    });

    const { result } = renderHook(() =>
      useImpactData({ email: "donor@example.com", actor: "store" })
    );

    await waitFor(() => expect(result.current.dataNote).toBe(""));
    await waitFor(() => expect(result.current.badges[0].name).toBe("API Badge"));
    await waitFor(() => expect(result.current.donorLeaderboard.items[0].name).toBe("Maple Market"));
    await waitFor(() => expect(result.current.studentLeaderboard.items[0].name).toBe("Maya R."));

    expect(result.current.impact.foodDonated).toBe(20);
    expect(result.current.visibleMetrics).toEqual(
      expect.arrayContaining([{ label: "Food donated", value: 20 }])
    );
  });

  it("falls back to mock data when APIs fail", async () => {
    axios.get.mockRejectedValue(new Error("Backend unavailable"));

    const { result } = renderHook(() =>
      useImpactData({ email: "student@example.com", actor: "retail_user" })
    );

    await waitFor(() => expect(result.current.dataNote).toBe(IMPACT_COPY.DATA_NOTE));
    await waitFor(() => expect(result.current.donorLeaderboard.items.length).toBeGreaterThan(0));
    await waitFor(() => expect(result.current.studentLeaderboard.items.length).toBeGreaterThan(0));

    expect(result.current.impact.userType).toBe("retail_user");
    expect(result.current.badges.length).toBeGreaterThan(0);
  });
});
