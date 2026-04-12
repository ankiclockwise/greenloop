import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthShell } from "../auth/AuthShell";
import { useAuth } from "../auth/AuthProvider";

function validateSignup({ fullName, email, password, confirmPassword }) {
  if (!fullName.trim()) {
    return "Please enter your full name.";
  }

  if (password.length < 6) {
    return "Use at least 6 characters for the password.";
  }

  if (password !== confirmPassword) {
    return "Passwords do not match.";
  }

  if (!email.trim()) {
    return "Please enter a valid email address.";
  }

  return "";
}

function getAuthErrorMessage(error) {
  switch (error?.code) {
    case "auth/email-already-in-use":
      return "That email is already registered. Try signing in instead.";
    case "auth/invalid-email":
      return "Please enter a valid email address.";
    case "auth/weak-password":
      return "Please choose a stronger password.";
    case "auth/popup-closed-by-user":
      return "Google sign-in was closed before it finished.";
    case "auth/popup-blocked":
      return "Your browser blocked the Google sign-in popup. Please allow popups and try again.";
    case "auth/operation-not-allowed":
      return "This sign-in method is not enabled in Firebase Authentication yet.";
    case "auth/configuration-not-found":
      return "Firebase Authentication is not configured correctly for this project.";
    case "auth/network-request-failed":
      return "Firebase could not be reached. Check your internet connection and Firebase project settings.";
    case "auth/invalid-api-key":
      return "The Firebase API key is invalid. Recheck the values in frontend/.env.";
    case "auth/app-not-authorized":
      return "This app is not authorized with your Firebase project. Check the Firebase web app config.";
    case "auth/unauthorized-domain":
      return "This localhost domain is not authorized in Firebase Authentication.";
    default:
      return error?.code
        ? `Signup failed: ${error.code}`
        : "We could not create your account right now. Please try again.";
  }
}

export function SignupPage() {
  const { signup, loginWithGoogle } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: ""
  });
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    const validationMessage = validateSignup(formData);

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    setErrorMessage("");
    setIsSubmitting(true);

    try {
      await signup(formData);
      navigate("/", { replace: true });
    } catch (error) {
      console.error("Firebase signup error:", error);
      setErrorMessage(getAuthErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleGoogleSignup() {
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      await loginWithGoogle();
      navigate("/", { replace: true });
    } catch (error) {
      console.error("Firebase Google signup error:", error);
      setErrorMessage(getAuthErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <AuthShell
      title="Create account"
      description="Join GreenLoop to share surplus food, discover nearby listings, and measure local impact."
      alternateText="Already have an account?"
      alternateLabel="Sign in"
      alternateHref="/login"
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        <button
          className="social-button"
          type="button"
          onClick={handleGoogleSignup}
          disabled={isSubmitting}
        >
          <span className="social-mark">G</span>
          Continue with Google
        </button>

        <div className="auth-divider">
          <span>or create an account with email</span>
        </div>

        <label>
          Full name
          <input
            type="text"
            name="fullName"
            autoComplete="name"
            placeholder="Avery Johnson"
            value={formData.fullName}
            onChange={(event) =>
              setFormData((current) => ({
                ...current,
                fullName: event.target.value
              }))
            }
            required
          />
        </label>

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
            autoComplete="new-password"
            placeholder="At least 6 characters"
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

        <label>
          Confirm password
          <input
            type="password"
            name="confirmPassword"
            autoComplete="new-password"
            placeholder="Re-enter your password"
            value={formData.confirmPassword}
            onChange={(event) =>
              setFormData((current) => ({
                ...current,
                confirmPassword: event.target.value
              }))
            }
            required
          />
        </label>

        {errorMessage ? <p className="form-message error">{errorMessage}</p> : null}

        <button className="auth-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating account..." : "Create account"}
        </button>

        <p className="form-footer">
          Already registered? <Link to="/login">Sign in here</Link>
        </p>
      </form>
    </AuthShell>
  );
}
