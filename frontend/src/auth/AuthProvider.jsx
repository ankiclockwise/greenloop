import {
  createContext,
  useContext,
  useEffect,
  useState
} from "react";
import {
  createUserWithEmailAndPassword,
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  updateProfile
} from "firebase/auth";
import { auth, firebaseConfigError, usingMockAuth } from "./firebase";

const AuthContext = createContext(null);
const googleProvider = new GoogleAuthProvider();
const ACTOR_STORAGE_KEY = "greenloop-user-actors";

function readStoredActors() {
  try {
    return JSON.parse(window.localStorage.getItem(ACTOR_STORAGE_KEY) || "{}");
  } catch {
    return {};
  }
}

function getStoredActor(email) {
  if (!email) {
    return "";
  }

  return readStoredActors()[email] || "";
}

function storeActor(email, actor) {
  if (!email || !actor) {
    return;
  }

  const next = readStoredActors();
  next[email] = actor;
  window.localStorage.setItem(ACTOR_STORAGE_KEY, JSON.stringify(next));
}

function normalizeUser(nextUser) {
  if (!nextUser) {
    return null;
  }

  return {
    uid: nextUser.uid,
    email: nextUser.email,
    displayName: nextUser.displayName,
    emailVerified: nextUser.emailVerified,
    photoURL: nextUser.photoURL || "",
    actor: getStoredActor(nextUser.email)
  };
}

function createMockUser({ email = "mock@example.com", fullName = "Mock User" } = {}) {
  return {
    uid: `mock-${Math.random().toString(36).slice(2, 11)}`,
    email,
    displayName: fullName,
    emailVerified: true,
    actor: getStoredActor(email)
  };
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!auth) {
      if (usingMockAuth) {
        setUser(createMockUser());
      }
      setLoading(false);
      return undefined;
    }

    const unsubscribe = onAuthStateChanged(auth, (nextUser) => {
      setUser(normalizeUser(nextUser));
      setLoading(false);
    });

    return unsubscribe;
  }, []);

  const value = {
    user,
    loading,
    configError: firebaseConfigError,
    async login(email, password) {
      if (!auth) {
        if (!usingMockAuth) {
          throw new Error(firebaseConfigError || "Firebase is not configured.");
        }
        const mockUser = createMockUser({ email, fullName: email.split("@")[0] });
        setUser(mockUser);
        return { user: mockUser };
      }
      const credentials = await signInWithEmailAndPassword(auth, email, password);
      const normalized = normalizeUser(credentials.user);
      setUser(normalized);
      return { ...credentials, user: normalized };
    },
    async loginWithGoogle() {
      if (!auth) {
        if (!usingMockAuth) {
          throw new Error(firebaseConfigError || "Firebase is not configured.");
        }
        const mockUser = createMockUser({ email: "mock-google@example.com", fullName: "Mock Google User" });
        setUser(mockUser);
        return { user: mockUser };
      }
      const credentials = await signInWithPopup(auth, googleProvider);
      const normalized = normalizeUser(credentials.user);
      setUser(normalized);
      return { ...credentials, user: normalized };
    },
    async signup({ fullName, email, password }) {
      if (!auth) {
        if (!usingMockAuth) {
          throw new Error(firebaseConfigError || "Firebase is not configured.");
        }
        const mockUser = createMockUser({ email, fullName: fullName || email.split("@")[0] });
        setUser(mockUser);
        return { user: mockUser };
      }
      const credentials = await createUserWithEmailAndPassword(auth, email, password);
      if (fullName.trim()) {
        await updateProfile(credentials.user, {
          displayName: fullName.trim()
        });
      }
      const normalized = normalizeUser(auth.currentUser);
      setUser(normalized);
      return { ...credentials, user: normalized };
    },
    async setActor(actor) {
      if (!user?.email || !actor) {
        return;
      }

      storeActor(user.email, actor);
      setUser((current) => (current ? { ...current, actor } : current));
    },
    async logout() {
      if (!auth) {
        setUser(null);
        return;
      }
      await signOut(auth);
      setUser(null);
    }
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
