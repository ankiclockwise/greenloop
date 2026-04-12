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

function createMockUser({ email = "mock@example.com", fullName = "Mock User" } = {}) {
  return {
    uid: `mock-${Math.random().toString(36).slice(2, 11)}`,
    email,
    displayName: fullName,
    emailVerified: true
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
      setUser(nextUser);
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
      return signInWithEmailAndPassword(auth, email, password);
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
      return signInWithPopup(auth, googleProvider);
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
      setUser(auth.currentUser);
      return credentials;
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
