import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthShell } from "../../src/auth/AuthShell";
import { ProtectedRoute } from "../../src/auth/ProtectedRoute";
import { PublicOnlyRoute } from "../../src/auth/PublicOnlyRoute";

const authState = vi.hoisted(() => ({
  user: null,
  loading: false,
  configError: ""
}));

vi.mock("../../src/auth/AuthProvider", () => ({
  useAuth: () => authState
}));

function LocationText() {
  const location = useLocation();
  return <p>Location: {location.pathname}</p>;
}

function renderProtected(path = "/") {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <p>Private content</p>
            </ProtectedRoute>
          }
        />
        <Route
          path="/onboarding"
          element={
            <ProtectedRoute>
              <p>Onboarding content</p>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<LocationText />} />
      </Routes>
    </MemoryRouter>
  );
}

describe("ProtectedRoute", () => {
  beforeEach(() => {
    authState.user = null;
    authState.loading = false;
    authState.configError = "";
  });

  it("shows loading and configuration error states", () => {
    authState.loading = true;
    const { rerender } = renderProtected();
    expect(screen.getByText("Checking your session...")).toBeInTheDocument();

    authState.loading = false;
    authState.configError = "Missing Firebase key.";
    rerender(
      <MemoryRouter>
        <ProtectedRoute>
          <p>Private content</p>
        </ProtectedRoute>
      </MemoryRouter>
    );
    expect(screen.getByRole("heading", { name: "Firebase setup needed" })).toBeInTheDocument();
    expect(screen.getByText("Missing Firebase key.")).toBeInTheDocument();
  });

  it("redirects guests to login", () => {
    renderProtected("/");
    expect(screen.getByText("Location: /login")).toBeInTheDocument();
  });

  it("redirects users without an actor to onboarding", () => {
    authState.user = { email: "student@example.com" };

    render(
      <MemoryRouter initialEntries={["/"]}>
        <Routes>
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <p>Private content</p>
              </ProtectedRoute>
            }
          />
          <Route path="/onboarding" element={<LocationText />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText("Location: /onboarding")).toBeInTheDocument();
  });

  it("allows actor users into private routes and away from onboarding", () => {
    authState.user = { email: "donor@example.com", actor: "store" };
    const { rerender } = renderProtected("/");

    expect(screen.getByText("Private content")).toBeInTheDocument();

    rerender(
      <MemoryRouter initialEntries={["/onboarding"]}>
        <Routes>
          <Route
            path="/onboarding"
            element={
              <ProtectedRoute>
                <p>Onboarding content</p>
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<LocationText />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText("Location: /")).toBeInTheDocument();
  });
});

describe("PublicOnlyRoute", () => {
  beforeEach(() => {
    authState.user = null;
    authState.loading = false;
    authState.configError = "";
  });

  it("shows loading, error, public content, and signed-in redirects", () => {
    authState.loading = true;
    const { rerender } = render(
      <MemoryRouter>
        <PublicOnlyRoute>
          <p>Login form</p>
        </PublicOnlyRoute>
      </MemoryRouter>
    );

    expect(screen.getByText("Loading...")).toBeInTheDocument();

    authState.loading = false;
    authState.configError = "Missing Firebase key.";
    rerender(
      <MemoryRouter>
        <PublicOnlyRoute>
          <p>Login form</p>
        </PublicOnlyRoute>
      </MemoryRouter>
    );
    expect(screen.getByRole("heading", { name: "Firebase setup needed" })).toBeInTheDocument();

    authState.configError = "";
    rerender(
      <MemoryRouter>
        <PublicOnlyRoute>
          <p>Login form</p>
        </PublicOnlyRoute>
      </MemoryRouter>
    );
    expect(screen.getByText("Login form")).toBeInTheDocument();

    authState.user = { email: "student@example.com" };
    rerender(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route
            path="/login"
            element={
              <PublicOnlyRoute>
                <p>Login form</p>
              </PublicOnlyRoute>
            }
          />
          <Route path="/" element={<LocationText />} />
        </Routes>
      </MemoryRouter>
    );
    expect(screen.getByText("Location: /")).toBeInTheDocument();
  });
});

describe("AuthShell", () => {
  it("renders auth shell copy, alternate link, and children", () => {
    render(
      <MemoryRouter>
        <AuthShell
          title="Welcome"
          description="Sign in to continue"
          alternateText="Need an account?"
          alternateLabel="Sign up"
          alternateHref="/signup"
        >
          <form aria-label="Login form" />
        </AuthShell>
      </MemoryRouter>
    );

    expect(screen.getByText("GreenLoop")).toBeInTheDocument();
    expect(screen.getByText("Food rescue starts with better access.")).toBeInTheDocument();
    expect(screen.getByText("Welcome")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Sign up" })).toHaveAttribute("href", "/signup");
    expect(screen.getByRole("form", { name: "Login form" })).toBeInTheDocument();
  });
});
