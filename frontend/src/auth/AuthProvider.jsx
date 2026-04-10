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
import { auth, firebaseConfigError } from "../firebase";

const AuthContext = createContext(null);
const googleProvider = new GoogleAuthProvider();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!auth) {
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
        throw new Error(firebaseConfigError || "Firebase is not configured.");
      }
      return signInWithEmailAndPassword(auth, email, password);
    },
    async loginWithGoogle() {
      if (!auth) {
        throw new Error(firebaseConfigError || "Firebase is not configured.");
      }
      return signInWithPopup(auth, googleProvider);
    },
    async signup({ fullName, email, password }) {
      if (!auth) {
        throw new Error(firebaseConfigError || "Firebase is not configured.");
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
        return;
      }
      return signOut(auth);
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
