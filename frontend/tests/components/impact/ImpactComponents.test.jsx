import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { BadgePalette } from "../../../src/components/impact/BadgePalette";
import { ImpactMetrics } from "../../../src/components/impact/ImpactMetrics";
import { LeaderboardTable } from "../../../src/components/impact/LeaderboardTable";
import { Pagination } from "../../../src/components/impact/Pagination";

describe("ImpactMetrics", () => {
  it("renders supplied metrics plus badges earned", () => {
    render(
      <ImpactMetrics
        badgesEarned={2}
        metrics={[
          { label: "Food donated", value: 10 },
          { label: "CO2 saved", value: "12 kg" }
        ]}
      />
    );

    expect(screen.getByLabelText("My impact metrics")).toBeInTheDocument();
    expect(screen.getByText("Food donated")).toBeInTheDocument();
    expect(screen.getByText("12 kg")).toBeInTheDocument();
    expect(screen.getByText("Badges earned")).toBeInTheDocument();
    expect(screen.getByText("2")).toBeInTheDocument();
  });
});

describe("BadgePalette", () => {
  it("renders earned and locked badges with accessible descriptions", () => {
    render(
      <BadgePalette
        badges={[
          {
            id: "earned",
            name: "Loop Starter",
            description: "Completed a first pickup.",
            earned: true,
            earnedAt: "2026-05-10T00:00:00.000Z"
          },
          {
            id: "locked",
            name: "Waste Warrior",
            description: "Save 50kg of CO2.",
            earned: false
          }
        ]}
      />
    );

    expect(screen.getByText("My Badges")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Loop Starter" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Waste Warrior" })).toBeInTheDocument();
    expect(screen.getByText("Locked")).toBeInTheDocument();
    expect(screen.getByLabelText("Waste Warrior: Save 50kg of CO2.")).toBeInTheDocument();
    expect(screen.getByText("Earned", { exact: false })).toBeInTheDocument();
  });
});

describe("Pagination", () => {
  it("disables previous on the first page and advances to the next page", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();

    render(<Pagination page={1} total={12} onChange={onChange} />);

    expect(screen.getByText("Page 1 of 3")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Previous" })).toBeDisabled();

    await user.click(screen.getByRole("button", { name: "Next" }));
    expect(onChange).toHaveBeenCalledWith(2);
  });

  it("disables next on the last page and moves backward", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();

    render(<Pagination page={3} total={12} onChange={onChange} />);

    expect(screen.getByRole("button", { name: "Next" })).toBeDisabled();
    await user.click(screen.getByRole("button", { name: "Previous" }));
    expect(onChange).toHaveBeenCalledWith(2);
  });
});

describe("LeaderboardTable", () => {
  it("renders student leaderboard rows and pagination", async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();

    render(
      <LeaderboardTable
        title="Students"
        student
        page={1}
        total={8}
        onPageChange={onPageChange}
        items={[
          {
            userId: "u1",
            rank: 1,
            name: "Maya Patel",
            foodReceived: 4,
            foodDonated: 2,
            co2SavedKg: 9.5
          }
        ]}
      />
    );

    expect(screen.getByRole("heading", { name: "Students" })).toBeInTheDocument();
    expect(screen.getByRole("columnheader", { name: "Received" })).toBeInTheDocument();
    expect(screen.getByText("#1")).toBeInTheDocument();
    expect(screen.getByText("Maya Patel")).toBeInTheDocument();
    expect(screen.getByText("9.5 kg")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Next" }));
    expect(onPageChange).toHaveBeenCalledWith(2);
  });
});
