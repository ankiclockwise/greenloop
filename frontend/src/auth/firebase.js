import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID
};

const missingKeys = Object.entries(firebaseConfig)
  .filter(([, value]) => !value)
  .map(([key]) => key);

export const firebaseConfigError =
  missingKeys.length > 0
    ? `Firebase config is missing: ${missingKeys.join(", ")}. Add them to frontend/.env before signing in.`
    : "";

const useMockAuth = import.meta.env.VITE_USE_MOCK_AUTH === "true";

if (firebaseConfigError && !useMockAuth) {
  console.warn(firebaseConfigError);
}

const app = !useMockAuth && !firebaseConfigError
  ? initializeApp(firebaseConfig)
  : null;

export const auth = app ? getAuth(app) : null;
export const usingMockAuth = useMockAuth;
