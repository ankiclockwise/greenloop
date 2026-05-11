import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import axios from "axios";
import { MemoryRouter, Route, Routes, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ActorOnboardingPage } from "../../src/pages/ActorOnboardingPage";
import { DashboardPage } from "../../src/pages/DashboardPage";
import { ImpactPage } from "../../src/pages/ImpactPage";
import { LoginPage } from "../../src/pages/LoginPage";
import { SignupPage } from "../../src/pages/SignupPage";

const authMocks = vi.hoisted(() => ({
  user: {
    email: "student@example.com",
    displayName: "Student User",
    actor: "retail_user"
  },
  login: vi.fn(),
  loginWithGoogle: vi.fn(),
  logout: vi.fn(),
  setActor: vi.fn(),
  signup: vi.fn()
}));

const impactDataMock = vi.hoisted(() => ({
  badges: [{ id: "badge-1", name: "First Donation", description: "Donated once.", earned: true }],
  dataNote: "Showing sample impact data until the backend impact APIs are available.",
  donorLeaderboard: {
    items: [{ userId: "d1", rank: 1, name: "Maple Market", foodDonated: 10, co2SavedKg: 20 }],
    total: 1
  },
  donorPage: 1,
  impact: {
    userType: "retail_user",
    foodReceived: 3,
    foodDonated: 2,
    co2SavedKg: 8,
    leaderboardPosition: 4,
    badges: [{ id: "badge-1" }]
  },
  setDonorPage: vi.fn(),
  setStudentPage: vi.fn(),
  studentLeaderboard: {
    items: [
      {
        userId: "s1",
        rank: 1,
        name: "Maya R.",
        foodReceived: 5,
        foodDonated: 1,
        co2SavedKg: 7
      }
    ],
    total: 1
  },
  studentPage: 1,
  visibleMetrics: [
    { label: "Food received", value: 3 },
    { label: "Food donated", value: 2 },
    { label: "CO2 saved", value: "8 kg" },
    { label: "Leaderboard position", value: "#4" }
  ]
}));

vi.mock("../../src/auth/AuthProvider", () => ({
  useAuth: () => authMocks
}));

vi.mock("../../src/hooks/useImpactData", () => ({
  useImpactData: () => impactDataMock
}));

vi.mock("axios", () => ({
  default: {
    get: vi.fn()
  }
}));

function LocationText() {
  const location = useLocation();
  return <p>Location: {location.pathname}</p>;
}

function renderWithRoutes(ui, initialPath = "/") {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Routes>
        <Route path="/" element={ui} />
        <Route path="/dashboard" element={ui} />
        <Route path="/impact" element={ui} />
        <Route path="/target" element={<LocationText />} />
        <Route path="/signup" element={<LocationText />} />
        <Route path="/login" element={<LocationText />} />
      </Routes>
    </MemoryRouter>
  );
}

describe("LoginPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("logs in with email and redirects to the requested page", async () => {
    const user = userEvent.setup();
    authMocks.login.mockResolvedValue({ user: authMocks.user });

    render(
      <MemoryRouter initialEntries={[{ pathname: "/", state: { from: { pathname: "/target" } } }]}>
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/target" element={<LocationText />} />
        </Routes>
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText("Email"), "student@example.com");
    await user.type(screen.getByLabelText("Password"), "secret123");
    await user.click(screen.getByRole("button", { name: "Sign in" }));

    expect(authMocks.login).toHaveBeenCalledWith("student@example.com", "secret123");
    await screen.findByText("Location: /target");
  });

  it("shows Firebase login errors and supports Google login", async () => {
    const user = userEvent.setup();
    authMocks.login.mockRejectedValueOnce({ code: "auth/invalid-credential" });
    authMocks.loginWithGoogle.mockResolvedValueOnce({ user: authMocks.user });
    vi.spyOn(console, "error").mockImplementation(() => {});

    renderWithRoutes(<LoginPage />);

    await user.type(screen.getByLabelText("Email"), "student@example.com");
    await user.type(screen.getByLabelText("Password"), "wrongpass");
    await user.click(screen.getByRole("button", { name: "Sign in" }));
    expect(await screen.findByText("That email/password combination did not match our records.")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /Continue with Google/ }));
    expect(authMocks.loginWithGoogle).toHaveBeenCalledTimes(1);
  });
});

describe("SignupPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("validates signup input before calling Firebase", async () => {
    const user = userEvent.setup();

    renderWithRoutes(<SignupPage />);

    await user.type(screen.getByLabelText("Full name"), "Avery Johnson");
    await user.type(screen.getByLabelText("Email"), "avery@example.com");
    await user.type(screen.getByLabelText("Password"), "secret1");
    await user.type(screen.getByLabelText("Confirm password"), "secret2");
    await user.click(screen.getByRole("button", { name: "Create account" }));

    expect(screen.getByText("Passwords do not match.")).toBeInTheDocument();
    expect(authMocks.signup).not.toHaveBeenCalled();
  });

  it("creates an account and supports Google signup", async () => {
    const user = userEvent.setup();
    authMocks.signup.mockResolvedValueOnce({ user: authMocks.user });
    authMocks.loginWithGoogle.mockResolvedValueOnce({ user: authMocks.user });

    renderWithRoutes(<SignupPage />);

    await user.type(screen.getByLabelText("Full name"), "Avery Johnson");
    await user.type(screen.getByLabelText("Email"), "avery@example.com");
    await user.type(screen.getByLabelText("Password"), "secret1");
    await user.type(screen.getByLabelText("Confirm password"), "secret1");
    await user.click(screen.getByRole("button", { name: "Create account" }));

    expect(authMocks.signup).toHaveBeenCalledWith({
      fullName: "Avery Johnson",
      email: "avery@example.com",
      password: "secret1",
      confirmPassword: "secret1"
    });

    await user.click(screen.getByRole("button", { name: /Continue with Google/ }));
    expect(authMocks.loginWithGoogle).toHaveBeenCalledTimes(1);
  });
});

describe("ActorOnboardingPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("requires a role, saves selected actor, and logs out", async () => {
    const user = userEvent.setup();
    authMocks.setActor.mockResolvedValueOnce();

    render(
      <MemoryRouter initialEntries={["/onboarding"]}>
        <Routes>
          <Route path="/onboarding" element={<ActorOnboardingPage />} />
          <Route path="/" element={<LocationText />} />
        </Routes>
      </MemoryRouter>
    );

    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(screen.getByText("Please select Student, Store, or Diner to continue.")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Logout" }));
    expect(authMocks.logout).toHaveBeenCalledTimes(1);

    await user.click(screen.getByRole("button", { name: /Store/ }));
    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(authMocks.setActor).toHaveBeenCalledWith("store");
    await screen.findByText("Location: /");
  });
});

describe("ImpactPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders impact data and logs out", async () => {
    const user = userEvent.setup();

    renderWithRoutes(<ImpactPage />, "/impact");

    expect(screen.getByRole("heading", { name: "GreenLoop Impact" })).toBeInTheDocument();
    expect(screen.getByText("Student impact for Student User")).toBeInTheDocument();
    expect(screen.getByText("Food received")).toBeInTheDocument();
    expect(screen.getByText("First Donation")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Stores + Diners" })).toBeInTheDocument();
    expect(screen.getByText("Maya R.")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Logout" }));
    expect(authMocks.logout).toHaveBeenCalledTimes(1);
  });
});

describe("DashboardPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    window.localStorage.clear();
  });

  it("falls back to sample listings, opens details, and logs out", async () => {
    const user = userEvent.setup();
    axios.get.mockRejectedValueOnce(new Error("Backend unavailable"));
    vi.spyOn(console, "error").mockImplementation(() => {});

    renderWithRoutes(<DashboardPage />, "/dashboard");

    expect(await screen.findByText("Showing saved local and sample listings because the backend is not available.")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /Fresh Bakery Bread/ }));

    const dialog = screen.getByRole("dialog");
    expect(dialog).toBeInTheDocument();
    expect(within(dialog).getByRole("heading", { name: "Fresh Bakery Bread" })).toBeInTheDocument();
    expect(within(dialog).getByText("Reserved by")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Close" }));
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());

    await user.click(screen.getByRole("button", { name: "Logout" }));
    expect(authMocks.logout).toHaveBeenCalledTimes(1);
  });
});
