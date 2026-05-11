import { act, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthProvider, useAuth } from "../../src/auth/AuthProvider";
import {
  createUserWithEmailAndPassword,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  updateProfile
} from "firebase/auth";

const firebaseState = vi.hoisted(() => ({
  auth: { currentUser: null },
  firebaseConfigError: "",
  usingMockAuth: false
}));

vi.mock("../../src/auth/firebase", () => firebaseState);

vi.mock("firebase/auth", () => ({
  createUserWithEmailAndPassword: vi.fn(),
  GoogleAuthProvider: vi.fn(function GoogleAuthProvider() {
    return { providerId: "google.com" };
  }),
  onAuthStateChanged: vi.fn(),
  signInWithEmailAndPassword: vi.fn(),
  signInWithPopup: vi.fn(),
  signOut: vi.fn(),
  updateProfile: vi.fn()
}));

let latestAuth;

function AuthProbe() {
  latestAuth = useAuth();

  return (
    <div>
      <p>Loading: {String(latestAuth.loading)}</p>
      <p>User: {latestAuth.user?.email || "none"}</p>
      <p>Actor: {latestAuth.user?.actor || "none"}</p>
    </div>
  );
}

const firebaseUser = {
  uid: "uid-1",
  email: "student@example.com",
  displayName: "Student User",
  emailVerified: true,
  photoURL: "https://example.com/photo.jpg"
};

describe("AuthProvider", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    window.localStorage.clear();
    firebaseState.auth.currentUser = firebaseUser;
    firebaseState.firebaseConfigError = "";
    firebaseState.usingMockAuth = false;

    onAuthStateChanged.mockImplementation((auth, callback) => {
      callback(firebaseUser);
      return vi.fn();
    });
  });

  it("normalizes Firebase users and restores a stored actor", async () => {
    window.localStorage.setItem(
      "greenloop-user-actors",
      JSON.stringify({ "student@example.com": "retail_user" })
    );

    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>
    );

    await screen.findByText("Loading: false");
    expect(screen.getByText("User: student@example.com")).toBeInTheDocument();
    expect(screen.getByText("Actor: retail_user")).toBeInTheDocument();
    expect(onAuthStateChanged).toHaveBeenCalledWith(firebaseState.auth, expect.any(Function));
  });

  it("logs in, signs in with Google, sets actor, and logs out", async () => {
    signInWithEmailAndPassword.mockResolvedValueOnce({
      user: { ...firebaseUser, email: "login@example.com", displayName: "Login User" }
    });
    signInWithPopup.mockResolvedValueOnce({
      user: { ...firebaseUser, email: "google@example.com", displayName: "Google User" }
    });
    signOut.mockResolvedValueOnce();

    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>
    );

    await screen.findByText("Loading: false");

    await act(async () => {
      await latestAuth.login("login@example.com", "secret123");
    });
    expect(signInWithEmailAndPassword).toHaveBeenCalledWith(
      firebaseState.auth,
      "login@example.com",
      "secret123"
    );
    expect(screen.getByText("User: login@example.com")).toBeInTheDocument();

    await act(async () => {
      await latestAuth.loginWithGoogle();
    });
    expect(signInWithPopup).toHaveBeenCalledWith(firebaseState.auth, expect.any(Object));
    expect(screen.getByText("User: google@example.com")).toBeInTheDocument();

    await act(async () => {
      await latestAuth.setActor("store");
    });
    expect(screen.getByText("Actor: store")).toBeInTheDocument();
    expect(JSON.parse(window.localStorage.getItem("greenloop-user-actors"))).toMatchObject({
      "google@example.com": "store"
    });

    await act(async () => {
      await latestAuth.logout();
    });
    expect(signOut).toHaveBeenCalledWith(firebaseState.auth);
    expect(screen.getByText("User: none")).toBeInTheDocument();
  });

  it("signs up with Firebase and updates the display name", async () => {
    const signupUser = {
      ...firebaseUser,
      email: "new@example.com",
      displayName: ""
    };
    firebaseState.auth.currentUser = {
      ...signupUser,
      displayName: "New Person"
    };
    createUserWithEmailAndPassword.mockResolvedValueOnce({ user: signupUser });
    updateProfile.mockResolvedValueOnce();

    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>
    );

    await screen.findByText("Loading: false");

    await act(async () => {
      await latestAuth.signup({
        fullName: " New Person ",
        email: "new@example.com",
        password: "secret123"
      });
    });

    expect(createUserWithEmailAndPassword).toHaveBeenCalledWith(
      firebaseState.auth,
      "new@example.com",
      "secret123"
    );
    expect(updateProfile).toHaveBeenCalledWith(signupUser, { displayName: "New Person" });
    await waitFor(() => expect(screen.getByText("User: new@example.com")).toBeInTheDocument());
  });

});
