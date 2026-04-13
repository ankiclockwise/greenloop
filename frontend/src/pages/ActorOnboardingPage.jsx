import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";

const ACTOR_OPTIONS = [
  {
    label: "Student",
    actor: "retail_user",
    description:
      "Individual members with extra food or people looking for affordable meals nearby."
  },
  {
    label: "Store",
    actor: "store",
    description:
      "Grocery stores with near-expiry inventory or excess items selling on heavy discount or free."
  },
  {
    label: "Diner",
    actor: "diner",
    description:
      "University dining halls or restaurants with end-of-day surplus donating food or selling at discounts."
  }
];

export function ActorOnboardingPage() {
  const { user, setActor, logout } = useAuth();
  const navigate = useNavigate();
  const [selectedActor, setSelectedActor] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleContinue() {
    if (!selectedActor) {
      setErrorMessage("Please select Student, Store, or Diner to continue.");
      return;
    }

    setErrorMessage("");
    setIsSubmitting(true);

    try {
      await setActor(selectedActor);
      navigate("/", { replace: true });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="actor-shell">
      <section className="actor-card">
        <div className="section-header">
          <div>
            <span className="eyebrow">Required setup</span>
            <h1>I am a Student, Store, or Diner</h1>
          </div>
          <button className="secondary-button" type="button" onClick={() => logout()}>
            Logout
          </button>
        </div>

        <p className="actor-intro">
          Welcome {user?.displayName || user?.email}. Choose one role to continue.
          We only ask this once.
        </p>

        <div className="actor-option-grid">
          {ACTOR_OPTIONS.map((option) => (
            <button
              key={option.actor}
              type="button"
              className={`actor-option${selectedActor === option.actor ? " active" : ""}`}
              onClick={() => setSelectedActor(option.actor)}
            >
              <strong>{option.label}</strong>
              <span>{option.description}</span>
            </button>
          ))}
        </div>

        {errorMessage ? <p className="form-message error">{errorMessage}</p> : null}

        <button className="auth-button" type="button" onClick={handleContinue} disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : "Continue"}
        </button>
      </section>
    </main>
  );
}
