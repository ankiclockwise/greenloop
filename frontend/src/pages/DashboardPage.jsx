import { useAuth } from "../auth/AuthProvider";
import { Link } from "react-router-dom";

export function DashboardPage() {
  const { user, logout } = useAuth();

  return (
    <main className="dashboard-shell">
      <section className="dashboard-card">
        <span className="eyebrow">Authenticated</span>
        <h1>Welcome, {user?.displayName || user?.email}.</h1>
        <p>
          Your Firebase login flow is connected. This page is protected and only
          renders for signed-in users.
        </p>
        <div className="dashboard-metrics">
          <article>
            <strong>Auth provider</strong>
            <span>Firebase Email/Password</span>
          </article>
          <article>
            <strong>Next backend step</strong>
            <span>Exchange Firebase ID token with the Java API</span>
          </article>
        </div>
        <div className="dashboard-actions">
          <Link className="auth-button dashboard-link" to="/feed">
            Open feed
          </Link>
          <button className="secondary-button" type="button" onClick={() => logout()}>
            Sign out
          </button>
        </div>
      </section>
    </main>
  );
}
