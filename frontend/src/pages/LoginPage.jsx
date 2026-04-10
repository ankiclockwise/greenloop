import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { AuthShell } from "../components/AuthShell";
import { useAuth } from "../auth/AuthProvider";

function getAuthErrorMessage(error) {
  switch (error?.code) {
    case "auth/invalid-credential":
      return "That email/password combination did not match our records.";
    case "auth/too-many-requests":
      return "Too many attempts. Please wait a moment and try again.";
    case "auth/popup-closed-by-user":
      return "Google sign-in was closed before it finished.";
    case "auth/popup-blocked":
      return "Your browser blocked the Google sign-in popup. Please allow popups and try again.";
    case "auth/operation-not-allowed":
      return "This sign-in method is not enabled in Firebase Authentication yet.";
    case "auth/network-request-failed":
      return "Firebase could not be reached. Check your internet connection and Firebase project settings.";
    case "auth/invalid-api-key":
      return "The Firebase API key is invalid. Recheck the values in frontend/.env.";
    case "auth/unauthorized-domain":
      return "This localhost domain is not authorized in Firebase Authentication.";
    default:
      return error?.code
        ? `Login failed: ${error.code}`
        : "We could not sign you in right now. Please try again.";
  }
}

export function LoginPage() {
  const { login, loginWithGoogle } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const redirectTo = location.state?.from?.pathname || "/";

  async function handleSubmit(event) {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      await login(formData.email, formData.password);
      navigate(redirectTo, { replace: true });
    } catch (error) {
      console.error("Firebase login error:", error);
      setErrorMessage(getAuthErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleGoogleSignin() {
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      await loginWithGoogle();
      navigate(redirectTo, { replace: true });
    } catch (error) {
      console.error("Firebase Google login error:", error);
      setErrorMessage(getAuthErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <AuthShell
      title="Welcome back"
      description="Sign in to manage pickups, track donations, and keep food out of the landfill."
      alternateText="Need an account?"
      alternateLabel="Create one"
      alternateHref="/signup"
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        <button
          className="social-button"
          type="button"
          onClick={handleGoogleSignin}
          disabled={isSubmitting}
        >
          <span className="social-mark">G</span>
          Continue with Google
        </button>

        <div className="auth-divider">
          <span>or sign in with email</span>
        </div>

        <label>
          Email
          <input
            type="email"
            name="email"
            autoComplete="email"
            placeholder="you@school.edu"
            value={formData.email}
            onChange={(event) =>
              setFormData((current) => ({
                ...current,
                email: event.target.value
              }))
            }
            required
          />
        </label>

        <label>
          Password
          <input
            type="password"
            name="password"
            autoComplete="current-password"
            placeholder="Enter your password"
            value={formData.password}
            onChange={(event) =>
              setFormData((current) => ({
                ...current,
                password: event.target.value
              }))
            }
            required
          />
        </label>

        {errorMessage ? <p className="form-message error">{errorMessage}</p> : null}

        <button className="auth-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Signing in..." : "Sign in"}
        </button>

        <p className="form-footer">
          New to GreenLoop? <Link to="/signup">Create an account</Link>
        </p>
      </form>
    </AuthShell>
  );
}
