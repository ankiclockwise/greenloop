import { Link } from "react-router-dom";

export function AuthShell({
  title,
  description,
  alternateLabel,
  alternateHref,
  alternateText,
  children
}) {
  return (
    <main className="auth-shell">
      <section className="auth-hero">
        <div className="brand-chip">GreenLoop</div>
        <h1>Food rescue starts with better access.</h1>
        <p>
          Connect students, donors, stores, and dining halls in one place and
          turn surplus meals into measurable climate impact.
        </p>
        <div className="impact-card">
          <span className="impact-kicker">Mission Snapshot</span>
          <strong>Every rescued meal avoids waste and keeps good food moving.</strong>
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-panel-header">
          <div>
            <span className="eyebrow">{title}</span>
            <h2>{description}</h2>
          </div>
          <p>
            {alternateText} <Link to={alternateHref}>{alternateLabel}</Link>
          </p>
        </div>
        {children}
      </section>
    </main>
  );
}
