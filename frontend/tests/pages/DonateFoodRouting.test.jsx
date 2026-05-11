import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import axios from "axios";
import { DashboardPage } from "../../src/pages/DashboardPage";
import { DiscoveryFeed } from "../../src/pages/DiscoveryFeed";
import { ImpactPage } from "../../src/pages/ImpactPage";

const authMocks = vi.hoisted(() => ({
  user: {
    email: "student@example.com",
    displayName: "Student User",
    actor: "retail_user"
  },
  logout: vi.fn()
}));

const geoState = vi.hoisted(() => ({
  lat: null,
  lng: null,
  error: null,
  loading: false,
  setManualLocation: vi.fn()
}));

const wsState = vi.hoisted(() => ({
  connected: false,
  newListings: []
}));

const impactDataMock = vi.hoisted(() => ({
  badges: [],
  dataNote: "",
  donorLeaderboard: { items: [], total: 0 },
  donorPage: 1,
  impact: {
    userType: "retail_user",
    badges: [],
    foodReceived: 0,
    foodDonated: 0,
    co2SavedKg: 0,
    leaderboardPosition: 1
  },
  setDonorPage: vi.fn(),
  setStudentPage: vi.fn(),
  studentLeaderboard: { items: [], total: 0 },
  studentPage: 1,
  visibleMetrics: []
}));

vi.mock("../../src/auth/AuthProvider", () => ({
  useAuth: () => authMocks
}));

vi.mock("../../src/hooks/useGeolocation", () => ({
  useGeolocation: () => geoState
}));

vi.mock("../../src/hooks/useFeedWebSocket", () => ({
  useFeedWebSocket: () => wsState
}));

vi.mock("../../src/hooks/useImpactData", () => ({
  useImpactData: () => impactDataMock
}));

vi.mock("axios", () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn().mockResolvedValue({ data: {} })
  }
}));

function LocationText() {
  const location = useLocation();
  return <p>Location: {location.pathname}</p>;
}

async function fillRequiredListingFields(user) {
  await user.type(screen.getByLabelText("Listing name"), "Campus leftovers");
  await user.type(screen.getByLabelText("Pickup window start"), "2026-05-10T10:00");
  await user.type(screen.getByLabelText("Pickup window end"), "2026-05-10T12:00");
  await user.type(screen.getByLabelText("Pickup location"), "Student Union");
}

describe("Donate Food modal", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    axios.post.mockResolvedValue({ data: {} });
  });

  it("opens the donate form from the feed Donate Food button", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/"]}>
        <Routes>
          <Route path="/" element={<DiscoveryFeed />} />
        </Routes>
      </MemoryRouter>
    );

    await user.click(screen.getByRole("button", { name: "Donate Food" }));

    expect(screen.getByRole("heading", { name: "Post a new listing" })).toBeInTheDocument();
  });

  it("opens the donate form on the dashboard without navigating away", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/dashboard"]}>
        <Routes>
          <Route path="/dashboard" element={<DashboardPage />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByRole("heading", { name: "My Dashboard" })).toBeInTheDocument());
    await user.click(screen.getByRole("button", { name: "Donate Food" }));

    expect(screen.getByRole("heading", { name: "Post a new listing" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "My Dashboard" })).toBeInTheDocument();
  });

  it("opens the donate form on impact without navigating away", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/impact"]}>
        <Routes>
          <Route path="/impact" element={<ImpactPage />} />
        </Routes>
      </MemoryRouter>
    );

    await user.click(screen.getByRole("button", { name: "Donate Food" }));

    expect(screen.getByRole("heading", { name: "Post a new listing" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "GreenLoop Impact" })).toBeInTheDocument();
  });

  it("goes to the feed after posting from the dashboard form", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/dashboard"]}>
        <Routes>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/" element={<LocationText />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByRole("heading", { name: "My Dashboard" })).toBeInTheDocument());
    await user.click(screen.getByRole("button", { name: "Donate Food" }));
    await fillRequiredListingFields(user);
    await user.click(screen.getByRole("button", { name: "Post listing" }));

    expect(await screen.findByText("Location: /")).toBeInTheDocument();
  });
});
